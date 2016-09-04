package me.dags.actionreplay.impl;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.animation.frame.Frame;
import me.dags.actionreplay.animation.frame.FrameProvider;
import me.dags.actionreplay.animation.frame.FrameReader;

import java.io.File;

/**
 * @author dags <dags@dags.me>
 */
public class FileFrameProvider implements FrameProvider {

    private final File file;
    private FrameReader frameReader;

    public FileFrameProvider(String name) {
        this.file = ActionReplay.getRecordingFile(name);
    }

    @Override
    public Frame nextFrame() throws Exception {
        return frameReader.next();
    }

    @Override
    public boolean hasNext() throws Exception {
        return frameReader.hasNext();
    }

    @Override
    public void close() throws Exception {
        frameReader.close();
    }

    @Override
    public FileFrameProvider forward() throws Exception {
        this.frameReader = new FrameReader.Forward(file);
        return this;
    }

    @Override
    public FileFrameProvider backward() throws Exception {
        this.frameReader = new FrameReader.Backward(file);
        return this;
    }
}
