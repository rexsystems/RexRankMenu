package me.rexsystems.rexRankMenu.luck;

import java.util.Set;

public record TrackSnapshot(java.util.List<String> order, Set<String> owned) {
    public TrackSnapshot {
        order = order == null ? java.util.List.of() : java.util.List.copyOf(order);
        owned = owned == null || owned.isEmpty() ? Set.of() : Set.copyOf(owned);
    }
}
