package me.dags.replay.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface Sink<T> extends Closeable {

    void goToEnd() throws IOException;

    void write(T frame) throws IOException;

    void writeHeader(T header) throws IOException;
}
