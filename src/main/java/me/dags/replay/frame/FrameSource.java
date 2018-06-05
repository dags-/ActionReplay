package me.dags.replay.frame;

import me.dags.replay.replay.ReplayMeta;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSource extends Closeable {

    ReplayMeta header() throws IOException;

    FrameView next() throws IOException;
}
