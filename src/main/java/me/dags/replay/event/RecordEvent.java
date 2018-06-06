package me.dags.replay.event;

import me.dags.replay.frame.FrameRecorder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * @author dags <dags@dags.me>
 */
public abstract class RecordEvent extends AbstractEvent {

    private final FrameRecorder recorder;
    private final Cause cause;

    private RecordEvent(FrameRecorder recorder) {
        this.recorder = recorder;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public FrameRecorder getRecorder() {
        return recorder;
    }

    public static class Start extends RecordEvent {

        public Start(FrameRecorder recorder) {
            super(recorder);
        }
    }

    public static class Stop extends RecordEvent {

        public Stop(FrameRecorder recorder) {
            super(recorder);
        }
    }
}
