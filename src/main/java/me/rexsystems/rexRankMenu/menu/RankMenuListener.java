package me.rexsystems.rexRankMenu.menu;

import me.rexsystems.rexRankMenu.RexRankMenu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Optional;

public final class RankMenuListener implements Listener {

    private final RexRankMenu plugin;

    public RankMenuListener(RexRankMenu plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RankMenuHolder holder)) {
            return;
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked != null && clicked != top) {
            if (event.isShiftClick() || event.getAction() == org.bukkit.event.inventory.InventoryAction.COLLECT_TO_CURSOR) {
                event.setCancelled(true);
            }
            return;
        }
        event.setCancelled(true);
        int slot = event.getSlot();
        if (holder.confirm() && slot == holder.confirmSlot()) {
            String pending = holder.pending();
            if (pending == null || pending.isBlank()) {
                plugin.tell(player, "select-first", Map.of());
                return;
            }
            plugin.purchases().buy(player, pending);
            return;
        }
        String group = holder.groupAt(slot);
        if (group == null) {
            return;
        }
        Optional<String> next = holder.next();
        if (next.isPresent() && next.get().equalsIgnoreCase(group)) {
            if (!holder.confirm()) {
                plugin.purchases().buy(player, group);
                return;
            }
            holder.setPending(group);
            RankMenu.paint(plugin, player, holder);
            plugin.tell(player, "selected", MenuText.fields(plugin.config(), player.getName(), group, group, ""));
            return;
        }
        if (holder.owns(group)) {
            plugin.tell(player, "already-owned", MenuText.fields(plugin.config(), player.getName(), group, next.orElse(""), ""));
            return;
        }
        if (next.isEmpty()) {
            plugin.tell(player, "highest", MenuText.fields(plugin.config(), player.getName(), group, "", ""));
            return;
        }
        plugin.tell(player, "not-next", MenuText.fields(plugin.config(), player.getName(), group, next.get(), ""));
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        Inventory top = event.getView().getTopInventory();
        if (!(top.getHolder() instanceof RankMenuHolder)) {
            return;
        }
        int size = top.getSize();
        for (int raw : event.getRawSlots()) {
            if (raw < size) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
