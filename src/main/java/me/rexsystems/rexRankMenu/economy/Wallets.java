package me.rexsystems.rexRankMenu.economy;

import me.rexsystems.rexRankMenu.RexRankMenu;
import me.rexsystems.rexRankMenu.config.CurrencySpec;
import me.rexsystems.rexRankMenu.util.Placeholders;
import me.rexsystems.rexRankMenu.util.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

public final class Wallets {

    public enum Result {
        OK,
        NOT_ENOUGH,
        UNAVAILABLE
    }

    private final RexRankMenu plugin;

    public Wallets(RexRankMenu plugin) {
        this.plugin = plugin;
    }

    public Result afford(Player player, String currencyId, double amount) {
        if (amount <= 0d) {
            return Result.OK;
        }
        CurrencySpec spec = plugin.config().currency(currencyId);
        if (spec == null) {
            return Result.UNAVAILABLE;
        }
        if (spec.vault()) {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                return Result.UNAVAILABLE;
            }
            return VaultBridge.has(player, amount) ? Result.OK : Result.NOT_ENOUGH;
        }
        if (!spec.placeholder() || spec.balancePlaceholder().isBlank() || spec.takeCommand().isBlank()) {
            return Result.UNAVAILABLE;
        }
        double balance = Amounts.parse(Placeholders.apply(player, spec.balancePlaceholder()));
        if (Double.isNaN(balance)) {
            return Result.UNAVAILABLE;
        }
        return Amounts.covers(balance, amount) ? Result.OK : Result.NOT_ENOUGH;
    }

    public void take(Player player, String currencyId, double amount, Consumer<Result> callback) {
        if (amount <= 0d) {
            callback.accept(Result.OK);
            return;
        }
        CurrencySpec spec = plugin.config().currency(currencyId);
        if (spec == null) {
            callback.accept(Result.UNAVAILABLE);
            return;
        }
        if (spec.vault()) {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                callback.accept(Result.UNAVAILABLE);
                return;
            }
            callback.accept(VaultBridge.withdraw(player, amount) ? Result.OK : Result.NOT_ENOUGH);
            return;
        }
        String command = Amounts.command(spec.takeCommand(), player.getName(), amount);
        if (command.isBlank()) {
            callback.accept(Result.UNAVAILABLE);
            return;
        }
        SchedulerUtils.runSync(plugin, () -> callback.accept(dispatch(command) ? Result.OK : Result.UNAVAILABLE));
    }

    public void refund(UUID playerId, String playerName, String currencyId, double amount, Consumer<Boolean> callback) {
        if (amount <= 0d) {
            callback.accept(true);
            return;
        }
        CurrencySpec spec = plugin.config().currency(currencyId);
        if (spec == null) {
            callback.accept(false);
            return;
        }
        if (spec.vault()) {
            if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
                callback.accept(false);
                return;
            }
            OfflinePlayer offline = Bukkit.getOfflinePlayer(playerId);
            callback.accept(VaultBridge.deposit(offline, amount));
            return;
        }
        String command = Amounts.command(spec.giveCommand(), playerName, amount);
        if (command.isBlank()) {
            callback.accept(false);
            return;
        }
        SchedulerUtils.runSync(plugin, () -> callback.accept(dispatch(command)));
    }

    private boolean dispatch(String command) {
        try {
            return Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
        } catch (RuntimeException ex) {
            plugin.getLogger().warning("Currency command failed: " + ex.getMessage());
            return false;
        }
    }
}
