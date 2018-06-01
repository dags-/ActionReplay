package me.dags.replay.frame;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSink {

    void accept(Frame frame) throws IOException;
}
