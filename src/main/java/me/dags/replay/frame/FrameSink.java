package me.dags.replay.frame;

import me.dags.replay.data.Node;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSink extends Closeable {

    void goToEnd() throws IOException;

    void write(Node frame) throws IOException;

    void writeHeader(Node header) throws IOException;
}
