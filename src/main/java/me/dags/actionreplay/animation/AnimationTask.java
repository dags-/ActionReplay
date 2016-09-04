package me.dags.actionreplay.animation;

import me.dags.actionreplay.animation.avatar.AvatarInstance;
import me.dags.actionreplay.animation.avatar.AvatarSnapshot;
import me.dags.actionreplay.animation.frame.Frame;
import me.dags.actionreplay.animation.frame.FrameProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class AnimationTask implements Consumer<Task> {

    private final Map<UUID, AvatarInstance> avatars = new HashMap<>();
    private final Set<UUID> entityIds = new HashSet<>();
    private final FrameProvider frameProvider;
    private final int intervalTicks;
    private final Location<World> center;
    private final Runnable finishCallback;

    private boolean showAvatars = true;
    private boolean interrupted = false;
    private int count = 0;
    private Frame next;

    public AnimationTask(FrameProvider frameProvider, Runnable finishCallback, Location<World> center, int intervalTicks) {
        this.frameProvider = frameProvider;
        this.intervalTicks = intervalTicks;
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

    @Listener
    public void damageListener(DamageEntityEvent event) {
        if (entityIds.contains(event.getTargetEntity().getUniqueId())) {
            event.setCancelled(true);
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
        Sponge.getEventManager().unregisterListeners(this);
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
        entityIds.clear();
    }

    private void restoreAvatars() {
        if (!showAvatars || next == null) {
            return;
        }

        for (AvatarSnapshot snapshot : next.getAvatars()) {
            AvatarInstance instance = avatars.get(snapshot.getUUID());
            if (instance != null) {
                if (snapshot.isTerminal()) {
                    avatars.remove(snapshot.getUUID());
                    entityIds.remove(instance.getEntityId());
                    instance.remove();
                } else {
                    instance.sync(snapshot, center);
                }
            } else if (!snapshot.isTerminal()) {
                instance = new AvatarInstance(snapshot.getUUID());
                instance.sync(snapshot, center);

                avatars.put(snapshot.getUUID(), instance);
                entityIds.add(instance.getEntityId());
            }
        }
    }
}
