package me.dags.actionreplay;

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

    public Frame next() {
        return next;
    }

    public Frame previous() {
        return previous;
    }

    public void setNext(Frame frame) {
        frame.previous = this;
        this.next = frame;
        this.avatars.stream().map(AvatarSnapshot::getUpdatedCopy).forEach(frame.avatars::add);
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

    public static void restoreAll(Frame input) {
        Frame frame = first(input);
        while (frame != null) {
            frame.getChange().restore();
            frame = frame.next();
        }
    }

    public static void undoAll(Frame input) {
        Frame frame = last(input);
        while (frame != null) {
            frame.getChange().undo();
            frame = frame.previous();
        }
    }
}
