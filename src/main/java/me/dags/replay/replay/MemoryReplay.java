package me.dags.replay.replay;

import java.util.ArrayList;
import java.util.List;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.frame.FrameSource;

/**
 * @author dags <dags@dags.me>
 */
public class MemoryReplay implements FrameSource, FrameSink {

    private final List<Frame> frames = new ArrayList<>();
    private int position = -1;
    private int direction = 1;

    @Override
    public Frame next() {
        int next = position + direction;
        if (next > -1 && next < frames.size()) {
            position = next;
            return frames.get(position);
        }
        return Frame.NONE;
    }

    @Override
    public Frame first() {
        if (frames.size() > 0) {
            return frames.get(0);
        }
        return Frame.NONE;
    }

    @Override
    public Frame last() {
        if (frames.size() > 0) {
            return frames.get(frames.size() - 1);
        }
        return Frame.NONE;
    }

    @Override
    public FrameSource reset() {
        position = direction == 1 ? -1 : frames.size();
        return this;
    }

    @Override
    public void accept(Frame frame) {
        frames.add(frame);
    }
}
