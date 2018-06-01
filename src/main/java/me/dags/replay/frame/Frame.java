package me.dags.replay.frame;

import com.sk89q.jnbt.CompoundTag;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import me.dags.replay.avatar.Avatar;
import me.dags.replay.avatar.AvatarSnapshot;
import me.dags.replay.block.BlockChange;
import me.dags.replay.replay.ReplayContext;
import me.dags.replay.util.CompoundBuilder;
import me.dags.replay.util.OptionalValue;
import me.dags.replay.serialize.Serializer;
import me.dags.replay.serialize.Serializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Frame implements OptionalValue {

    public static final Frame NONE = new Frame(Collections.emptyList(), Collections.emptyList()) {
        @Override
        public boolean isPresent() {
            return false;
        }
    };

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

    public List<BlockChange> getChanges() {
        return changes;
    }

    public List<AvatarSnapshot> getAvatars() {
        return avatars;
    }

    public void apply(Location<World> origin, ReplayContext context) {
        context.push();

        for (BlockChange change : getChanges()) {
            change.apply(origin);
        }

        for (AvatarSnapshot snapshot : getAvatars()) {
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
        public void serialize(Frame frame, CompoundBuilder builder) {
            Serializers.list(builder, "changes", frame.getChanges(), BlockChange.SERIALIZER);
            Serializers.list(builder, "avatars", frame.getAvatars(), AvatarSnapshot.SERIALIZER);
        }

        @Override
        public Frame deserialize(CompoundTag tag) {
            List<BlockChange> changes = new ArrayList<>();
            List<AvatarSnapshot> avatars = new ArrayList<>();
            Serializers.list(tag.getList("changes", CompoundTag.class), changes, BlockChange.SERIALIZER);
            Serializers.list(tag.getList("avatars", CompoundTag.class), avatars, AvatarSnapshot.SERIALIZER);
            return new Frame(changes, avatars);
        }
    };
}
