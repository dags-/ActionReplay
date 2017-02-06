package me.dags.actionreplay.io;

import me.dags.actionreplay.replay.frame.Frame;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author dags <dags@dags.me>
 */

/**
 * Format of one block: { (int) dataLength : (byte[]) data : (int) dataLength }
 * Where 'data' is a byte array of length 'dataLength'
 */
public class FrameFileFormat {

    private static final Frame.Builder BUILDER = new Frame.Builder();
    static final FrameFileFormat FORMAT = new FrameFileFormat();
    static final int INT_BYTES = 4;

    /**
     * Writes the DataSerializable to the provided OutputStream (output)
     * Writes the length of the DataSerializable bytes before and after, so that the file can be read in reverse
     */
    public void write(ByteArrayDataOutputStream output, DataSerializable data) throws IOException {
        ByteArrayDataOutputStream content = new ByteArrayDataOutputStream(8192);
        DataFormats.NBT.writeTo(content, data.toContainer());

        output.writeInt(content.size());
        content.writeTo(output);
        output.writeInt(content.size());
    }

    /**
     * Reads one 'block' forwards from the Frame file
     * Expects that the file pointer is directly before the block
     *
     * 1. Read int at the head of the block (the length of the following byte array)
     * 2. Read 'length' number of bytes into an array
     * 3. Skip the next 4 bytes as these are the tail of the block (ie directly before the next block, or end of file)
     * 4. Builds and returns the Frame from read byte array
     */
    public Frame readForward(RandomAccessFile file) throws IOException {
        int size = file.readInt();

        byte[] data = new byte[size];
        file.read(data);
        file.readInt();

        return read(data);
    }

    /**
     * Reads one 'block' backwards from the Frame file
     * Expects that the file pointer is directly after the block
     *
     * 1. Move the file pointer back 4 bytes (ie 1 int) and read the int (the length of the preceding byte array)
     * 2. Move the file pointer back (4 + length) bytes and read the byte array
     * 3. Move the file pointer back (4 + length + 4) bytes (ie directly before the preceding block, or start of file)
     * 4. Builds and returns the Frame from the read byte array
     */
    public Frame readBackward(RandomAccessFile file) throws IOException {
        long pos = file.getFilePointer();

        file.seek(pos -= INT_BYTES);
        int size = file.readInt();

        file.seek(pos -= size);
        byte[] data = new byte[size];
        file.read(data);

        file.seek(pos - INT_BYTES);
        return read(data);
    }

    private Frame read(byte[] data) throws IOException {
        DataContainer container = DataFormats.NBT.readFrom(new ByteArrayInputStream(data));
        return BUILDER.buildUnchecked(container);
    }
}
