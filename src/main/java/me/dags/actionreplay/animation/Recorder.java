package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.BlockChange;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.EntityChange;
import me.dags.actionreplay.event.SignChange;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.entity.SpawnEntityEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Recorder {

    public static final Recorder EMPTY = new Recorder();

    private final UUID worldId;
    private final Vector3i center;
    private final Vector3i min;
    private final Vector3i max;

    private boolean recording = false;
    private Frame first = null;
    private Frame last = null;

    private Recorder() {
        this.worldId = UUID.randomUUID();
        this.center = Vector3i.ZERO;
        this.min = Vector3i.ZERO;
        this.max = Vector3i.ZERO;
    }

    public Recorder(UUID worldId, Vector3i center, int radius, int height) {
        this.worldId = worldId;
        this.center = center;
        this.min = new Vector3i(center.getX() - radius, center.getY(), center.getZ() - radius);
        this.max = new Vector3i(center.getX() + radius, center.getY() + height, center.getZ() + radius);
    }

    @Listener(order = Order.POST)
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player, center.toDouble());
            List<Transaction<BlockSnapshot>> changed = new ArrayList<>();
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                BlockSnapshot from = transaction.getDefault();
                BlockSnapshot to = transaction.getFinal();

                from = BlockSnapshot.builder().from(from).position(from.getPosition().sub(center)).build();
                to = BlockSnapshot.builder().from(to).position(to.getPosition().sub(center)).build();
                changed.add(new Transaction<>(from, to));
            }
            Change change = new BlockChange(changed);
            addNextFrame(snapshot, change);
        }
    }

    @Listener(order = Order.POST)
    public void onSignChange(ChangeSignEvent event, @Root Player player) {
        if (activeLocation(event.getTargetTile().getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player, center.toDouble());
            BlockSnapshot block = BlockSnapshot.builder().from(event.getTargetTile().getLocation()).build();
            List<Text> lines = event.getText().asList();
            Change change = new SignChange(block, lines);
            addNextFrame(snapshot, change);
        }
    }

    // @Listener(order = Order.POST)
    public void onEntitySpawn(SpawnEntityEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player, center.toDouble());
            Change change = new EntityChange(event.getEntitySnapshots());
            addNextFrame(snapshot, change);
        }
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public Animation getAnimation() {
        return isRecording() ? Animation.EMPTY : new Animation(first, last).setCenter(center);
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = isPresent() && recording;
    }

    private void addNextFrame(AvatarSnapshot snapshot, Change change) {
        Frame nextFrame = new Frame(snapshot, change);
        if (first == null) {
            first = nextFrame;
            last = first;
        } else {
            last.setNext(nextFrame);
            last.passAvatarsToNext(center.toDouble());
            last = nextFrame;
        }
    }

    private boolean activeLocation(Location<World> location) {
        return isRecording() && location.getExtent().getUniqueId() == worldId && contains(location.getBlockPosition());
    }

    private boolean contains(Vector3i pos) {
        return greaterOrEqual(pos, min) && lesserOrEqual(pos, max);
    }

    private static boolean lesserOrEqual(Vector3i pos, Vector3i upperBound) {
        return pos.getX() <= upperBound.getX() && pos.getY() <= upperBound.getY() && pos.getZ() <= upperBound.getZ();
    }

    private static boolean greaterOrEqual(Vector3i pos, Vector3i lowerBound) {
        return pos.getX() >= lowerBound.getX() && pos.getY() >= lowerBound.getY() && pos.getZ() >= lowerBound.getZ();
    }
}
