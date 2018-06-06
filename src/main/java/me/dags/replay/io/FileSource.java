package me.dags.replay.io;

import me.dags.replay.ActionReplay;
import me.dags.replay.data.Node;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.frame.FrameView;
import me.dags.replay.replay.ReplayMeta;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

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
        Node node = nextNode();
        if (node.isAbsent()) {
            return ReplayMeta.NONE;
        }
        return ReplayMeta.SERIALIZER.deserialize(node);
    }

    @Override
    public FrameView next() throws IOException {
        if (file.getFilePointer() >= file.length()) {
            return Frame.NONE;
        }
        Node node = nextNode();
        if (node.isAbsent()) {
            return Frame.NONE;
        }
        return Frame.SERIALIZER.deserialize(node);
    }

    @Override
    public void close() throws IOException {
        file.close();
    }

    private Node nextNode() throws IOException {
        int length = file.readInt();
        byte[] data = new byte[length];
        file.read(data);
        return ActionReplay.getNodeFactory().read(new ByteArrayInputStream(data));
    }
}
