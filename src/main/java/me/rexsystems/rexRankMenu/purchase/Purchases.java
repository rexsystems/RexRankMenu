package me.rexsystems.rexRankMenu.purchase;

import me.rexsystems.rexRankMenu.RexRankMenu;
import me.rexsystems.rexRankMenu.config.RankDress;
import me.rexsystems.rexRankMenu.economy.Amounts;
import me.rexsystems.rexRankMenu.economy.Wallets;
import me.rexsystems.rexRankMenu.luck.TrackSnapshot;
import me.rexsystems.rexRankMenu.menu.MenuText;
import me.rexsystems.rexRankMenu.menu.RankMenu;
import me.rexsystems.rexRankMenu.menu.RankMenuHolder;
import me.rexsystems.rexRankMenu.rank.RankTrack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public final class Purchases {

    private final RexRankMenu plugin;

    public Purchases(RexRankMenu plugin) {
        this.plugin = plugin;
    }

    public void buy(Player player, String group) {
        UUID id = player.getUniqueId();
        if (!player.hasPermission("rexrankmenu.buy")) {
            plugin.tell(player, "no-buy", Map.of());
            return;
        }
        if (!plugin.beginPurchase(id)) {
            plugin.tell(player, "busy", Map.of());
            return;
        }
        String name = player.getName();
        var config = plugin.config();
        plugin.luckPerms().load(id, config.track(), config.serverContext(), config.order())
                .whenComplete((snapshot, error) -> plugin.syncPlayer(id, () -> afterLoad(id, name, group, snapshot, error)));
    }

    private void afterLoad(UUID id, String name, String group, Optional<TrackSnapshot> snapshot, Throwable error) {
        try {
            Player player = plugin.getServer().getPlayer(id);
            if (player == null) {
                plugin.endPurchase(id);
                return;
            }
            var config = plugin.config();
            if (error != null) {
                plugin.getLogger().log(Level.WARNING, "Could not read ranks for " + name, error);
                plugin.tell(player, "luckperms-failed", Map.of("track", config.track(), "player", name));
                plugin.endPurchase(id);
                return;
            }
            if (snapshot == null || snapshot.isEmpty()) {
                plugin.tell(player, "track-missing", Map.of("track", config.track(), "player", name));
                plugin.endPurchase(id);
                return;
            }
            TrackSnapshot loaded = snapshot.get();
            Optional<String> next = RankTrack.nextRank(loaded.order(), loaded.owned());
            if (next.isEmpty()) {
                plugin.tell(player, "highest", MenuText.fields(config, name, group, "", ""));
                plugin.endPurchase(id);
                return;
            }
            if (!next.get().equalsIgnoreCase(group)) {
                String key = RankTrack.hasGroup(loaded.owned(), group) ? "already-owned" : "not-next";
                plugin.tell(player, key, MenuText.fields(config, name, group, next.get(), ""));
                plugin.endPurchase(id);
                return;
            }
            RankDress dress = config.dressFor(group);
            double price = dress.price();
            if (price > 0d) {
                Wallets.Result afford = plugin.wallets().afford(player, dress.currency(), price);
                if (afford == Wallets.Result.NOT_ENOUGH) {
                    plugin.tell(player, "not-enough", MenuText.fields(config, name, group, next.get(), ""));
                    plugin.endPurchase(id);
                    return;
                }
                if (afford != Wallets.Result.OK) {
                    plugin.tell(player, "currency-missing", MenuText.fields(config, name, group, next.get(), ""));
                    plugin.endPurchase(id);
                    return;
                }
            }
            Map<String, String> fields = MenuText.fields(config, name, group, next.get(), "");
            plugin.wallets().take(player, dress.currency(), price, result -> {
                if (result == Wallets.Result.OK) {
                    grant(id, name, group, dress.currency(), price);
                    return;
                }
                plugin.syncPlayer(id, () -> {
                    Player online = plugin.getServer().getPlayer(id);
                    if (online != null) {
                        String key = result == Wallets.Result.NOT_ENOUGH ? "not-enough" : "currency-missing";
                        plugin.tell(online, key, fields);
                    }
                    plugin.endPurchase(id);
                });
            });
        } catch (RuntimeException ex) {
            plugin.getLogger().log(Level.WARNING, "Rank purchase failed for " + name, ex);
            plugin.endPurchase(id);
        }
    }

    private void grant(UUID id, String name, String group, String currency, double price) {
        CompletableFuture<Boolean> future;
        try {
            future = plugin.luckPerms().grantAndVerify(id, group, plugin.config().serverContext());
        } catch (RuntimeException ex) {
            settle(id, name, group, currency, price, false, ex);
            return;
        }
        future.whenComplete((present, error) ->
                settle(id, name, group, currency, price, error == null && Boolean.TRUE.equals(present), error));
    }

    private void settle(UUID id, String name, String group, String currency, double price, boolean verified, Throwable error) {
        plugin.syncPlayer(id, () -> {
            if (!RankTrack.shouldRefund(verified)) {
                Player player = plugin.getServer().getPlayer(id);
                if (player != null) {
                    plugin.tell(player, "purchased", MenuText.fields(plugin.config(), name, group, "", ""));
                    markOwned(player, id, group);
                }
                plugin.endPurchase(id);
                if (player != null) {
                    plugin.rankMenu().refresh(player);
                }
                return;
            }
            if (error != null) {
                plugin.getLogger().log(Level.WARNING, "Could not add " + group + " to " + name, error);
            }
            if (price <= 0d) {
                plugin.getLogger().warning(group + " was not applied for " + name + ".");
                tellFailed(id, name, group);
                plugin.endPurchase(id);
                return;
            }
            plugin.wallets().refund(id, name, currency, price, returned -> plugin.syncPlayer(id, () -> {
                String amount = Amounts.format(price);
                if (returned) {
                    plugin.getLogger().warning("Refunded " + amount + " " + currency + " to " + name + " because " + group + " was not applied.");
                } else {
                    plugin.getLogger().severe("Refund failed for " + name + ": " + amount + " " + currency + " after " + group + " was not applied.");
                }
                tellFailed(id, name, group);
                plugin.endPurchase(id);
            }));
        });
    }

    private void markOwned(Player player, UUID id, String group) {
        Inventory top = player.getOpenInventory().getTopInventory();
        if (!(top.getHolder() instanceof RankMenuHolder holder) || !holder.playerId().equals(id)) {
            return;
        }
        Set<String> owned = new HashSet<>(holder.owned());
        owned.add(group);
        holder.show(holder.order(), owned, holder.slots());
        RankMenu.paint(plugin, player, holder);
    }

    private void tellFailed(UUID id, String name, String group) {
        Player player = plugin.getServer().getPlayer(id);
        if (player != null) {
            plugin.tell(player, "failed", MenuText.fields(plugin.config(), name, group, "", ""));
        }
    }
}
