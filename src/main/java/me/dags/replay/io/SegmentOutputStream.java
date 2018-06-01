package me.dags.replay.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class SegmentOutputStream extends ByteArrayOutputStream {

    private final DataOutput output;

    public SegmentOutputStream(int size, DataOutput output) {
        super(size);
        this.output = output;
    }

    @Override
    public void flush() throws IOException {
        output.writeInt(count);
        output.write(buf, 0, count);
    }
}
