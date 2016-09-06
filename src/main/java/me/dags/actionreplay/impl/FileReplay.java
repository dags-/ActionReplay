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

    public FileReplay(String name, Location<World> center) {
        super(center);
        this.name = name;
    }

    @Override
    public void undoAllFrames(Runnable callback) {
        try {
            final Location<World> location = center;
            doTask(new FileFrameProvider(name).backward(), frame -> frame.getChange().undo(location), callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void redoAllFrames(Runnable callback) {
        try {
            final Location<World> location = center;
            doTask(new FileFrameProvider(name).forward(), frame -> frame.getChange().restore(location), callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doTask(FrameProvider provider, Consumer<Frame> action, Runnable callback) {
        try {
            currentOperation = new FrameTask(provider, action, callback);
            Task.builder().execute(currentOperation).delayTicks(1).intervalTicks(1).submit(ActionReplay.getInstance());
        } catch (Exception e) {
            e.printStackTrace();
            if (provider != null) {
                try {
                    provider.close();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public FrameProvider getFrameProvider() throws Exception {
        return new FileFrameProvider(name);
    }
}
