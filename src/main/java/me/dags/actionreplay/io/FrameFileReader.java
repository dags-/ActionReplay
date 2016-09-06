package me.dags.actionreplay.io;

import me.dags.actionreplay.replay.frame.Frame;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author dags <dags@dags.me>
 */
public abstract class FrameFileReader implements AutoCloseable {

    private final RandomAccessFile file;
    private final long length;

    private FrameFileReader(File file) throws FileNotFoundException {
        this.file = new RandomAccessFile(file, "r");
        this.length = file.length();
    }

    @Override
    public void close() throws Exception {
        file.close();
    }

    public abstract boolean hasNext() throws IOException;

    public abstract Frame next() throws IOException;

    public static class Forward extends FrameFileReader {

        public Forward(File file) throws FileNotFoundException {
            super(file);
        }

        @Override
        public boolean hasNext() throws IOException {
            return super.file.getFilePointer() + FrameFileFormat.INT_BYTES < super.length;
        }

        @Override
        public Frame next() throws IOException {
            return FrameFileFormat.FORMAT.readForward(super.file);
        }
    }

    public static class Backward extends FrameFileReader {

        public Backward(File file) throws IOException {
            super(file);
            super.file.seek(super.length);
        }

        @Override
        public boolean hasNext() throws IOException {
            return super.file.getFilePointer() - FrameFileFormat.INT_BYTES >= 0;
        }

        @Override
        public Frame next() throws IOException {
            return FrameFileFormat.FORMAT.readBackward(super.file);
        }
    }
}
