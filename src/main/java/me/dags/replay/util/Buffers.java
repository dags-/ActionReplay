package me.dags.replay.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author dags <dags@dags.me>
 */
public final class Buffers {

    private static final ThreadLocal<ByteArrayOutputStream> cache = ThreadLocal.withInitial(() -> new ByteArrayOutputStream(128));

    private Buffers() {

    }

    public static ByteArrayOutputStream getCachedBuffer() {
        ByteArrayOutputStream out = cache.get();
        out.reset();
        return out;
    }

    public static OutputStream createOutputBuffer(DataOutput dataOutput, int size) {
        return new DataOutputBuffer(dataOutput, size);
    }

    private static class DataOutputBuffer extends ByteArrayOutputStream {

        private final DataOutput output;

        private DataOutputBuffer(DataOutput output, int size) {
            super(size);
            this.output = output;
        }

        @Override
        public void flush() throws IOException {
            output.writeInt(count);
            output.write(buf, 0, count);
            reset();
        }
    }
}
