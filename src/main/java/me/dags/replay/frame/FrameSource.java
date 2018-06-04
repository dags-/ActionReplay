package me.dags.replay.frame;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSource {

    FrameView next() throws IOException;

    FrameView first() throws IOException;

    FrameView last() throws IOException;

    FrameSource reset() throws IOException;

    void close() throws IOException;
}
