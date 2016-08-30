package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector2i;
import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
    private PlaybackTask playback = PlaybackTask.EMPTY;
    private boolean recording = false;
    private KeyFrame first = null;
    private KeyFrame last = null;

    public Recorder(UUID worldId, Vector3i center, int size) {
        this.worldId = worldId;
        this.min = new Vector2i(center.getX() - size, center.getZ() - size);
        this.max = new Vector2i(center.getX() + size, center.getZ() + size);
    }

    @Listener(order = Order.POST)
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            Avatar avatar = getOrCreate(player);
            KeyFrame blockChange = new BlockFrame(avatar, event);
            addNextFrame(blockChange);
        }
    }

    @Listener(order = Order.POST)
    public void onSignChange(ChangeSignEvent event, @Root Player player) {
        if (activeLocation(event.getTargetTile().getLocation())) {
            Avatar avatar = getOrCreate(player);
            KeyFrame signChange = new SignFrame(avatar, event);
            addNextFrame(signChange);
        }
    }

    @Listener(order = Order.POST)
    public void onEntitySpawn(SpawnEntityEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            Avatar avatar = getOrCreate(player);
            KeyFrame entityChange = new EntityFrame(avatar, event);
            addNextFrame(entityChange);
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
        playback = PlaybackTask.startPlayback(plugin, first, ticks);
    }

    public void resetPlayback() {
        KeyFrame frame = last;
        while (frame != null) {
            frame.reset();
            frame = frame.previous();
        }
    }

    public void stopPlayback() {
        playback.cancel();
        playback = PlaybackTask.EMPTY;
        resetPlayback();
        KeyFrame frame = first;
        while (frame != null) {
            frame.restore();
            frame = frame.next();
        }
    }

    private Avatar getOrCreate(Player player) {
        Avatar avatar = avatars.get(player.getUniqueId());
        if (avatar == null) {
            avatars.put(player.getUniqueId(), avatar = new Avatar(player));
        }
        return avatar;
    }

    private void addNextFrame(KeyFrame next) {
        if (first == null) {
            first = next;
            last = first;
        } else {
            last.setNext(next);
            last = next;
        }
    }

    private boolean activeLocation(Location<World> location) {
        return isRecording() && location.getExtent().getUniqueId() == worldId && contains(location.getBlockPosition());
    }

    private boolean contains(Vector3i pos) {
        return min.getX() < pos.getX() && min.getY() < pos.getZ() && max.getX() > pos.getX() && max.getY() > pos.getZ();
    }
}
