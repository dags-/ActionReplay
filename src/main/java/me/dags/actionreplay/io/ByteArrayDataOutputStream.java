package me.dags.actionreplay.io;

import com.google.common.primitives.Ints;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */

/**
 * A ByteArrayOutputStream that can 'writeTo' DataOutputs without the need to copy the buffer first
 *
 * i.e. avoids:
 *     void writeTo(ByteArrayOutputStream stream, DataOutput output) {
 *         byte[] datatypes = stream.toByteArray(); // Array.copy operation!
 *         output.writeTo(datatypes);
 *     }
 */
public class ByteArrayDataOutputStream extends ByteArrayOutputStream {

    public ByteArrayDataOutputStream(int size) {
        super(size);
    }

    public void writeTo(DataOutput dataOutput) throws IOException {
        dataOutput.write(super.buf, 0, super.count);
    }

    public void writeInt(int integer) throws IOException {
        super.write(Ints.toByteArray(integer));
    }
}
