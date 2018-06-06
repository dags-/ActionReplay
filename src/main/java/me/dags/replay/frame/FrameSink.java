package me.dags.replay.frame;

import java.io.Closeable;
import java.io.IOException;
import me.dags.replay.data.Node;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSink extends Closeable {

    void skipHeader() throws IOException;

    void write(Node frame) throws IOException;

    void writeHeader(Node header) throws IOException;
}
