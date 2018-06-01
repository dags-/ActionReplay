package me.dags.replay.io;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public class SegmentInputStream extends InputStream {

    private final DataInput input;
    private final int length;

    private int pos = 0;

    public SegmentInputStream(DataInput input, int length) {
        this.input = input;
        this.length = length;
    }

    @Override
    public int read() throws IOException {
        if (pos < length) {
            return input.readByte();
        }
        return -1;
    }

    @Override
    public int read(byte[] bytes, int start, int end) throws IOException {
        int amount = end - start;
        if (amount <= 0) {
            return 0;
        }

        if (pos + amount >= length) {
            amount = length - pos;
        }

        input.readFully(bytes, start, start + amount);
        pos += amount;

        return amount;
    }
}
