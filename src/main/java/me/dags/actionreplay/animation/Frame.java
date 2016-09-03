package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.Change;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Frame implements DataSerializable {

    private static final DataQuery AVATARS = DataQuery.of("AVATARS");
    private static final DataQuery CHANGE = DataQuery.of("CHANGE");

    private final Set<AvatarSnapshot> avatars = new HashSet<>();
    private final Change change;
    private Frame previous;
    private Frame next;

    public Frame(AvatarSnapshot avatar, Change change) {
        this.change = change;
        this.avatars.add(avatar);
    }

    private Frame(Collection<AvatarSnapshot> avatars, Change change) {
        this.avatars.addAll(avatars);
        this.change = change;
    }

    public Frame next() {
        return next;
    }

    public Frame previous() {
        return previous;
    }

    public void setNext(Frame frame) {
        frame.previous = this;
        this.next = frame;
    }

    public void setNextAndUpdate(Frame next, Vector3d relative) {
        setNext(next);
        next.updateFromPrevious(this, relative);
    }

    public void updateFromPrevious(Frame previous, Vector3d relative) {
        previous.avatars.stream().map(avatar -> avatar.getUpdatedCopy(relative)).forEach(this.avatars::add);
    }

    public Collection<AvatarSnapshot> getAvatars() {
        return avatars;
    }

    public Change getChange() {
        return change;
    }

    public static Frame first(Frame frame) {
        if (frame == null) {
            throw new UnsupportedOperationException("Input Frame cannot be null!");
        }
        while (frame.previous() != null) {
            frame = frame.previous();
        }
        return frame;
    }

    public static Frame last(Frame frame) {
        if (frame == null) {
            throw new UnsupportedOperationException("Input Frame cannot be null!");
        }
        while (frame.next() != null) {
            frame = frame.next();
        }
        return frame;
    }

    public static void restoreAll(Frame input, Vector3i relative) {
        Frame frame = first(input);
        while (frame != null) {
            frame.getChange().restore(relative);
            frame = frame.next();
        }
    }

    public static void undoAll(Frame input, Vector3i relative) {
        Frame frame = last(input);
        while (frame != null) {
            frame.getChange().undo(relative);
            frame = frame.previous();
        }
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
