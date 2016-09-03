package me.dags.actionreplay.animation;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Animation {

    public static final Animation EMPTY = new Animation() {
        public void undoAllFrames(Runnable callback) {}
        public void redoAllFrames(Runnable callback) {}
        public void onFinish() {}
        public FrameProvider getFrameProvider() throws Exception {
            throw new UnsupportedOperationException("EMPTY cannot provide a FrameProvider");
        }
    };

    protected Location<World> center;
    protected boolean playing = false;
    protected AnimationTask animationTask;

    private Animation() {}

    public Animation(Location<World> center) {
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
            throw new UnsupportedOperationException("An animation is already playing");
        }
        this.playing = true;
        undoAllFrames(() -> start(plugin, intervalTicks));
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
            animationTask = new AnimationTask(getFrameProvider(), this::onFinish, center, intervalTicks);
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
