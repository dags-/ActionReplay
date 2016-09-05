package me.dags.actionreplay.replay.frame;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.collect.ImmutableSet;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.replay.avatar.AvatarSnapshot;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class Frame implements DataSerializable {

    private static final DataQuery AVATARS = DataQuery.of("AVATARS");
    private static final DataQuery CHANGE = DataQuery.of("CHANGE");

    private final ImmutableSet<AvatarSnapshot> avatars;
    private final Change change;

    public Frame(AvatarSnapshot avatar, Change change, Collection<AvatarSnapshot> avatars) {
        this.change = change;
        this.avatars = ImmutableSet.<AvatarSnapshot>builder().add(avatar).addAll(avatars).build();
    }

    public Frame(AvatarSnapshot avatar, Change change) {
        this.change = change;
        this.avatars = ImmutableSet.of(avatar);
    }

    private Frame(Collection<AvatarSnapshot> avatars, Change change) {
        this.avatars = ImmutableSet.copyOf(avatars);
        this.change = change;
    }

    public Collection<AvatarSnapshot> getRelativeAvatars(Vector3d relative) {
        return avatars.stream().map(a -> a.getUpdatedCopy(relative)).collect(Collectors.toList());
    }

    public Collection<AvatarSnapshot> getAvatars() {
        return avatars;
    }

    public Change getChange() {
        return change;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(AVATARS, avatars)
                .set(CHANGE, change);
    }

    public static class Builder extends AbstractDataBuilder<Frame> {

        public Builder() {
            super(Frame.class, 0);
        }

        public Frame fastBuild(DataView container) throws InvalidDataException {
            Optional<List<AvatarSnapshot>> avatars = container.getSerializableList(AVATARS, AvatarSnapshot.class);
            Optional<Change> change = container.getSerializable(CHANGE, Change.class);
            if (avatars.isPresent() && change.isPresent()) {
                return new Frame(avatars.get(), change.get());
            }
            return null;
        }

        @Override
        public Optional<Frame> buildContent(DataView container) throws InvalidDataException {
            return Optional.ofNullable(fastBuild(container));
        }
    }
}
