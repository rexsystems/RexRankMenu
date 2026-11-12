package me.rexsystems.rexRankMenu.rank;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RankTrackTest {

    private static final List<String> TRACK = List.of("default", "vip", "mvp", "elite");

    @Test
    void nextRankReturnsTheFirstMissingGroup() {
        assertEquals(Optional.of("default"), RankTrack.nextRank(TRACK, Set.of()));
        assertEquals(Optional.of("vip"), RankTrack.nextRank(TRACK, Set.of("default")));
        assertEquals(Optional.of("mvp"), RankTrack.nextRank(TRACK, Set.of("default", "VIP")));
        assertEquals(Optional.of("elite"), RankTrack.nextRank(TRACK, Set.of("default", "vip", "mvp")));
    }

    @Test
    void nextRankIsEmptyWhenThePlayerHasTheLastGroup() {
        assertEquals(Optional.empty(), RankTrack.nextRank(TRACK, Set.of("default", "vip", "mvp", "elite")));
        assertEquals(Optional.empty(), RankTrack.nextRank(TRACK, Set.of("elite")));
        assertEquals(Optional.empty(), RankTrack.nextRank(TRACK, Set.of("Elite")));
    }

    @Test
    void refundsWhenTheVerifyFlagIsFalse() {
        assertTrue(RankTrack.shouldRefund(false));
        assertFalse(RankTrack.shouldRefund(true));
    }

    @Test
    void orderOverrideKeepsTrackGroupsThatWereNotListed() {
        assertEquals(List.of("mvp", "default", "vip"), RankTrack.displayOrder(
                List.of("default", "vip", "mvp"),
                List.of("mvp", "missing", "default")
        ));
        assertEquals(List.of("default", "vip"), RankTrack.displayOrder(List.of("default", "vip"), List.of()));
    }
}
