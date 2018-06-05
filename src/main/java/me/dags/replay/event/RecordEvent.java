package me.dags.replay.event;

import me.dags.replay.frame.FrameRecorder;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * @author dags <dags@dags.me>
 */
public class RecordEvent extends AbstractEvent {

    private final FrameRecorder recorder;
    private final Cause cause;

    public RecordEvent(FrameRecorder recorder, Cause cause) {
        this.recorder = recorder;
        this.cause = cause;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public FrameRecorder getRecorder() {
        return recorder;
    }

    public static class Start extends RecordEvent {

        public Start(FrameRecorder recorder, Cause cause) {
            super(recorder, cause);
        }
    }

    public static class Stop extends RecordEvent {

        public Stop(FrameRecorder recorder, Cause cause) {
            super(recorder, cause);
        }
    }
}
