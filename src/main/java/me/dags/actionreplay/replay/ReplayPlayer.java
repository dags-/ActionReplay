package me.dags.actionreplay.replay;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.event.masschange.MassChange;
import me.dags.actionreplay.replay.avatar.AvatarInstance;
import me.dags.actionreplay.replay.avatar.AvatarSnapshot;
import me.dags.actionreplay.replay.frame.Frame;
import me.dags.actionreplay.replay.frame.FrameProvider;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayPlayer {

    private final Map<UUID, AvatarInstance> avatars = new HashMap<>();
    private final Set<UUID> entityIds = new HashSet<>();
    private final FrameProvider provider;
    private final int minOperations;
    private final int maxOperations;

    private int progress = 0;
    private Frame current;

    public ReplayPlayer(FrameProvider provider, int minOperations) {
        this.provider = provider;
        this.minOperations = Math.max(minOperations, 1);
        this.maxOperations = Math.max(ActionReplay.getInstance().getConfig().maxOperationsPerTick, 1);
    }

    public boolean protect(Entity entity) {
        return entityIds.contains(entity.getUniqueId());
    }

    public boolean finished() throws Exception {
        return !provider.hasNext() && current == null;
    }

    public void stop() throws Exception {
        provider.close();
    }

    public void clear() {
        entityIds.clear();
        avatars.values().forEach(AvatarInstance::remove);
        avatars.clear();
    }

    public void pause() {
        avatars.values().forEach(AvatarInstance::pause);
    }

    // loads next Frame if one isn't already loaded
    // if frame is spanning multiple ticks, do not load a new one
    public void loadNext() throws Exception {
        if (current == null) {
            if (!provider.hasNext()) {
                return;
            }

            progress = 0;
            current = provider.nextFrame();
        }
    }

    // restores the current frame's changes and syncs avatars
    public void playNext(Location<World> offset) throws Exception {
        if (current == null) {
            return;
        }

        play(current, offset);
    }

    private void play(Frame frame, Location<World> offset) throws Exception {
        if (frame.getChange() instanceof MassChange) {
            playMassChange(frame, offset);
        } else {
            playSingleChange(frame, offset);
        }
    }

    private void playMassChange(Frame frame, Location<World> offset) throws Exception {
        MassChange change = (MassChange) frame.getChange();

        // bulk restore from current progress position and update position
        progress += change.restoreRange(progress, maxOperations, offset);

        if (progress >= change.size() - 1) {
            current = null;
        }

        syncAvatars(frame, offset);
    }

    private void playSingleChange(Frame frame, Location<World> offset) throws Exception {
        frame.getChange().restore(offset);

        if (progress == 0) {
            Frame last = current;

            while (++progress < minOperations && provider.hasNext()) {
                last = provider.nextFrame();

                if (last != null) {
                    play(last, offset);
                }
            }

            syncAvatars(last, offset);
            current = null;
        }
    }

    private void syncAvatars(Frame frame, Location<World> offset) {
        // sync active avatars creating them if necessary. Remove any marked as terminal
        for (AvatarSnapshot snapshot : frame.getAvatars()) {
            AvatarInstance instance = avatars.get(snapshot.getUUID());

            if (instance != null) {
                if (snapshot.isTerminal()) {
                    avatars.remove(snapshot.getUUID());
                    entityIds.remove(instance.getEntityId());
                    instance.remove();
                } else {
                    instance.sync(snapshot, offset);
                }
            } else if (!snapshot.isTerminal()) {
                instance = new AvatarInstance(snapshot.getUUID());
                instance.sync(snapshot, offset);

                avatars.put(snapshot.getUUID(), instance);
                entityIds.add(instance.getEntityId());
            }
        }
    }
}
