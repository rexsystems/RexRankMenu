package me.rexsystems.rexRankMenu.menu;

import me.rexsystems.rexRankMenu.luck.TrackSnapshot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class RankMenuHolder implements InventoryHolder {

    private Inventory inventory;
    private final UUID playerId;
    private final boolean confirm;
    private final int confirmSlot;
    private List<String> order = List.of();
    private Set<String> owned = Set.of();
    private Map<Integer, String> slots = Map.of();
    private String pending;

    public RankMenuHolder(UUID playerId, boolean confirm, int confirmSlot) {
        this.playerId = playerId;
        this.confirm = confirm;
        this.confirmSlot = confirmSlot;
    }

    public void attach(Inventory inventory) {
        this.inventory = inventory;
    }

    public void show(TrackSnapshot snapshot, Map<Integer, String> slots) {
        this.order = snapshot.order();
        this.owned = snapshot.owned();
        this.slots = slots;
        this.pending = null;
    }

    public void show(List<String> order, Set<String> owned, Map<Integer, String> slots) {
        this.order = List.copyOf(order);
        this.owned = Set.copyOf(owned);
        this.slots = Map.copyOf(slots);
        this.pending = null;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public UUID playerId() {
        return playerId;
    }

    public boolean confirm() {
        return confirm;
    }

    public int confirmSlot() {
        return confirmSlot;
    }

    public List<String> order() {
        return order;
    }

    public Set<String> owned() {
        return owned;
    }

    public Map<Integer, String> slots() {
        return slots;
    }

    public String groupAt(int slot) {
        return slots.get(slot);
    }

    public Optional<String> next() {
        return me.rexsystems.rexRankMenu.rank.RankTrack.nextRank(order, owned);
    }

    public boolean owns(String group) {
        return me.rexsystems.rexRankMenu.rank.RankTrack.hasGroup(owned, group);
    }

    public String pending() {
        return pending;
    }

    public void setPending(String pending) {
        this.pending = pending;
    }
}
