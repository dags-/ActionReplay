package me.dags.actionreplay;

import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Playback implements Consumer<Task> {

    private final int delay;
    private Snapshot snapshot;
    private int counter;

    public Playback(Snapshot first, int delay) {
        this.snapshot = first;
        this.delay = delay;
        this.counter = delay;
    }

    @Override
    public void accept(Task task) {
        if (counter-- > 0) {
            if (snapshot != null) {
                snapshot.pauseAvatars();
            }
        } else {
            counter = delay;
            Snapshot current = snapshot;
            if (current != null) {
                current.restore();
                snapshot = snapshot.next();
            }

            if (snapshot == null) {
                if (current != null) {
                    current.removeAvatars();
                }
                task.cancel();
            }
        }
    }
}
