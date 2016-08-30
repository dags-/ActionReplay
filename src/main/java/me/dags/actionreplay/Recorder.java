package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
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
    private Snapshot first = null;
    private Snapshot current = null;

    public Recorder(UUID worldId, Vector3i center, int size) {
        this.worldId = worldId;
        this.min = new Vector2i(center.getX() - size, center.getZ() - size);
        this.max = new Vector2i(center.getX() + size, center.getZ() + size);
    }

    @Listener(order = Order.POST)
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        if (player.getWorld().getUniqueId() == worldId && contains(player.getLocation().getBlockPosition())) {
            Avatar avatar = getOrCreate(player);
            if (first == null) {
                first = new Snapshot(avatar, event.getTransactions());
                current = first;
            } else {
                Snapshot next = new Snapshot(avatar, event.getTransactions());
                current.setNext(next);
                current = next;
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
        stopPlayback();
        resetPlayback();
        Playback playback = new Playback(first, ticks);
        task = Task.builder().delayTicks(ticks).intervalTicks(1).execute(playback).submit(plugin);
    }

    public void resetPlayback() {
        Snapshot record = current;
        while (record != null) {
            record.resetBlocks();
            record = record.previous();
        }
    }

    public void stopPlayback() {
        if (task != null) {
            task.cancel();
            task = null;
            resetPlayback();
            Snapshot record = first;
            while (record != null) {
                record.restoreBlocks();
                record = record.next();
            }
        }
    }

    private Avatar getOrCreate(Player player) {
        Avatar avatar = avatars.get(player.getUniqueId());
        if (avatar == null) {
            avatars.put(player.getUniqueId(), avatar = new Avatar(player));
        }
        return avatar;
    }

    private boolean contains(Vector3i pos) {
        return min.getX() < pos.getX() && min.getY() < pos.getZ() && max.getX() > pos.getX() && max.getY() > pos.getZ();
    }
}
