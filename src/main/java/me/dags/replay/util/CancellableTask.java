package me.dags.replay.util;

import java.util.function.Consumer;
import org.spongepowered.api.scheduler.Task;

/**
 * @author dags <dags@dags.me>
 */
public abstract class CancellableTask implements Consumer<Task> {

    private volatile boolean cancelled = false;

    public final CancellableTask startSync(Object plugin, int intervalTicks) {
        Task.builder().execute(this).intervalTicks(intervalTicks).submit(plugin);
        return this;
    }

    public final CancellableTask startAsync(Object plugin, int intervalTicks) {
        Task.builder().execute(this).intervalTicks(intervalTicks).submit(plugin);
        return this;
    }

    public final boolean isCancelled() {
        return cancelled;
    }

    public final void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public void accept(Task task) {
        if (isCancelled()) {
            task.cancel();
            dispose();
            return;
        }
        run();
    }

    public abstract void run();

    public abstract void dispose();
}
