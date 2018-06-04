package me.dags.replay.replay;

import java.io.IOException;
import me.dags.replay.ReplayManager;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.frame.FrameView;
import me.dags.replay.util.CancellableTask;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Replay extends CancellableTask implements OptionalValue {

    private final ReplayManager manager;
    private final ReplayContext context;
    private final Location<World> origin;
    private final FrameSource frames;

    private long tick = 0L;
    private long interval = 1L;
    private boolean playing = false;

    public Replay(FrameSource frames, Location<World> origin, ReplayManager manager) {
        this.frames = frames;
        this.origin = origin;
        this.manager = manager;
        this.context = new ReplayContext();
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void start(Object plugin, int intervalTicks) {
        startSync(plugin, intervalTicks);
        interval = intervalTicks;
        playing = true;
        manager.onReplayStarted();
    }

    public void stop() {
        setCancelled(true);
        playing = false;
    }

    @Override
    public void run() {
        if (!isFrameTick()) {
            context.tick();
            return;
        }

        try {
            FrameView frame = frames.next();

            if (!frame.isPresent()) {
                setCancelled(true);
                return;
            }

            frame.apply(origin, context);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void dispose() {
        context.dispose();
        manager.onReplayStopped();
    }

    private boolean isFrameTick() {
        if (++tick >= interval) {
            tick = 0;
            return true;
        }
        return false;
    }

    public static final Replay NONE = new Replay(null, null, null) {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void start(Object plugin, int intervalTicks) {

        }

        @Override
        public void stop() {

        }

        @Override
        public void run() {

        }

        @Override
        public void dispose() {

        }
    };
}
