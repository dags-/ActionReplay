package me.dags.replay.util;

import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public abstract class CancellableTask implements Consumer<Task> {

    private volatile boolean cancelled = false;

    public final CancellableTask startSync(Object plugin) {
        Task.builder().execute(this).delayTicks(1).intervalTicks(1).submit(plugin);
        return this;
    }

    public final CancellableTask startAsync(Object plugin) {
        Task.builder().execute(this).delayTicks(1).intervalTicks(1).async().submit(plugin);
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
