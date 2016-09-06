package me.dags.actionreplay.replay;

import me.dags.actionreplay.replay.frame.FrameProvider;
import me.dags.actionreplay.replay.frame.FrameTask;
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
        public void stopNow() {}
        public FrameProvider getFrameProvider() throws Exception {
            throw new UnsupportedOperationException("EMPTY cannot provide a FrameProvider");
        }
    };

    protected Location<World> center;
    protected boolean playing = false;
    protected ReplayTask animationTask;
    protected FrameTask currentTask = null;

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

    public void play(Object plugin, int intervalTicks, int changesPerTick) {
        if (isPlaying()) {
            throw new UnsupportedOperationException("An replay is already playing");
        }
        playing = true;
        undoAllFrames(() -> start(plugin, intervalTicks, changesPerTick));
    }

    public void stop() {
        if (!isPlaying()) {
            throw new UnsupportedOperationException("Animation is not playing");
        }

        animationTask.interrupt();

        undoAllFrames(() -> redoAllFrames(() -> {
            playing = false;
            animationTask = null;
            currentTask = null;
        }));
    }

    public void stopNow() {
        if (!isPresent()) {
            return;
        }

        if (isPlaying()) {
            animationTask.stop();
        }

        if (isPlaying() || (currentTask != null && currentTask.active())) {
            try {
                currentTask.interrupt();

                try (FrameProvider undo = getFrameProvider().backward()) {
                    while (undo.hasNext()) {
                        undo.nextFrame().getChange().undo(center);
                    }
                }

                try (FrameProvider restore = getFrameProvider().forward()) {
                    while (restore.hasNext()) {
                        restore.nextFrame().getChange().restore(center);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void start(Object plugin, int interval, int changesPer) {
        try {
            animationTask = new ReplayTask(getFrameProvider().forward(), this::onFinish, center, interval, changesPer);
            Sponge.getEventManager().registerListeners(plugin, animationTask);
            Task.builder().intervalTicks(1).delayTicks(1).execute(animationTask).submit(plugin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onFinish() {
        animationTask = null;
        playing = false;
    }

    public abstract void undoAllFrames(Runnable callback);

    public abstract void redoAllFrames(Runnable callback);

    public abstract FrameProvider getFrameProvider() throws Exception;
}
