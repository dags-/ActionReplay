package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.avatar.AvatarInstance;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class AnimationTask implements Consumer<Task> {

    static final AnimationTask EMPTY = new AnimationTask();

    private final Map<UUID, AvatarInstance> avatars = new HashMap<>();
    private final Vector3i center;
    private final boolean showAvatars;
    private final int intervalTicks;
    private int count = 0;
    private Frame frame;
    private boolean interrupt = false;

    private AnimationTask() {
        this.center = Vector3i.ZERO;
        this.intervalTicks = -1;
        this.interrupt = true;
        this.showAvatars = false;
    }

    public AnimationTask(Vector3i center, Frame first, int intervalTicks, boolean showAvatars) {
        this.center = center;
        this.showAvatars = showAvatars;
        this.intervalTicks = intervalTicks;
        this.frame = first;
    }

    @Override
    public void accept(Task task) {
        if (interrupt) {
            task.cancel();
            removeAvatars();
            return;
        }
        if (count-- > 0) {
            pauseAvatars();
        } else if (frame == null) {
            stop();
        } else {
            count = intervalTicks;

            syncAvatars(frame);
            restoreChanges(frame);
            frame = frame.next();
        }
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean isInterrupted() {
        return this.interrupt;
    }

    public void start(Object plugin) {
        Task.builder().delayTicks(1).intervalTicks(1).execute(this).submit(plugin);
    }

    public void stop() {
        this.interrupt = true;
    }

    private void pauseAvatars() {
        avatars.values().forEach(AvatarInstance::pause);
    }

    private void removeAvatars() {
        avatars.values().forEach(AvatarInstance::remove);
        avatars.clear();
    }

    private void syncAvatars(Frame frame) {
        if (showAvatars) {
            Vector3d relative = center.toDouble();
            for (AvatarSnapshot snapshot : frame.getAvatars()) {
                AvatarInstance instance = avatars.get(snapshot.getUUID());
                if (instance != null) {
                    if (snapshot.isTerminal()) {
                        instance.remove();
                        avatars.remove(snapshot.getUUID());
                    } else {
                        instance.sync(snapshot, relative);
                    }
                } else if (!snapshot.isTerminal()) {
                    instance = new AvatarInstance(snapshot.getUUID());
                    avatars.put(snapshot.getUUID(), instance);
                    instance.sync(snapshot, relative);
                }
            }
        }
    }

    private void restoreChanges(Frame frame) {
        frame.getChange().restore(center);
    }
}
