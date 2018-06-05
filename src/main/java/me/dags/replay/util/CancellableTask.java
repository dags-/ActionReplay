package me.dags.replay.util;

import org.spongepowered.api.scheduler.Task;

import java.io.Closeable;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public abstract class CancellableTask implements Consumer<Task>, Closeable {

    private volatile boolean cancelled = false;

    @Override
    public final void accept(Task task) {
        if (cancelled) {
            task.cancel();
            close();
            return;
        }
        run();
    }

    protected final boolean isCancelled() {
        return cancelled;
    }

    protected final void cancel() {
        this.cancelled = true;
    }

    protected final void startSyncTask(Object plugin, int delay, int ticks) {
        Task.builder().execute(this).delayTicks(delay).intervalTicks(ticks).submit(plugin);
    }

    protected final void startAsyncTask(Object plugin, int delay, int ticks) {
        Task.builder().execute(this).delayTicks(delay).intervalTicks(ticks).async().submit(plugin);
    }

    public abstract void run();

    public abstract void close();
}
