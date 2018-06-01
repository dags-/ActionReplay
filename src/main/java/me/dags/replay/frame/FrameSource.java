package me.dags.replay.frame;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSource {

    Frame next() throws IOException;

    Frame first() throws IOException;

    Frame last() throws IOException;

    FrameSource reset() throws IOException;
}
