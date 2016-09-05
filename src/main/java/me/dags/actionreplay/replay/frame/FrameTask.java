package me.dags.actionreplay.replay.frame;

import me.dags.actionreplay.ActionReplay;
import org.spongepowered.api.scheduler.Task;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class FrameTask implements Consumer<Task> {

    private final FrameProvider frameProvider;
    private final Consumer<Frame> action;
    private final Runnable callback;
    private final int operations;
    private boolean interrupt = false;

    public FrameTask(FrameProvider frameProvider, Consumer<Frame> action, Runnable callback) {
        this.frameProvider = frameProvider;
        this.callback = callback;
        this.action = action;
        this.operations = ActionReplay.getInstance().getConfig().operationsPerTick;
    }

    public void interrupt() throws Exception {
        if (!interrupt) {
            this.interrupt = true;
            flush();
        }
    }

    public boolean active() {
        return !interrupt;
    }

    @Override
    public void accept(Task task) {
        if (interrupt) {
            task.cancel();
            return;
        }
        try {
            int count = operations;
            while (count-- > 0 && frameProvider.hasNext()) {
                Frame frame = frameProvider.nextFrame();
                if (frame != null) {
                    action.accept(frame);
                }
            }
            if (!frameProvider.hasNext()) {
                task.cancel();
                callback.run();
                frameProvider.close();
            }
        } catch (Exception e) {
            task.cancel();
            try {
                frameProvider.close();
            } catch (Exception e1) {
                e1.printStackTrace();
            }
        }
    }

    private void flush() throws Exception {
        while (frameProvider.hasNext()) {
            Frame frame = frameProvider.nextFrame();
            if (frame != null) {
                action.accept(frame);
            }
        }
        frameProvider.close();
    }
}
