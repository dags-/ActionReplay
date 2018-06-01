package me.dags.replay.io;

import com.sk89q.jnbt.NBTOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPOutputStream;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.util.CompoundBuilder;

/**
 * @author dags <dags@dags.me>
 */
public class FileSink implements FrameSink {

    private final SegmentOutputStream buffer;
    private final RandomAccessFile file;

    public FileSink(RandomAccessFile file) {
        this.file = file;
        this.buffer = new SegmentOutputStream(1024, file);
    }

    @Override
    public void accept(Frame frame) throws IOException {
        CompoundBuilder builder = new CompoundBuilder();
        Frame.SERIALIZER.serialize(frame, builder);

        // make sure cursor is at end of file
        file.seek(file.length());

        // reset buffer
        buffer.reset();
        try (NBTOutputStream nbt = new NBTOutputStream(new GZIPOutputStream(buffer))) {
            nbt.writeNamedTag("", builder.build());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // flush buffer to file
        buffer.flush();
    }
}
