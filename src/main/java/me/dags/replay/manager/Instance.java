package me.dags.replay.manager;

import me.dags.replay.frame.FrameRecorder;
import me.dags.replay.replay.Replay;
import me.dags.replay.replay.ReplayFile;
import me.dags.replay.replay.ReplayMeta;
import me.dags.replay.util.OptionalValue;

/**
 * @author dags <dags@dags.me>
 */
public class Instance implements OptionalValue {

    private final ReplayFile file;

    private Replay replay = Replay.NONE;
    private ReplayMeta meta = ReplayMeta.NONE;
    private FrameRecorder recorder = FrameRecorder.NONE;

    Instance(ReplayFile file) {
        this.file = file;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    public ReplayFile getReplayFile() {
        return file;
    }

    public Replay getReplay() {
        return replay;
    }

    public ReplayMeta getMeta() {
        return meta;
    }

    public FrameRecorder getRecorder() {
        return recorder;
    }

    public void setMeta(ReplayMeta meta) {
        this.meta = meta;
    }

    public void setReplay(Replay replay) {
        this.replay = replay;
    }

    public void setRecorder(FrameRecorder recorder) {
        this.recorder = recorder;
    }

    public void dispose() {
        if (replay.isPresent() && replay.isActive()) {
            replay.stop();
        }
        if (recorder.isPresent() && recorder.isActive()) {
            recorder.stop();
        }

        replay = Replay.NONE;
        recorder = FrameRecorder.NONE;
    }

    public static final Instance NONE = new Instance(null) {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void setRecorder(FrameRecorder recorder) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setReplay(Replay replay) {
            throw new UnsupportedOperationException();
        }

        public ReplayMeta getMeta() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Replay getReplay() {
            throw new UnsupportedOperationException();
        }

        @Override
        public FrameRecorder getRecorder() {
            throw new UnsupportedOperationException();
        }
    };
}
