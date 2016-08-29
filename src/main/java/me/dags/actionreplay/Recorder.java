package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.scheduler.Task;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Recorder {

    private final Map<UUID, Avatar> avatars = new HashMap<>();
    private final UUID worldId;
    private final Vector2i min;
    private final Vector2i max;

    private boolean recording = false;
    private Task task = null;
    private ChangeRecord first = null;
    private ChangeRecord current = null;

    public Recorder(UUID worldId, Vector3i center, int size) {
        this.worldId = worldId;
        min = new Vector2i(center.getX() - size, center.getZ() - size);
        max = new Vector2i(center.getX() + size, center.getZ() + size);
    }

    private Avatar getOrCreateAvatar(Player player) {
        Avatar avatar = avatars.get(player.getUniqueId());
        if (avatar == null) {
            avatars.put(player.getUniqueId(), avatar = new Avatar(player));
        }
        return avatar;
    }

    private void reset() {
        ChangeRecord record = current;
        while (record != null) {
            record.reset();
            record = record.previous();
        }
    }

    private boolean contains(Vector3i pos) {
        return min.getX() < pos.getX() && min.getY() < pos.getZ() && max.getX() > pos.getX() && max.getY() > pos.getZ();
    }

    @Listener
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        if (player.getWorld().getUniqueId() == worldId && contains(player.getLocation().getBlockPosition())) {
            Avatar avatar = getOrCreateAvatar(player);
            BlockRecord blockRecord = new BlockRecord(event.getTransactions());
            if (first == null) {
                first = new ChangeRecord(avatar, blockRecord);
                current = first;
            } else {
                current = current.addRecord(avatar, blockRecord);
            }
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public void playBack(Object plugin, int ticks) {
        stop();
        reset();
        Playback playback = new Playback(first, ticks);
        this.task = Task.builder().delayTicks(ticks).intervalTicks(1).execute(playback).submit(plugin);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
            reset();
            ChangeRecord record = first;
            while (record != null) {
                record.restore();
                record = record.next();
            }
        }
    }
}
