package me.dags.actionreplay.impl;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.io.FrameFileReader;
import me.dags.actionreplay.replay.frame.Frame;
import me.dags.actionreplay.replay.frame.FrameProvider;

import java.io.File;

/**
 * @author dags <dags@dags.me>
 */
public class FileFrameProvider implements FrameProvider {

    private final File file;
    private FrameFileReader reader;

    public FileFrameProvider(String name) {
        this.file = ActionReplay.getRecordingFile(name);
    }

    @Override
    public Frame nextFrame() throws Exception {
        return reader.next();
    }

    @Override
    public boolean hasNext() throws Exception {
        return reader.hasNext();
    }

    @Override
    public void close() throws Exception {
        reader.close();
    }

    @Override
    public FileFrameProvider forward() throws Exception {
        this.reader = new FrameFileReader.Forward(file);
        return this;
    }

    @Override
    public FileFrameProvider backward() throws Exception {
        this.reader = new FrameFileReader.Backward(file);
        return this;
    }
}
