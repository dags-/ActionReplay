package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.scheduler.Task;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Animation {

    public static final Animation EMPTY = new Animation(Vector3i.ZERO) {
        public void undoAllFrames(Runnable callback) {}
        public void redoAllFrames(Runnable callback) {}
        public void onFinish() {}
        public FrameProvider getFrameProvider() throws Exception {
            throw new UnsupportedOperationException("EMPTY cannot provide a FrameProvider");
        }
    };

    protected Vector3i center;
    protected boolean playing = false;
    protected AnimationTask animationTask;

    public Animation(Vector3i center) {
        this.center = center;
    }

    public void setCenter(Vector3i center) {
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
            this.playing = false;
            this.animationTask = null;
        }));
    }

    public void start(Object plugin, int intervalTicks) {
        try {
            this.animationTask = new AnimationTask(getFrameProvider(), this::onFinish, center, intervalTicks);
            Task.builder().intervalTicks(1).delayTicks(1).execute(this.animationTask).submit(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void undoAllFrames(Runnable callback);

    public abstract void redoAllFrames(Runnable callback);

    public abstract void onFinish();

    public abstract FrameProvider getFrameProvider() throws Exception;
}
