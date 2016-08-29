package me.dags.actionreplay;

import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Playback implements Consumer<Task> {

    private final int delay;
    private ChangeRecord changeRecord;
    private int counter;

    public Playback(ChangeRecord first, int delay) {
        this.changeRecord = first;
        this.delay = delay;
        this.counter = delay;
    }

    @Override
    public void accept(Task task) {
        if (counter-- > 0) {
            if (changeRecord != null) {
                changeRecord.suspendAvatars();
            }
        } else {
            counter = delay;
            ChangeRecord current = changeRecord;
            if (current != null) {
                current.forward();
                changeRecord = changeRecord.next();
            }

            if (changeRecord == null) {
                if (current != null) {
                    current.remove();
                }
                task.cancel();
            }
        }
    }
}
