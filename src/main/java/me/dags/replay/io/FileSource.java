package me.dags.replay.io;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.GZIPInputStream;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSource;

/**
 * @author dags <dags@dags.me>
 */
public class FileSource implements FrameSource {

    private final RandomAccessFile file;

    public FileSource(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public Frame next() throws IOException {
        int length = file.readInt();
        if (file.getFilePointer() + length < file.length()) {
            try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(new SegmentInputStream(file, length)))) {
                Tag tag = nbt.readNamedTag().getTag();
                if (tag instanceof CompoundTag) {
                    return Frame.SERIALIZER.deserialize((CompoundTag) tag);
                }
            }
        }
        return Frame.NONE;
    }

    @Override
    public Frame first() throws IOException {
        reset();
        return next();
    }

    @Override
    public Frame last() throws IOException {
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
}
