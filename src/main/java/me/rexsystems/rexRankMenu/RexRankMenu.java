package me.rexsystems.rexRankMenu;

import me.rexsystems.rexRankMenu.command.RankCommand;
import me.rexsystems.rexRankMenu.config.CurrencySpec;
import me.rexsystems.rexRankMenu.config.MenuConfig;
import me.rexsystems.rexRankMenu.economy.Wallets;
import me.rexsystems.rexRankMenu.luck.LuckPermsAccess;
import me.rexsystems.rexRankMenu.menu.RankMenu;
import me.rexsystems.rexRankMenu.menu.RankMenuHolder;
import me.rexsystems.rexRankMenu.menu.RankMenuListener;
import me.rexsystems.rexRankMenu.purchase.Purchases;
import me.rexsystems.rexRankMenu.util.MessageService;
import me.rexsystems.rexRankMenu.util.Placeholders;
import me.rexsystems.rexRankMenu.util.SchedulerUtils;
import me.rexsystems.rexRankMenu.util.TextParser;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RexRankMenu extends JavaPlugin {

    private boolean started;
    private volatile MenuConfig menuConfig;
    private LuckPermsAccess luckPerms;
    private Wallets wallets;
    private Purchases purchases;
    private RankMenu rankMenu;
    private final Set<UUID> buying = ConcurrentHashMap.newKeySet();

    @Override
    public void onEnable() {
        Plugin luckPermsPlugin = getServer().getPluginManager().getPlugin("LuckPerms");
        if (luckPermsPlugin == null) {
            getLogger().severe("LuckPerms is not installed. Disabling RexRankMenu.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!luckPermsPlugin.isEnabled()) {
            getLogger().severe("LuckPerms is not enabled. Disabling RexRankMenu.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        try {
            this.menuConfig = MenuConfig.load(this);
        } catch (RuntimeException ex) {
            getLogger().severe("Could not read the configuration: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        this.luckPerms = new LuckPermsAccess();
        this.wallets = new Wallets(this);
        this.purchases = new Purchases(this);
        this.rankMenu = new RankMenu(this);
        getServer().getPluginManager().registerEvents(new RankMenuListener(this), this);
        PluginCommand command = getCommand("rexrankmenu");
        if (command == null) {
            getLogger().severe("Command rexrankmenu is missing from plugin.yml. Disabling RexRankMenu.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        RankCommand executor = new RankCommand(this);
        command.setExecutor(executor);
        command.setTabCompleter(executor);
        warnCurrencies();
        this.started = true;
        getLogger().info("RexRankMenu enabled.");
    }

    @Override
    public void onDisable() {
        if (!started) {
            return;
        }
        buying.clear();
        getLogger().info("RexRankMenu disabled.");
    }

    public void reloadPlugin() {
        this.menuConfig = MenuConfig.load(this);
        warnCurrencies();
        for (Player player : getServer().getOnlinePlayers()) {
            SchedulerUtils.runForPlayer(this, player, () -> {
                Inventory top = player.getOpenInventory().getTopInventory();
                if (top.getHolder() instanceof RankMenuHolder) {
                    player.closeInventory();
                }
            });
        }
    }

    public MenuConfig config() {
        return menuConfig;
    }

    public LuckPermsAccess luckPerms() {
        return luckPerms;
    }

    public Wallets wallets() {
        return wallets;
    }

    public Purchases purchases() {
        return purchases;
    }

    public RankMenu rankMenu() {
        return rankMenu;
    }

    public boolean beginPurchase(UUID playerId) {
        return buying.add(playerId);
    }

    public void endPurchase(UUID playerId) {
        buying.remove(playerId);
    }

    public void tell(CommandSender sender, String key, Map<String, String> values) {
        String template = menuConfig.message(key);
        if (template.isBlank()) {
            return;
        }
        String resolved = TextParser.apply(template, values);
        if (sender instanceof Player player) {
            resolved = Placeholders.apply(player, resolved);
        }
        MessageService.send(sender, resolved);
    }

    public void help(CommandSender sender) {
        for (String line : menuConfig.help()) {
            String resolved = sender instanceof Player player ? Placeholders.apply(player, line) : line;
            MessageService.send(sender, resolved);
        }
    }

    public void syncPlayer(UUID playerId, Runnable task) {
        SchedulerUtils.runSync(this, () -> {
            Player player = getServer().getPlayer(playerId);
            if (player == null) {
                task.run();
                return;
            }
            SchedulerUtils.runForPlayer(this, player, task);
        });
    }

    private void warnCurrencies() {
        for (CurrencySpec spec : menuConfig.currencies().values()) {
            if (spec.vault() && getServer().getPluginManager().getPlugin("Vault") == null) {
                getLogger().warning("Currency " + spec.id() + " uses Vault, and Vault is not installed.");
            }
            if (spec.placeholder() && getServer().getPluginManager().getPlugin("PlaceholderAPI") == null) {
                getLogger().warning("Currency " + spec.id() + " uses a placeholder, and PlaceholderAPI is not installed.");
            }
        }
    }
}
