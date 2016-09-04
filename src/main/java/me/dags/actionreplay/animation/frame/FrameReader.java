package me.dags.actionreplay.animation.frame;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author dags <dags@dags.me>
 */
public abstract class FrameReader {

    private static final Frame.Builder BUILDER = new Frame.Builder();
    private static final long INT_LENGTH = 4;

    private final RandomAccessFile file;
    private final long length;
    private long pos = 0;

    public FrameReader(RandomAccessFile file) throws IOException {
        this.file = file;
        this.length = file.length();
    }

    public abstract boolean hasNext() throws IOException;

    public abstract Frame next() throws IOException;

    public void close() throws IOException {
        file.close();
    }

    protected boolean hasForward() throws IOException {
        return pos + 4 < length;
    }

    protected boolean hasBackward() throws IOException {
        return pos - 4 >= 0;
    }

    protected Frame readForward() throws IOException {
        int length = file.readInt();
        Frame frame = readFrame(length);
        file.readInt();
        pos += INT_LENGTH + length + INT_LENGTH;
        return frame;
    }

    protected Frame readBackward() throws IOException {
        file.seek(pos -= FrameReader.INT_LENGTH);
        int length = file.readInt();

        file.seek(pos -= length);
        Frame frame = readFrame(length);

        pos -= FrameReader.INT_LENGTH;
        return frame;
    }

    private Frame readFrame(int length) throws IOException {
        byte[] data = new byte[length];
        file.read(data);
        DataContainer container = DataFormats.NBT.readFrom(new ByteArrayInputStream(data));
        return BUILDER.fastBuild(container);
    }

    public static class Forward extends FrameReader {

        public Forward(File file) throws IOException {
            super(new RandomAccessFile(file, "r"));
        }

        @Override
        public boolean hasNext() throws IOException {
            return super.hasForward();
        }

        @Override
        public Frame next() throws IOException {
            return super.readForward();
        }
    }

    public static class Backward extends FrameReader {

        public Backward(File file) throws IOException {
            super(new RandomAccessFile(file, "r"));
            super.pos = super.file.length();
        }

        @Override
        public boolean hasNext() throws IOException {
            return super.hasBackward();
        }

        @Override
        public Frame next() throws IOException {
            return super.readBackward();
        }
    }
}
