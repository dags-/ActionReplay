package me.dags.replay.event;

import me.dags.replay.replay.ReplayFile;
import me.dags.replay.replay.ReplayMeta;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.impl.AbstractEvent;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayEvent extends AbstractEvent {

    private final Cause cause;
    private final ReplayMeta meta;
    private final ReplayFile replay;

    private ReplayEvent(ReplayMeta meta, ReplayFile replay) {
        this.meta = meta;
        this.replay = replay;
        this.cause = Sponge.getCauseStackManager().getCurrentCause();
    }

    public ReplayMeta getMeta() {
        return meta;
    }

    public ReplayFile getReplay() {
        return replay;
    }

    @Override
    public Cause getCause() {
        return cause;
    }

    public static class Start extends ReplayEvent {

        public Start(ReplayMeta meta, ReplayFile replay) {
            super(meta, replay);
        }
    }

    public static class Stop extends ReplayEvent {

        public Stop(ReplayMeta meta, ReplayFile replay) {
            super(meta, replay);
        }
    }
}
