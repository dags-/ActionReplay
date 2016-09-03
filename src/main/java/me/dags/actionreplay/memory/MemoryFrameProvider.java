package me.dags.actionreplay.memory;

import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.animation.FrameProvider;

/**
 * @author dags <dags@dags.me>
 */
public class MemoryFrameProvider implements FrameProvider {

    private Frame current;

    public MemoryFrameProvider(Frame first) {
        this.current = first;
    }

    @Override
    public Frame nextFrame() throws Exception {
        Frame result = current;
        current = current.next();
        return result;
    }

    @Override
    public boolean hasNext() throws Exception {
        return current != null && current.next() != null;
    }

    @Override
    public void close() throws Exception {}

    @Override
    public void forward() throws Exception {}

    @Override
    public void backward() throws Exception {}
}
