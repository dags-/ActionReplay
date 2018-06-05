package me.dags.replay.frame;

import me.dags.replay.serialize.DataView;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameSink extends Closeable {

    void skipHeader() throws IOException;

    void write(DataView frame) throws IOException;

    void writeHeader(DataView header) throws IOException;
}
