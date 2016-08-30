package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.BlockChange;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.EntityChange;
import me.dags.actionreplay.event.SignChange;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Recorder {

    private final UUID worldId;
    private final Vector2i min;
    private final Vector2i max;

    private boolean recording = false;
    private Frame first = null;
    private Frame last = null;
    private Playback playback;

    public Recorder(UUID worldId, Vector3i center, int size) {
        this.worldId = worldId;
        this.min = new Vector2i(center.getX() - size, center.getZ() - size);
        this.max = new Vector2i(center.getX() + size, center.getZ() + size);
    }

    @Listener(order = Order.POST)
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player);
            Change change = new BlockChange(event);
            addNextFrame(snapshot, change);
        }
    }

    @Listener(order = Order.POST)
    public void onSignChange(ChangeSignEvent event, @Root Player player) {
        if (activeLocation(event.getTargetTile().getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player);
            Change change = new SignChange(event);
            addNextFrame(snapshot, change);
        }
    }

    @Listener(order = Order.POST)
    public void onEntitySpawn(SpawnEntityEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player);
            Change change = new EntityChange(event);
            addNextFrame(snapshot, change);
        }
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public void play(Object plugin, int intervalTicks) {
        if (!isRecording() && playback == null) {
            Frame.undoAll(last);
            playback = new Playback(first, intervalTicks);
            Task task = Task.builder().intervalTicks(intervalTicks).execute(playback).submit(plugin);
            playback.attachTask(task);
        }
    }

    public void interrupt() {
        if (playback != null) {
            playback.stop();
            Frame.undoAll(last);
            Frame.restoreAll(first);
        }
    }

    private void addNextFrame(AvatarSnapshot snapshot, Change change) {
        Frame nextFrame = new Frame(snapshot, change);
        if (first == null) {
            first = nextFrame;
            last = first;
        } else {
            last.setNext(nextFrame);
            last = nextFrame;
        }
    }

    private boolean activeLocation(Location<World> location) {
        return isRecording() && location.getExtent().getUniqueId() == worldId && contains(location.getBlockPosition());
    }

    private boolean contains(Vector3i pos) {
        return min.getX() < pos.getX() && min.getY() < pos.getZ() && max.getX() > pos.getX() && max.getY() > pos.getZ();
    }
}
