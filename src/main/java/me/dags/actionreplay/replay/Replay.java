package me.dags.actionreplay.replay;

import me.dags.actionreplay.replay.frame.FrameProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Replay {

    public static final Replay EMPTY = new Replay() {
        public void undoAllFrames(Runnable callback) {}
        public void redoAllFrames(Runnable callback) {}
        public void onFinish() {}
        public FrameProvider getFrameProvider() throws Exception {
            throw new UnsupportedOperationException("EMPTY cannot provide a FrameProvider");
        }
    };

    protected Location<World> center;
    protected boolean playing = false;
    protected ReplayTask animationTask;

    private Replay() {}

    public Replay(Location<World> center) {
        this.center = center;
    }

    public void setCenter(Location<World> center) {
        this.center = center;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean isPlaying() {
        return playing || animationTask != null;
    }

    public void play(Object plugin, int intervalTicks) {
        if (isPlaying()) {
            throw new UnsupportedOperationException("An replay is already playing");
        }
        playing = true;
        undoAllFrames(() -> start(plugin, Math.max(intervalTicks, 1)));
    }

    public void stop() {
        if (!isPlaying()) {
            throw new UnsupportedOperationException("Animation is not playing");
        }

        animationTask.interrupt();

        undoAllFrames(() -> redoAllFrames(() -> {
            playing = false;
            animationTask = null;
        }));
    }

    public void start(Object plugin, int intervalTicks) {
        try {
            animationTask = new ReplayTask(getFrameProvider(), this::onFinish, center, intervalTicks);
            Sponge.getEventManager().registerListeners(plugin, animationTask);
            Task.builder().intervalTicks(1).delayTicks(1).execute(animationTask).submit(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void undoAllFrames(Runnable callback);

    public abstract void redoAllFrames(Runnable callback);

    public abstract void onFinish();

    public abstract FrameProvider getFrameProvider() throws Exception;
}
