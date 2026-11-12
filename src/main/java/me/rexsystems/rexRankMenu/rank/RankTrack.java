package me.rexsystems.rexRankMenu.rank;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class RankTrack {

    private RankTrack() {
    }

    public static Optional<String> nextRank(List<String> track, Set<String> owned) {
        List<String> groups = clean(track);
        if (groups.isEmpty()) {
            return Optional.empty();
        }
        // Owning the last group on the ladder ends it, including when a lower group is missing.
        if (hasGroup(owned, groups.get(groups.size() - 1))) {
            return Optional.empty();
        }
        for (String group : groups) {
            if (!hasGroup(owned, group)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }

    public static boolean shouldRefund(boolean verified) {
        return !verified;
    }

    public static boolean hasGroup(Set<String> owned, String group) {
        if (owned == null || group == null || group.isBlank()) {
            return false;
        }
        for (String name : owned) {
            if (name != null && name.equalsIgnoreCase(group)) {
                return true;
            }
        }
        return false;
    }

    public static List<String> displayOrder(List<String> track, List<String> override) {
        List<String> source = clean(track);
        if (override == null || override.isEmpty()) {
            return source;
        }
        Map<String, String> canonical = new LinkedHashMap<>();
        for (String group : source) {
            canonical.putIfAbsent(group.toLowerCase(Locale.ROOT), group);
        }
        List<String> ordered = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String entry : override) {
            if (entry == null || entry.isBlank()) {
                continue;
            }
            String key = entry.toLowerCase(Locale.ROOT);
            String name = canonical.get(key);
            if (name != null && seen.add(key)) {
                ordered.add(name);
            }
        }
        for (String group : source) {
            if (seen.add(group.toLowerCase(Locale.ROOT))) {
                ordered.add(group);
            }
        }
        return List.copyOf(ordered);
    }

    private static List<String> clean(List<String> track) {
        if (track == null || track.isEmpty()) {
            return List.of();
        }
        List<String> groups = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String group : track) {
            if (group == null || group.isBlank()) {
                continue;
            }
            if (seen.add(group.toLowerCase(Locale.ROOT))) {
                groups.add(group);
            }
        }
        return List.copyOf(groups);
    }
}
