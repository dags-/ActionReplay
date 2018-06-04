package me.dags.replay;

import me.dags.replay.frame.FrameSink;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.io.FileSink;
import me.dags.replay.io.FileSource;
import org.spongepowered.api.CatalogType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayFile implements CatalogType {

    private final String name;
    private final File file;

    ReplayFile(String name, File file) {
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

    public FrameSource getSource() throws FileNotFoundException {
        return new FileSource(new RandomAccessFile(file, "r"));
    }

    public FrameSink getSink() throws FileNotFoundException {
        return new FileSink(new RandomAccessFile(file, "rw"));
    }
}
