package me.dags.actionreplay.memory;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.FrameProvider;

/**
 * @author dags <dags@dags.me>
 */
public class MemoryAnimation extends Animation {

    public MemoryAnimation(Vector3i pos) {
        super(pos);
    }

    @Override
    public void undoAllFrames(Runnable callback) {}

    @Override
    public void redoAllFrames(Runnable callback) {}

    @Override
    public void onFinish() {}

    @Override
    public FrameProvider getFrameProvider() throws Exception {
        return null;
    }
}
