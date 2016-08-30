package me.dags.actionreplay;

import org.spongepowered.api.scheduler.Task;

/**
 * @author dags <dags@dags.me>
 */
public class PlaybackTask implements Runnable {

    static final PlaybackTask EMPTY = new PlaybackTask(null, -1);

    private final int delay;
    private boolean running;
    private KeyFrame frame;
    private int counter;
    private Task task;

    private PlaybackTask(KeyFrame first, int delay) {
        this.frame = first;
        this.delay = delay;
        this.counter = delay;
        this.running = first != null;
    }

    @Override
    public void run() {
        if (!isRunning()) {
            return;
        }
        if (counter-- > 0) {
            if (frame != null) {
                frame.pauseAvatars();
            }
        } else {
            counter = delay;
            KeyFrame current = frame;
            if (current != null) {
                current.play();
                frame = frame.next();
            }

            if (frame == null) {
                if (current != null) {
                    current.removeAvatars();
                }
                cancel();
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void cancel() {
        this.running = false;
        if (task != null) {
            task.cancel();
        }
    }

    public static PlaybackTask startPlayback(Object plugin, KeyFrame first, int delay) {
        PlaybackTask playback = new PlaybackTask(first, delay);
        playback.task = Task.builder().execute(playback).intervalTicks(1).submit(plugin);
        return playback;
    }
}
