package me.dags.replay.io;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface Source<T, H> extends Closeable {

    H header() throws IOException;

    T next() throws IOException;

    T first() throws IOException;

    T last() throws IOException;
}
