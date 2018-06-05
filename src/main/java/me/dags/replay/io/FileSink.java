package me.dags.replay.io;

import com.sk89q.jnbt.NBTOutputStream;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.serialize.DataView;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class FileSink implements FrameSink {

    private final ByteArrayOutputBuffer buffer;
    private final RandomAccessFile file;

    public FileSink(RandomAccessFile file) {
        this.file = file;
        this.buffer = new ByteArrayOutputBuffer(1024);
    }

    @Override
    public void skipHeader() throws IOException {
        int length = file.readInt();
        file.seek(file.getFilePointer() + length);
    }

    @Override
    public void writeHeader(DataView header) throws IOException {
        file.seek(0);
        file.setLength(0);
        write(header);
    }

    @Override
    public void write(DataView data) throws IOException {
        buffer.reset();

        try (NBTOutputStream nbt = new NBTOutputStream(new GZIPOutputStream(buffer))) {
            nbt.writeNamedTag("", data.getData());
        }

        buffer.writeTo(file);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
