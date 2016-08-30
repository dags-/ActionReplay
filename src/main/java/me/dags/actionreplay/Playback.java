package me.dags.actionreplay;

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
public class Playback implements Consumer<Task> {

    private final Map<UUID, AvatarInstance> avatars = new HashMap<>();
    private final int intervalTicks;
    private int count = 0;
    private Frame frame;
    private boolean interrupt = false;

    public Playback(Frame first, int intervalTicks) {
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
            syncAvatars(frame);
            restoreChanges(frame);
            count = intervalTicks;
            frame = frame.next();
        }
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
}
