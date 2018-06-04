package me.dags.replay.frame;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSink {

    void accept(FrameView frame) throws IOException;
}
