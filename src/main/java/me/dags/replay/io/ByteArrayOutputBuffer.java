package me.dags.replay.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class ByteArrayOutputBuffer extends ByteArrayOutputStream {

    public ByteArrayOutputBuffer(int size) {
        super(size);
    }

    public void writeTo(DataOutput output) throws IOException {
        output.writeInt(count);
        output.write(buf, 0, count);
    }
}
