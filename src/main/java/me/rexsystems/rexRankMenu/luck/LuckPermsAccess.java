package me.rexsystems.rexRankMenu.luck;

import me.rexsystems.rexRankMenu.rank.RankTrack;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.context.ImmutableContextSet;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LuckPermsAccess {

    public CompletableFuture<Optional<TrackSnapshot>> load(
            UUID uniqueId,
            String trackName,
            String serverContext,
            List<String> orderOverride
    ) {
        LuckPerms api = LuckPermsProvider.get();
        return api.getTrackManager().loadTrack(trackName).thenCompose(track -> {
            if (track.isEmpty()) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
            return api.getUserManager().loadUser(uniqueId).thenApply(user -> Optional.of(new TrackSnapshot(
                    RankTrack.displayOrder(track.get().getGroups(), orderOverride),
                    owned(user, serverContext)
            )));
        });
    }

    public CompletableFuture<Boolean> grantAndVerify(UUID uniqueId, String group, String serverContext) {
        LuckPerms api = LuckPermsProvider.get();
        InheritanceNode node = parent(group, serverContext);
        // Read the user after the save. A missing group is refunded by the caller.
        return api.getUserManager().modifyUser(uniqueId, user -> user.data().add(node))
                .thenCompose(ignored -> api.getUserManager().loadUser(uniqueId))
                .thenApply(user -> groupPresent(user, group, serverContext));
    }

    static boolean groupPresent(User user, String group, String serverContext) {
        if (user == null || group == null || group.isBlank()) {
            return false;
        }
        for (Group inherited : user.getInheritedGroups(options(serverContext))) {
            if (inherited.getName().equalsIgnoreCase(group)) {
                return true;
            }
        }
        for (Node node : user.getNodes()) {
            if (!node.getValue() || node.hasExpired() || !NodeType.INHERITANCE.matches(node)) {
                continue;
            }
            InheritanceNode inheritance = NodeType.INHERITANCE.cast(node);
            if (!inheritance.getGroupName().equalsIgnoreCase(group)) {
                continue;
            }
            if (serverContext == null || serverContext.isBlank()) {
                if (node.getContexts().isEmpty()) {
                    return true;
                }
            } else if (node.getContexts().contains("server", serverContext)) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> owned(User user, String serverContext) {
        Set<String> names = new HashSet<>();
        for (Group group : user.getInheritedGroups(options(serverContext))) {
            if (group.getName() != null) {
                names.add(group.getName());
            }
        }
        return names;
    }

    private static QueryOptions options(String serverContext) {
        if (serverContext == null || serverContext.isBlank()) {
            return QueryOptions.nonContextual();
        }
        return QueryOptions.contextual(ImmutableContextSet.of("server", serverContext));
    }

    private static InheritanceNode parent(String group, String serverContext) {
        InheritanceNode.Builder builder = InheritanceNode.builder(group);
        if (serverContext != null && !serverContext.isBlank()) {
            builder.withContext("server", serverContext);
        }
        return builder.build();
    }
}
