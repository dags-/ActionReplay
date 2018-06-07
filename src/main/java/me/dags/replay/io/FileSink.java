package me.dags.replay.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import me.dags.replay.data.Node;
import me.dags.replay.util.Buffers;

/**
 * @author dags <dags@dags.me>
 */
public class FileSink implements Sink<Node,  Node> {

    private final RandomAccessFile file;
    private final OutputStream buffer;

    public FileSink(RandomAccessFile file) {
        this.file = file;
        this.buffer = Buffers.createOutputBuffer(file, 4096);
    }

    @Override
    public void goToEnd() throws IOException {
        file.seek(file.length());
    }

    @Override
    public void writeHeader(Node node) throws IOException {
        if (node.isPresent()) {
            file.seek(0);
            file.setLength(0);
            write(node);
        }
    }

    public void write(Node node) throws IOException {
        if (node.isPresent()) {
            node.write(buffer);
            buffer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
