package me.dags.replay.frame;

import java.io.Closeable;
import java.io.IOException;
import me.dags.replay.replay.ReplayMeta;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSource extends Closeable {

    ReplayMeta header() throws IOException;

    FrameView next() throws IOException;
}
