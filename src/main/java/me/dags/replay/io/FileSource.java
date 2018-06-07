package me.dags.replay.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import me.dags.replay.ActionReplay;
import me.dags.replay.data.Node;

/**
 * @author dags <dags@dags.me>
 */
public class FileSource implements Source<Node, Node> {

    private final RandomAccessFile file;

    public FileSource(RandomAccessFile file) {
        this.file = file;
    }

    @Override
    public Node header() throws IOException {
        file.seek(0);
        return nextNode();
    }

    @Override
    public Node next() throws IOException {
        return nextNode();
    }

    @Override
    public Node first() throws IOException {
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

        return Node.EMPTY;
    }

    @Override
    public Node last() throws IOException {
        while (true) {
            int length = file.readInt();
            long pos = file.getFilePointer();
            if (pos + length + 4 >= file.length()) {
                byte[] data = new byte[length];
                file.read(data);
                return ActionReplay.getNodeFactory().read(new ByteArrayInputStream(data));
            }
            file.seek(pos + length);
        }
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private Node nextNode() throws IOException {
        if (file.getFilePointer() >= file.length()) {
            return Node.EMPTY;
        }
        int length = file.readInt();
        byte[] data = new byte[length];
        file.read(data);
        return ActionReplay.getNodeFactory().read(new ByteArrayInputStream(data));
    }
}
