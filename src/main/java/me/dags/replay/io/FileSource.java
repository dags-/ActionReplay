package me.dags.replay.io;

import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.Tag;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.frame.FrameView;
import me.dags.replay.replay.ReplayMeta;

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
    public ReplayMeta header() throws IOException {
        file.seek(0);
        Tag tag = nextTag();
        if (tag == null || !(tag instanceof CompoundTag)) {
            return ReplayMeta.NONE;
        }
        return ReplayMeta.fromData((CompoundTag) tag);
    }

    @Override
    public FrameView next() throws IOException {
        if (file.getFilePointer() >= file.length()) {
            return Frame.NONE;
        }

        Tag tag = nextTag();
        if (tag instanceof CompoundTag) {
            return Frame.SERIALIZER.deserialize((CompoundTag) tag);
        }

        return Frame.NONE;
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private Tag nextTag() throws IOException {
        int length = file.readInt();
        byte[] data = new byte[length];
        file.read(data);
        try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(new ByteArrayInputStream(data)))) {
            return nbt.readNamedTag().getTag();
        }
    }
}
