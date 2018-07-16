package me.dags.replay.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface Source<T> extends Closeable {

    T header() throws IOException;

    T next() throws IOException;

    T first() throws IOException;

    T last() throws IOException;
}
