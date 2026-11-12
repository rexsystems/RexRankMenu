package me.rexsystems.rexRankMenu.config;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class MenuConfig {

    private static final List<String> DEFAULT_LORE = List.of("<gray>{price} {currency}", "<gray>{status}");
    private static final List<String> DEFAULT_HELP = List.of(
            "<gray>RexRankMenu",
            "<white>/rexrankmenu <gray>- open the menu",
            "<white>/rexrankmenu open [player]",
            "<white>/rexrankmenu reload",
            "<white>/rexrankmenu help"
    );
    private static final Map<String, String> DEFAULT_MESSAGES = Map.ofEntries(
            Map.entry("no-permission", "<red>You do not have permission."),
            Map.entry("players-only", "<red>Only a player can open this menu."),
            Map.entry("player-not-found", "<red>{player} is not online."),
            Map.entry("reloaded", "<green>RexRankMenu reloaded."),
            Map.entry("track-missing", "<red>LuckPerms track <white>{track} <red>does not exist."),
            Map.entry("track-empty", "<red>LuckPerms track <white>{track} <red>has no groups."),
            Map.entry("luckperms-failed", "<red>LuckPerms did not return your groups. Nothing was charged."),
            Map.entry("already-owned", "<yellow>You already have <white>{rank}<yellow>."),
            Map.entry("not-next", "<red>The next rank is <white>{next}<red>."),
            Map.entry("not-enough", "<red>You need <white>{price} {currency}<red>."),
            Map.entry("purchased", "<green>You bought <white>{rank}<green>."),
            Map.entry("failed", "<red>{rank} was not applied. Your payment was returned."),
            Map.entry("highest", "<gray>You already have the last rank on <white>{track}<gray>."),
            Map.entry("no-buy", "<red>You cannot buy ranks."),
            Map.entry("currency-missing", "<red>Currency <white>{currency} <red>is not available. Nothing was charged."),
            Map.entry("busy", "<gray>A purchase is already in progress."),
            Map.entry("selected", "<yellow>Click confirm to buy <white>{rank} <yellow>for <white>{price} {currency}<yellow>."),
            Map.entry("select-first", "<gray>Select the next rank first."),
            Map.entry("status-owned", "Owned"),
            Map.entry("status-next", "Available"),
            Map.entry("status-locked", "Locked")
    );

    private final String track;
    private final boolean confirm;
    private final String serverContext;
    private final String title;
    private final int rows;
    private final int confirmSlot;
    private final Material filler;
    private final Material defaultMaterial;
    private final String confirmMaterial;
    private final String confirmName;
    private final List<String> confirmLore;
    private final String idleMaterial;
    private final String idleName;
    private final List<String> idleLore;
    private final List<String> order;
    private final Map<String, RankDress> dresses;
    private final Map<String, CurrencySpec> currencies;
    private final String defaultCurrency;
    private final Map<String, String> messages;
    private final List<String> help;

    private MenuConfig(
            String track,
            boolean confirm,
            String serverContext,
            String title,
            int rows,
            int confirmSlot,
            Material filler,
            Material defaultMaterial,
            String confirmMaterial,
            String confirmName,
            List<String> confirmLore,
            String idleMaterial,
            String idleName,
            List<String> idleLore,
            List<String> order,
            Map<String, RankDress> dresses,
            Map<String, CurrencySpec> currencies,
            String defaultCurrency,
            Map<String, String> messages,
            List<String> help
    ) {
        this.track = track;
        this.confirm = confirm;
        this.serverContext = serverContext;
        this.title = title;
        this.rows = rows;
        this.confirmSlot = confirmSlot;
        this.filler = filler;
        this.defaultMaterial = defaultMaterial;
        this.confirmMaterial = confirmMaterial;
        this.confirmName = confirmName;
        this.confirmLore = confirmLore;
        this.idleMaterial = idleMaterial;
        this.idleName = idleName;
        this.idleLore = idleLore;
        this.order = order;
        this.dresses = dresses;
        this.currencies = currencies;
        this.defaultCurrency = defaultCurrency;
        this.messages = messages;
        this.help = help;
    }

    public static MenuConfig load(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        File ranksFile = new File(plugin.getDataFolder(), "ranks.yml");
        if (!ranksFile.exists()) {
            plugin.saveResource("ranks.yml", false);
        }
        YamlConfiguration ranks = YamlConfiguration.loadConfiguration(ranksFile);
        return parse(plugin.getConfig(), ranks);
    }

    static MenuConfig parse(FileConfiguration config, YamlConfiguration ranks) {
        String track = text(config.getString("settings.track"), "ranks");
        boolean confirm = !config.contains("settings.confirm") || config.getBoolean("settings.confirm");
        String serverContext = text(config.getString("settings.server-context"), "");
        int rows = config.getInt("settings.gui.rows", 6);
        if (rows < 1) {
            rows = 1;
        } else if (rows > 6) {
            rows = 6;
        }
        int size = rows * 9;
        int confirmSlot = config.getInt("settings.gui.confirm-slot", Math.min(49, size - 1));
        if (confirmSlot < 0 || confirmSlot >= size) {
            confirmSlot = size - 1;
        }
        Material defaultMaterial = itemMaterial(config.getString("settings.default-material"), Material.PAPER);
        Material filler = filler(config.getString("settings.gui.filler", "GRAY_STAINED_GLASS_PANE"));
        Map<String, CurrencySpec> currencies = currencies(config);
        String defaultCurrency = currencies.containsKey("vault")
                ? "vault"
                : currencies.keySet().stream().findFirst().orElse("vault");
        Map<String, String> messages = new HashMap<>(DEFAULT_MESSAGES);
        ConfigurationSection messageSection = config.getConfigurationSection("messages");
        if (messageSection != null) {
            for (String key : messageSection.getKeys(false)) {
                messages.put(key, messageSection.getString(key, ""));
            }
        }
        List<String> help = config.getStringList("help");
        if (help.isEmpty()) {
            help = DEFAULT_HELP;
        }
        return new MenuConfig(
                track,
                confirm,
                serverContext,
                text(config.getString("settings.gui.title"), "<dark_gray>Ranks"),
                rows,
                confirmSlot,
                filler,
                defaultMaterial,
                text(config.getString("settings.gui.confirm-material"), "LIME_DYE"),
                text(config.getString("settings.gui.confirm-name"), "<green>Confirm {rank}"),
                lines(config, "settings.gui.confirm-lore", List.of("<gray>Price: <white>{price} {currency}", "<yellow>Click to buy")),
                text(config.getString("settings.gui.idle-material"), "GRAY_DYE"),
                text(config.getString("settings.gui.idle-name"), "<gray>Confirm"),
                lines(config, "settings.gui.idle-lore", List.of("<gray>Click the next rank, then click here.")),
                List.copyOf(ranks.getStringList("order")),
                dresses(ranks, defaultCurrency),
                currencies,
                defaultCurrency,
                Map.copyOf(messages),
                List.copyOf(help)
        );
    }

    private static Map<String, CurrencySpec> currencies(FileConfiguration config) {
        Map<String, CurrencySpec> map = new LinkedHashMap<>();
        ConfigurationSection section = config.getConfigurationSection("currencies");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection currency = section.getConfigurationSection(id);
                if (currency == null) {
                    continue;
                }
                map.put(id.toLowerCase(Locale.ROOT), new CurrencySpec(
                        id,
                        currency.getString("type", "vault"),
                        currency.getString("balance-placeholder", ""),
                        currency.getString("give-command", ""),
                        currency.getString("take-command", "")
                ));
            }
        }
        if (map.isEmpty()) {
            map.put("vault", new CurrencySpec("vault", "vault", "", "", ""));
        }
        return Map.copyOf(map);
    }

    private static Map<String, RankDress> dresses(YamlConfiguration ranks, String defaultCurrency) {
        Map<String, RankDress> map = new LinkedHashMap<>();
        ConfigurationSection section = ranks.getConfigurationSection("ranks");
        if (section == null) {
            return Map.of();
        }
        for (String group : section.getKeys(false)) {
            ConfigurationSection rank = section.getConfigurationSection(group);
            if (rank == null) {
                continue;
            }
            List<String> lore = rank.contains("lore") ? rank.getStringList("lore") : DEFAULT_LORE;
            map.put(group.toLowerCase(Locale.ROOT), new RankDress(
                    rank.getDouble("price", 0d),
                    text(rank.getString("currency"), defaultCurrency),
                    rank.getString("name", ""),
                    lore,
                    rank.contains("slot") ? rank.getInt("slot") : null,
                    rank.getString("material", ""),
                    rank.contains("custom-model-data") ? rank.getInt("custom-model-data") : null
            ));
        }
        return Map.copyOf(map);
    }

    private static List<String> lines(FileConfiguration config, String path, List<String> fallback) {
        if (!config.contains(path)) {
            return fallback;
        }
        return new ArrayList<>(config.getStringList(path));
    }

    private static Material filler(String name) {
        if (name == null || name.isBlank() || name.equalsIgnoreCase("none") || name.equalsIgnoreCase("air")) {
            return null;
        }
        Material material = Material.matchMaterial(name.trim());
        if (material == null || material.isAir() || !material.isItem()) {
            return null;
        }
        return material;
    }

    private static Material itemMaterial(String name, Material fallback) {
        Material material = name == null ? null : Material.matchMaterial(name.trim());
        if (material == null || material.isAir() || !material.isItem()) {
            return fallback;
        }
        return material;
    }

    private static String text(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    public String track() {
        return track;
    }

    public boolean confirm() {
        return confirm;
    }

    public String serverContext() {
        return serverContext;
    }

    public String title() {
        return title;
    }

    public int rows() {
        return rows;
    }

    public int size() {
        return rows * 9;
    }

    public int confirmSlot() {
        return confirmSlot;
    }

    public Material filler() {
        return filler;
    }

    public Material defaultMaterial() {
        return defaultMaterial;
    }

    public String confirmMaterial() {
        return confirmMaterial;
    }

    public String confirmName() {
        return confirmName;
    }

    public List<String> confirmLore() {
        return confirmLore;
    }

    public String idleMaterial() {
        return idleMaterial;
    }

    public String idleName() {
        return idleName;
    }

    public List<String> idleLore() {
        return idleLore;
    }

    public List<String> order() {
        return order;
    }

    public String defaultCurrency() {
        return defaultCurrency;
    }

    public RankDress dressFor(String group) {
        if (group == null) {
            return RankDress.unnamed(defaultCurrency);
        }
        RankDress dress = dresses.get(group.toLowerCase(Locale.ROOT));
        return dress == null ? RankDress.unnamed(defaultCurrency) : dress;
    }

    public CurrencySpec currency(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return currencies.get(id.toLowerCase(Locale.ROOT));
    }

    public Map<String, CurrencySpec> currencies() {
        return currencies;
    }

    public String message(String key) {
        String value = messages.get(key);
        return value == null ? "" : value;
    }

    public List<String> help() {
        return help;
    }
}
