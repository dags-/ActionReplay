package me.dags.actionreplay.impl;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.replay.Replay;
import me.dags.actionreplay.replay.frame.Frame;
import me.dags.actionreplay.replay.frame.FrameProvider;
import me.dags.actionreplay.replay.frame.FrameTask;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class FileReplay extends Replay {

    private final String name;
    private FrameTask currentTask = null;

    public FileReplay(String name, Location<World> center) {
        super(center);
        this.name = name;
    }

    @Override
    public void undoAllFrames(Runnable callback) {
        final Location<World> location = center;
        doTask(frame -> frame.getChange().undo(location), callback);
    }

    @Override
    public void redoAllFrames(Runnable callback) {
        final Location<World> location = center;
        doTask(frame -> frame.getChange().restore(location), callback);
    }

    private void doTask(Consumer<Frame> action, Runnable callback) {
        FrameProvider frameProvider = null;
        try {
            if (currentTask != null) {
                currentTask.interrupt();
            }

            frameProvider = new FileFrameProvider(name);
            currentTask = new FrameTask(frameProvider, action, callback);
            Task.builder().execute(currentTask).delayTicks(1).intervalTicks(1).submit(ActionReplay.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
            if (frameProvider != null) {
                try {
                    frameProvider.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onFinish() {
        super.animationTask = null;
        super.playing = false;
    }

    @Override
    public FrameProvider getFrameProvider() throws Exception {
        return new FileFrameProvider(name).forward();
    }
}
