package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class Animation implements DataSerializable {

    private static final DataQuery FRAMES = DataQuery.of("FRAMES");
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
        if (!isPresent()) {
            throw new UnsupportedOperationException("Cannot play an EMPTY animation");
        }
        if (isPlaying()) {
            throw new UnsupportedOperationException("An animation is already playing");
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
            last = next;
        }

        return new Animation(first, last);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        List<Frame> frames = Animation.toList(this);
        return new MemoryDataContainer()
                .set(FRAMES, frames);
    }

    public static class Builder extends AbstractDataBuilder<Animation> {

        public Builder() {
            super(Animation.class, 0);
        }

        @Override
        public Optional<Animation> buildContent(DataView container) throws InvalidDataException {
            return container.getSerializableList(FRAMES, Frame.class).map(Animation::fromList);
        }
    }
}
