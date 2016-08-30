package me.dags.actionreplay;

import me.dags.actionreplay.avatar.AvatarInstance;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Playback implements Runnable {

    private final Map<UUID, AvatarInstance> avatars = new HashMap<>();
    private final int intervalTicks;
    private int count = 0;
    private Frame frame;
    private Task task;

    public Playback(Frame first, int intervalTicks) {
        this.intervalTicks = intervalTicks;
        this.frame = first;
    }

    @Override
    public void run() {
        if (count-- > 0) {
            pauseAvatars();
        } else if (frame == null) {
            finishedPlayback();
        } else {
            count = intervalTicks;
            syncAvatars(frame);
            restoreChanges(frame);
            frame = frame.next();
        }
    }

    public void attachTask(Task task) {
        this.task = task;
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    private void pauseAvatars() {
        avatars.values().forEach(AvatarInstance::pause);
    }

    private void removeAvatars() {
        avatars.values().forEach(AvatarInstance::remove);
        avatars.clear();
    }

    private void syncAvatars(Frame frame) {
        for (AvatarSnapshot snapshot : frame.getAvatars()) {
            AvatarInstance instance = avatars.get(snapshot.getUUID());
            if (instance != null) {
                if (snapshot.isTerminal()) {
                    instance.remove();
                    avatars.remove(snapshot.getUUID());
                } else {
                    instance.sync(snapshot);
                }
            } else if (!snapshot.isTerminal()) {
                instance = new AvatarInstance(snapshot.getUUID());
                avatars.put(snapshot.getUUID(), instance);
                instance.sync(snapshot);
            }
        }
    }

    private void restoreChanges(Frame frame) {
        frame.getChange().restore();
    }

    private void finishedPlayback() {
        frame = null;
        removeAvatars();
        if (task != null) {
            task.cancel();
        }
    }
}
