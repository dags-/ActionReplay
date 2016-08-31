package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.Change;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author dags <dags@dags.me>
 */
public class Frame {

    private final Set<AvatarSnapshot> avatars = new HashSet<>();
    private final Change change;
    private Frame previous;
    private Frame next;

    public Frame(AvatarSnapshot avatar, Change change) {
        this.change = change;
        this.avatars.add(avatar);
    }

    private Frame(Mutable mutable) {
        this.avatars.addAll(mutable.avatars);
        this.change = mutable.change;
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

    public void passAvatarsToNext(Vector3d relative) {
        this.avatars.stream().map(avatar -> avatar.getUpdatedCopy(relative)).forEach(next().avatars::add);
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

    public static Mutable mutable() {
        return new Mutable();
    }

    public static class Mutable {

        public Set<AvatarSnapshot> avatars = new HashSet<>();
        public Change change = null;

        public Frame build() {
            if (change == null) {
                throw new UnsupportedOperationException("Change is null!");
            }
            return new Frame(this);
        }
    }
}
