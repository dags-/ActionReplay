package me.dags.actionreplay.replay;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.masschange.MassChange;
import me.dags.actionreplay.replay.avatar.Avatar;
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

    private boolean reuseCurrent = false;
    private int progress = 0;
    private Frame current;

    public ReplayPlayer(FrameProvider provider, int minOperations) {
        this.provider = provider;
        this.progress = minOperations;
        this.minOperations = minOperations;
        this.maxOperations = ActionReplay.getInstance().getConfig().maxOperationsPerTick;
    }

    public boolean protect(Entity entity) {
        return entityIds.contains(entity.getUniqueId());
    }

    public boolean finished() throws Exception {
        return !provider.hasNext();
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
        if (current == null || !reuseCurrent) {
            if (!provider.hasNext()) {
                return;
            }

            progress = 0;
            reuseCurrent = false;
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
        // handle MassChanges differently
        if (frame.getChange() instanceof MassChange) {
            playMassChange((MassChange) frame.getChange(), offset);
        } else {
            playSingleChange(frame.getChange(), offset);
        }

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

        // if restoring multiple changes per tick, may have skipped over terminal snapshots, so remove if
        // not present in the latest Frame
        if (minOperations > 1) {
            // N.B. AvatarInstance & AvatarSnapshot inherit hashCode() & equals(..) methods from Avatar
            Collection<? extends Avatar> active = frame.getAvatars();
            Iterator<? extends  Avatar> removalIterator = avatars.values().iterator();
            while (removalIterator.hasNext()) {
                if (!active.contains(removalIterator.next())) {
                    removalIterator.remove();
                }
            }
        }
    }

    private void playMassChange(MassChange change, Location<World> offset) throws Exception {
        // mark current frame as being re-used
        reuseCurrent = true;

        // bulk restore from current progress position and update position
        progress += change.restoreRange(progress, maxOperations, offset);

        // un-mark frame as in-use if nothing left to restore
        if (progress >= change.size()) {
            reuseCurrent = false;
        }
    }

    private void playSingleChange(Change change, Location<World> offset) throws Exception {
        while (progress++ < minOperations) {
            change.restore(offset);
            playNext(offset);
        }
    }
}
