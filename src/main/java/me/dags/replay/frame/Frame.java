package me.dags.replay.frame;

import java.util.Collections;
import java.util.List;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;
import me.dags.replay.frame.avatar.Avatar;
import me.dags.replay.frame.avatar.AvatarSnapshot;
import me.dags.replay.frame.block.BlockChange;
import me.dags.replay.replay.ReplayContext;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Frame implements OptionalValue {

    private final List<BlockChange> changes;
    private final List<AvatarSnapshot> avatars;

    public Frame(BlockChange change, List<AvatarSnapshot> avatars) {
        this(Collections.singletonList(change), avatars);
    }

    public Frame(List<BlockChange> changes, List<AvatarSnapshot> avatars) {
        this.changes = changes;
        this.avatars = avatars;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    public void apply(Location<World> origin, ReplayContext context) {
        context.push();

        for (BlockChange change : changes) {
            change.apply(origin);
        }

        for (AvatarSnapshot snapshot : avatars) {
            Avatar avatar = context.getAvatar(snapshot.getUUID());
            if (!avatar.isPresent()) {
                avatar = snapshot.create(origin);
                if (!avatar.isPresent()) {
                    continue;
                }
                context.setAvatar(snapshot.getUUID(), avatar);
            }
            snapshot.applyTo(origin, avatar);
        }

        context.pop();
    }

    public static final Serializer<Frame> SERIALIZER = new Serializer<Frame>() {
        @Override
        public void serialize(Frame frame, Node node) {
            node.put("changes", frame.changes, BlockChange.SERIALIZER);
            node.put("avatars", frame.avatars, AvatarSnapshot.SERIALIZER);
        }

        @Override
        public Frame deserialize(Node node) {
            List<BlockChange> changes = node.getList("changes", BlockChange.SERIALIZER);
            List<AvatarSnapshot> avatars = node.getList("avatars", AvatarSnapshot.SERIALIZER);
            return new Frame(changes, avatars);
        }
    };

    public static final Frame NONE = new Frame(Collections.emptyList(), Collections.emptyList()) {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void apply(Location<World> origin, ReplayContext context) {

        }
    };
}
