package me.dags.replay.replay;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import me.dags.replay.io.FileSink;
import me.dags.replay.io.FileSource;
import me.dags.replay.io.Sink;
import me.dags.replay.io.Source;
import org.jnbt.CompoundTag;
import org.spongepowered.api.CatalogType;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayFile implements CatalogType {

    private final String name;
    private final File file;

    public ReplayFile(String name, File file) {
        this.name = name;
        this.file = file;
    }

    @Override
    public String getId() {
        return name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return file.toString();
    }

    public boolean exists() {
        return file.exists();
    }

    public boolean delete() {
        return file.delete();
    }

    public Source<CompoundTag> getSource() throws FileNotFoundException {
        return new FileSource(new RandomAccessFile(file, "r"));
    }

    public Sink<CompoundTag> getSink() throws FileNotFoundException {
        return new FileSink(new RandomAccessFile(file, "rw"));
    }
}
