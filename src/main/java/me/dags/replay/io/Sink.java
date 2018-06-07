package me.dags.replay.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface Sink<T, H> extends Closeable {

    void goToEnd() throws IOException;

    void write(T frame) throws IOException;

    void writeHeader(H header) throws IOException;
}
