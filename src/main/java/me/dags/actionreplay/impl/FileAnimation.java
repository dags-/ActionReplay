package me.dags.actionreplay.impl;

import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.frame.FrameProvider;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class FileAnimation extends Animation {

    private final String name;

    public FileAnimation(String name, Location<World> center) {
        super(center);
        this.name = name;
    }

    @Override
    public void undoAllFrames(Runnable callback) {
        try (FrameProvider frameProvider = new FileFrameProvider(name).backward()) {
            while (frameProvider.hasNext()) {
                frameProvider.nextFrame().getChange().undo(center);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        callback.run();
    }

    @Override
    public void redoAllFrames(Runnable callback) {
        try (FrameProvider frameProvider = new FileFrameProvider(name).forward()) {
            while (frameProvider.hasNext()) {
                frameProvider.nextFrame().getChange().restore(center);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        callback.run();
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
