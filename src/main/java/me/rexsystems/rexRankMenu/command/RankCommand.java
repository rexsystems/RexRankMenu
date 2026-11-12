package me.rexsystems.rexRankMenu.command;

import me.rexsystems.rexRankMenu.RexRankMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class RankCommand implements CommandExecutor, TabCompleter {

    private final RexRankMenu plugin;

    public RankCommand(RexRankMenu plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            openSelf(sender);
            return true;
        }
        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "help" -> plugin.help(sender);
            case "reload" -> reload(sender);
            case "open" -> {
                if (args.length == 1) {
                    openSelf(sender);
                } else {
                    openOther(sender, args[1]);
                }
            }
            default -> plugin.help(sender);
        }
        return true;
    }

    private void openSelf(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            plugin.tell(sender, "players-only", Map.of());
            return;
        }
        if (!player.hasPermission("rexrankmenu.use")) {
            plugin.tell(player, "no-permission", Map.of());
            return;
        }
        plugin.rankMenu().open(player);
    }

    private void openOther(CommandSender sender, String name) {
        if (!sender.hasPermission("rexrankmenu.admin")) {
            plugin.tell(sender, "no-permission", Map.of());
            return;
        }
        Player target = plugin.getServer().getPlayerExact(name);
        if (target == null) {
            plugin.tell(sender, "player-not-found", Map.of("player", name));
            return;
        }
        plugin.rankMenu().open(target);
    }

    private void reload(CommandSender sender) {
        if (!sender.hasPermission("rexrankmenu.reload")) {
            plugin.tell(sender, "no-permission", Map.of());
            return;
        }
        plugin.reloadPlugin();
        plugin.tell(sender, "reloaded", Map.of());
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            options.add("open");
            options.add("help");
            if (sender.hasPermission("rexrankmenu.reload")) {
                options.add("reload");
            }
            return partial(options, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("open") && sender.hasPermission("rexrankmenu.admin")) {
            List<String> names = new ArrayList<>();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                names.add(player.getName());
            }
            return partial(names, args[1]);
        }
        return List.of();
    }

    private static List<String> partial(List<String> options, String token) {
        String prefix = token == null ? "" : token.toLowerCase(Locale.ROOT);
        List<String> matched = new ArrayList<>();
        for (String option : options) {
            if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
                matched.add(option);
            }
        }
        return matched;
    }
}
