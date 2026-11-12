package me.rexsystems.rexRankMenu.menu;

import me.rexsystems.rexRankMenu.RexRankMenu;
import me.rexsystems.rexRankMenu.config.MenuConfig;
import me.rexsystems.rexRankMenu.config.RankDress;
import me.rexsystems.rexRankMenu.luck.TrackSnapshot;
import me.rexsystems.rexRankMenu.rank.RankTrack;
import me.rexsystems.rexRankMenu.util.TextParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public final class RankMenu {

    private final RexRankMenu plugin;

    public RankMenu(RexRankMenu plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        MenuConfig config = plugin.config();
        plugin.luckPerms().load(player.getUniqueId(), config.track(), config.serverContext(), config.order())
                .whenComplete((snapshot, error) -> plugin.syncPlayer(player.getUniqueId(), () -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    if (error != null) {
                        plugin.getLogger().log(Level.WARNING, "Could not open ranks for " + player.getName(), error);
                        plugin.tell(player, "luckperms-failed", Map.of("track", config.track()));
                        return;
                    }
                    if (snapshot == null || snapshot.isEmpty()) {
                        plugin.tell(player, "track-missing", Map.of("track", config.track()));
                        return;
                    }
                    TrackSnapshot loaded = snapshot.get();
                    if (loaded.order().isEmpty()) {
                        plugin.tell(player, "track-empty", Map.of("track", config.track()));
                        return;
                    }
                    show(player, loaded);
                }));
    }

    public void refresh(Player player) {
        if (player == null || !player.isOnline()) {
            return;
        }
        Inventory top = player.getOpenInventory().getTopInventory();
        if (!(top.getHolder() instanceof RankMenuHolder holder) || !holder.playerId().equals(player.getUniqueId())) {
            return;
        }
        MenuConfig config = plugin.config();
        plugin.luckPerms().load(player.getUniqueId(), config.track(), config.serverContext(), config.order())
                .whenComplete((snapshot, error) -> plugin.syncPlayer(player.getUniqueId(), () -> {
                    if (!player.isOnline() || error != null || snapshot == null || snapshot.isEmpty()) {
                        return;
                    }
                    Inventory current = player.getOpenInventory().getTopInventory();
                    if (current.getHolder() != holder) {
                        return;
                    }
                    holder.show(snapshot.get(), slots(config, snapshot.get().order()));
                    paint(plugin, player, holder);
                }));
    }

    private void show(Player player, TrackSnapshot snapshot) {
        MenuConfig config = plugin.config();
        Map<Integer, String> slots = slots(config, snapshot.order());
        if (slots.size() < snapshot.order().size()) {
            plugin.getLogger().warning("The rank menu does not have a free slot for every group on " + config.track() + ".");
        }
        RankMenuHolder holder = new RankMenuHolder(player.getUniqueId(), config.confirm(), config.confirmSlot());
        Component title = TextParser.parse(MenuText.resolve(player, config.title(), Map.of(
                "track", config.track(),
                "player", player.getName()
        )));
        Inventory inventory = Bukkit.createInventory(holder, config.size(), title);
        holder.attach(inventory);
        holder.show(snapshot, slots);
        paint(plugin, player, holder);
        player.openInventory(inventory);
    }

    public static void paint(RexRankMenu plugin, Player player, RankMenuHolder holder) {
        MenuConfig config = plugin.config();
        Inventory inventory = holder.getInventory();
        inventory.clear();
        Material filler = config.filler();
        if (filler != null) {
            ItemStack pane = fillerItem(filler);
            for (int slot = 0; slot < inventory.getSize(); slot++) {
                inventory.setItem(slot, pane.clone());
            }
        }
        Optional<String> next = holder.next();
        for (Map.Entry<Integer, String> entry : holder.slots().entrySet()) {
            inventory.setItem(entry.getKey(), rankItem(plugin, player, config, entry.getValue(), next, holder.owned()));
        }
        if (holder.confirm()) {
            inventory.setItem(holder.confirmSlot(), confirmItem(plugin, player, config, holder.pending()));
        }
    }

    static Map<Integer, String> slots(MenuConfig config, List<String> groups) {
        int size = config.size();
        boolean[] used = new boolean[size];
        if (config.confirm() && config.confirmSlot() >= 0 && config.confirmSlot() < size) {
            used[config.confirmSlot()] = true;
        }
        Map<Integer, String> assigned = new LinkedHashMap<>();
        List<String> later = new ArrayList<>();
        for (String group : groups) {
            Integer requested = config.dressFor(group).slot();
            if (requested != null && requested >= 0 && requested < size && !used[requested]) {
                used[requested] = true;
                assigned.put(requested, group);
            } else {
                later.add(group);
            }
        }
        int cursor = 0;
        for (String group : later) {
            while (cursor < size && used[cursor]) {
                cursor++;
            }
            if (cursor >= size) {
                break;
            }
            used[cursor] = true;
            assigned.put(cursor, group);
            cursor++;
        }
        return Map.copyOf(assigned);
    }

    private static ItemStack rankItem(
            RexRankMenu plugin,
            Player player,
            MenuConfig config,
            String group,
            Optional<String> next,
            java.util.Set<String> owned
    ) {
        RankDress dress = config.dressFor(group);
        String statusKey = "status-locked";
        if (RankTrack.hasGroup(owned, group)) {
            statusKey = "status-owned";
        } else if (next.isPresent() && next.get().equalsIgnoreCase(group)) {
            statusKey = "status-next";
        }
        Map<String, String> fields = MenuText.fields(
                config,
                player.getName(),
                group,
                next.orElse(""),
                config.message(statusKey)
        );
        String nameTemplate = dress.name().isBlank() ? "<white>{group}" : dress.name();
        List<Component> lore = new ArrayList<>();
        for (String line : dress.lore()) {
            lore.add(line(MenuText.resolve(player, line, fields)));
        }
        return item(
                material(dress.material(), config.defaultMaterial()),
                line(MenuText.resolve(player, nameTemplate, fields)),
                lore,
                dress.customModelData(),
                false
        );
    }

    private static ItemStack confirmItem(RexRankMenu plugin, Player player, MenuConfig config, String pending) {
        boolean selected = pending != null && !pending.isBlank();
        Map<String, String> fields = selected
                ? MenuText.fields(config, player.getName(), pending, pending, config.message("status-next"))
                : new HashMap<>();
        if (!selected) {
            fields.put("player", player.getName());
            fields.put("track", config.track());
            fields.put("rank", "");
            fields.put("group", "");
            fields.put("price", "");
            fields.put("amount", "");
            fields.put("currency", "");
            fields.put("next", "");
            fields.put("status", "");
        }
        String name = selected ? config.confirmName() : config.idleName();
        List<String> loreLines = selected ? config.confirmLore() : config.idleLore();
        String materialName = selected ? config.confirmMaterial() : config.idleMaterial();
        List<Component> lore = new ArrayList<>();
        for (String line : loreLines) {
            lore.add(line(MenuText.resolve(player, line, fields)));
        }
        return item(material(materialName, Material.LIME_DYE), line(MenuText.resolve(player, name, fields)), lore, null, false);
    }

    private static ItemStack fillerItem(Material material) {
        return item(material, Component.empty(), List.of(), null, true);
    }

    private static ItemStack item(Material material, Component name, List<Component> lore, Integer model, boolean hideTooltip) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(name);
        if (!lore.isEmpty()) {
            meta.lore(lore);
        }
        if (model != null) {
            meta.setCustomModelData(model);
        }
        if (hideTooltip) {
            meta.setHideTooltip(true);
        }
        stack.setItemMeta(meta);
        return stack;
    }

    private static Component line(String text) {
        return TextParser.parse(text).decoration(TextDecoration.ITALIC, false);
    }

    private static Material material(String name, Material fallback) {
        if (name == null || name.isBlank()) {
            return fallback;
        }
        Material matched = Material.matchMaterial(name.trim());
        if (matched == null || matched.isAir() || !matched.isItem()) {
            return fallback;
        }
        return matched;
    }
}
