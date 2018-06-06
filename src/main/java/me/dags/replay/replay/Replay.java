package me.dags.replay.replay;

import java.io.IOException;
import me.dags.replay.event.ReplayEvent;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.frame.FrameView;
import me.dags.replay.util.CancellableTask;
import me.dags.replay.util.OptionalActivity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Replay extends CancellableTask implements OptionalActivity {

    private final ReplayContext context;
    private final ReplayMeta meta;
    private final ReplayFile file;
    private final Location<World> origin;
    private final FrameSource source;

    private long tick = 0L;
    private long interval = 1L;
    private boolean playing = false;

    public Replay(ReplayFile file, ReplayMeta meta, FrameSource source) {
        this.source = source;
        this.meta = meta;
        this.file = file;
        this.origin = meta.getOrigin();
        this.context = new ReplayContext();
    }

    public void start(Object plugin, int intervalTicks) {
        Task.builder().execute(this).delayTicks(1).intervalTicks(1).submit(plugin);
        interval = intervalTicks;
        playing = true;
        Sponge.getEventManager().post(new ReplayEvent.Start(meta, file));
    }

    public void stop() {
        cancel();
        playing = false;
        Sponge.getEventManager().post(new ReplayEvent.Stop(meta, file));
    }

    @Override
    public String getName() {
        return "replay";
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public boolean isActive() {
        return playing;
    }

    @Override
    public void run() {
        context.tick();

        if (!isFrameTick()) {
            return;
        }

        try {
            FrameView frame = source.next();

            if (!frame.isPresent()) {
                stop();
                return;
            }

            frame.apply(origin, context);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public void close() {
        context.dispose();
        try {
            source.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isFrameTick() {
        if (++tick >= interval) {
            tick = 0;
            return true;
        }
        return false;
    }

    public static final Replay NONE = new Replay(null, ReplayMeta.NONE, null) {
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
        public void close() {

        }
    };
}
