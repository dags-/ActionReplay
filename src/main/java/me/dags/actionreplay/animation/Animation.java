package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Animation {

    public static final Animation EMPTY = new Animation();

    private final Frame first;
    private final Frame last;
    private Vector3i center = Vector3i.ZERO;
    private AnimationTask playback = AnimationTask.EMPTY;

    private Animation() {
        this.first = null;
        this.last = null;
    }

    public Animation(Frame first, Frame last) {
        this.first = first;
        this.last = last;
    }

    public Animation setCenter(Vector3i center) {
        this.center = center;
        return this;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean isPlaying() {
        return playback.isPresent() && !playback.isInterrupted();
    }

    public boolean play(Object plugin, int intervalTicks, boolean showAvatars) {
        if (isPresent()) {
            throw new UnsupportedOperationException("Cannot play an EMPTY animation");
        }
        if (playback.isPresent()) {
            return false;
        }
        Frame.undoAll(last, center);
        playback = new AnimationTask(center, first, intervalTicks, showAvatars);
        playback.start(plugin);
        return true;
    }

    public boolean stop() {
        if (!isPresent()) {
            throw new UnsupportedOperationException("Cannot stop an EMPTY animation");
        }
        if (!playback.isPresent()) {
            return false;
        }
        playback.stop();
        playback = AnimationTask.EMPTY;
        Frame.undoAll(last, center);
        Frame.restoreAll(first, center);
        return true;
    }

    public static List<Frame> toList(Animation animation) {
        if (!animation.isPresent()) {
            return Collections.emptyList();
        }

        List<Frame> frames = new ArrayList<>();
        Frame frame = animation.first;
        while (frame != null) {
            frames.add(frame);
            frame = frame.next();
        }

        return frames;
    }

    public static Animation fromList(List<Frame> frames) {
        Iterator<Frame> iterator = frames.iterator();
        if (!iterator.hasNext()) {
            return Animation.EMPTY;
        }

        Frame first = frames.iterator().next();
        Frame last = first;

        while (iterator.hasNext()) {
            Frame next = iterator.next();
            last.setNext(next);
            last.passAvatarsToNext(Vector3d.ZERO);
            last = next;
        }

        return new Animation(first, last);
    }
}
