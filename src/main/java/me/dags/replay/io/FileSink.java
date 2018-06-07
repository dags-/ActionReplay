package me.dags.replay.io;

import java.io.ByteArrayOutputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.RandomAccessFile;
import me.dags.replay.data.Node;
import me.dags.replay.frame.FrameSink;

/**
 * @author dags <dags@dags.me>
 */
public class FileSink implements FrameSink {

    private final Buffer buffer;
    private final RandomAccessFile file;

    public FileSink(RandomAccessFile file) {
        this.file = file;
        this.buffer = new Buffer(1024);
    }

    @Override
    public void goToEnd() throws IOException {
        file.seek(file.length());
    }

    @Override
    public void writeHeader(Node node) throws IOException {
        file.seek(0);
        file.setLength(0);
        write(node);
    }

    public void write(Node node) throws IOException {
        buffer.reset();
        node.write(buffer);
        buffer.writeTo(file);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private static class Buffer extends ByteArrayOutputStream {

        private Buffer(int size) {
            super(size);
        }

        private void writeTo(DataOutput output) throws IOException {
            output.writeInt(count);
            output.write(buf, 0, count);
        }
    }
}
