package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.animation.avatar.AvatarInstance;
import me.dags.actionreplay.animation.avatar.AvatarSnapshot;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class AnimationTask implements Consumer<Task> {

    private final Map<UUID, AvatarInstance> avatars = new HashMap<>();
    private final FrameProvider frameProvider;
    private final int intervalTicks;
    private final Vector3d centerD;
    private final Vector3i center;
    private final Runnable finishCallback;

    private boolean showAvatars = true;
    private boolean interrupted = false;
    private int count = 0;
    private Frame next;

    public AnimationTask(FrameProvider frameProvider, Vector3i center, int intervalTicks) {
        this.frameProvider = frameProvider;
        this.intervalTicks = intervalTicks;
        this.centerD = center.toDouble();
        this.center = center;
        this.count = intervalTicks;
        this.finishCallback = () -> {};
    }

    public AnimationTask(FrameProvider frameProvider, Runnable finishCallback, Vector3i center, int intervalTicks) {
        this.frameProvider = frameProvider;
        this.intervalTicks = intervalTicks;
        this.centerD = center.toDouble();
        this.center = center;
        this.count = intervalTicks;
        this.finishCallback = finishCallback;
    }

    @Override
    public void accept(Task task) {
        try {
            if (isInterrupted()) {
                this.clear();
                task.cancel();
                finishCallback.run();
                frameProvider.close();
                return;
            }

            if (next == null && !hasNext()) {
                interrupt();
                return;
            }
            if (count-- > 0) {
                this.tick();
            } else {
                this.count = intervalTicks;
                this.playFrame();
            }
        } catch (Exception e) {
            e.printStackTrace();
            task.cancel();
        }
    }

    public void interrupt() {
        this.interrupted = true;
    }


    public boolean isInterrupted() {
        return interrupted;
    }

    private boolean hasNext() throws Exception {
        return frameProvider.hasNext();
    }

    private void tick() throws Exception {
        this.pauseAvatars();
        if (next == null) {
            next = frameProvider.nextFrame();
        }
    }

    private void playFrame() {
        if (next != null) {
            this.restoreAvatars();
            this.restoreChanges();
            next = null;
        }
    }
    private void clear() {
        removeAvatars();
    }

    private void restoreChanges() {
        if (next == null) {
            return;
        }
        next.getChange().restore(center);
    }

    private void pauseAvatars() {
        avatars.values().forEach(AvatarInstance::pause);
    }

    private void removeAvatars() {
        avatars.values().forEach(AvatarInstance::remove);
        avatars.clear();
    }

    private void restoreAvatars() {
        if (!showAvatars || next == null) {
            return;
        }

        for (AvatarSnapshot snapshot : next.getAvatars()) {
            AvatarInstance instance = avatars.get(snapshot.getUUID());
            if (instance != null) {
                if (snapshot.isTerminal()) {
                    instance.remove();
                    avatars.remove(snapshot.getUUID());
                } else {
                    instance.sync(snapshot, centerD);
                }
            } else if (!snapshot.isTerminal()) {
                instance = new AvatarInstance(snapshot.getUUID());
                avatars.put(snapshot.getUUID(), instance);
                instance.sync(snapshot, centerD);
            }
        }
    }
}
