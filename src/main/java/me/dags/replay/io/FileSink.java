package me.dags.replay.io;

import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import me.dags.replay.util.Buffers;
import org.jnbt.CompoundTag;
import org.jnbt.Nbt;

/**
 * @author dags <dags@dags.me>
 */
public class FileSink implements Sink<CompoundTag> {

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
    public void writeHeader(CompoundTag node) throws IOException {
        if (node.isPresent()) {
            file.seek(0);
            file.setLength(0);
            write(node);
        }
    }

    public void write(CompoundTag node) throws IOException {
        if (node.isPresent()) {
            Nbt.write(node, buffer);
            buffer.flush();
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
