package me.dags.replay.io;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.frame.FrameView;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;

/**
 * @author dags <dags@dags.me>
 */
public class FileSource implements FrameSource {

    private final RandomAccessFile file;

    public FileSource(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public FrameView next() throws IOException {
        if (file.getFilePointer() >= file.length()) {
            return Frame.NONE;
        }

        int length = file.readInt();
        byte[] data = new byte[length];
        file.read(data);

        try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(new ByteArrayInputStream(data)))) {
            Tag tag = nbt.readNamedTag().getTag();
            if (tag instanceof CompoundTag) {
                return Frame.SERIALIZER.deserialize((CompoundTag) tag);
            }
        }

        return Frame.NONE;
    }

    @Override
    public FrameView first() throws IOException {
        reset();
        return next();
    }

    @Override
    public FrameView last() throws IOException {
        reset();
        long last;
        while (true) {
            last = file.getFilePointer();
            int length = file.readInt();
            file.seek(last + length);
            if (file.getFilePointer() >= file.length()) {
                break;
            }
        }
        return next();
    }

    @Override
    public FrameSource reset() throws IOException {
        file.seek(0L);
        return this;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }
}
