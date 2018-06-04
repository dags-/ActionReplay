package me.dags.replay.io;

import com.sk89q.jnbt.NBTOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPOutputStream;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.frame.FrameView;

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
    public void accept(FrameView frame) throws IOException {
        // reset buffer
        buffer.reset();

        // write ot buffer
        try (NBTOutputStream nbt = new NBTOutputStream(new GZIPOutputStream(buffer))) {
            nbt.writeNamedTag("", frame.toData());
        }

        // writes the length of the data and then the data itself to file
        buffer.writeTo(file);
    }
}
