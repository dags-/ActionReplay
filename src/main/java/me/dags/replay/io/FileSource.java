package me.dags.replay.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import org.jnbt.CompoundTag;
import org.jnbt.EndTag;
import org.jnbt.Nbt;

/**
 * @author dags <dags@dags.me>
 */
public class FileSource implements Source<CompoundTag> {

    private final RandomAccessFile file;

    public FileSource(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public CompoundTag header() throws IOException {
        file.seek(0);
        return nextNode();
    }

    @Override
    public CompoundTag next() throws IOException {
        return nextNode();
    }

    @Override
    public CompoundTag first() throws IOException {
        file.seek(0);
        // header length
        int length = file.readInt();
        long pos = file.getFilePointer();

        // has at least 4 bytes after header
        if (pos + length + 4 < file.length()) {
            // seek to next length bytes
            file.seek(pos + length);
            // read node
            return nextNode();
        }

        return emptyTag();
    }

    @Override
    public CompoundTag last() throws IOException {
        while (true) {
            int length = file.readInt();
            long pos = file.getFilePointer();
            if (pos + length + 4 >= file.length()) {
                byte[] data = new byte[length];
                file.read(data);
                return Nbt.read(new ByteArrayInputStream(data)).getTag().asCompound();
            }
            file.seek(pos + length);
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private CompoundTag nextNode() throws IOException {
        if (file.getFilePointer() >= file.length()) {
            return emptyTag();
        }
        int length = file.readInt();
        byte[] data = new byte[length];
        file.read(data);
        return Nbt.read(new ByteArrayInputStream(data)).getTag().asCompound();
    }

    private static CompoundTag emptyTag() {
        return EndTag.END.asCompound();
    }
}
