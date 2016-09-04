package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.animation.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.BlockChange;
import me.dags.actionreplay.event.BlockTransaction;
import me.dags.actionreplay.event.Change;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Recorder {

    public static final Recorder EMPTY = new Recorder(){
        public void start(Object plugin) {
            throw new UnsupportedOperationException("Cannot start EMPTY recorder!");
        }
        public boolean isPresent() {
            return false;
        }
    };

    private final Vector3i min;
    private final Vector3i max;
    protected final Vector3i center;
    protected final UUID worldId;
    protected final int radius;
    protected final int height;

    private boolean recording = false;

    private Recorder() {
        this.worldId = UUID.randomUUID();
        this.center = Vector3i.ZERO;
        this.min = Vector3i.ZERO;
        this.max = Vector3i.ZERO;
        this.radius = 0;
        this.height = 0;
    }

    protected Recorder(UUID worldId, Vector3i center, int radius, int height) {
        this.worldId = worldId;
        this.center = center;
        this.min = new Vector3i(center.getX() - radius, center.getY() - height, center.getZ() - radius);
        this.max = new Vector3i(center.getX() + radius, center.getY() + height, center.getZ() + radius);
        this.radius = radius;
        this.height = height;
    }

    @Listener(order = Order.POST)
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player, center.toDouble());
            List<BlockTransaction> changed = new ArrayList<>();
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                Vector3i position = transaction.getOriginal().getPosition().sub(center);
                BlockState from = transaction.getOriginal().getExtendedState();
                BlockState to = transaction.getFinal().getExtendedState();
                changed.add(new BlockTransaction(position, from, to));
            }
            Change change = new BlockChange(changed);
            addNextFrame(snapshot, change);
        }
    }

    public void start(Object plugin) {
        setRecording(true);
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    public void stop() {
        setRecording(false);
        Sponge.getEventManager().unregisterListeners(this);
    }

    public void addNextFrame(AvatarSnapshot snapshot, Change change) {}

    public Vector3i getCenter() {
        return center;
    }

    public boolean isPresent() {
        return this != EMPTY;
    }

    public boolean isRecording() {
        return recording;
    }

    public void setRecording(boolean recording) {
        this.recording = isPresent() && recording;
    }

    private boolean activeLocation(Location<World> location) {
        return isRecording() && location.getExtent().getUniqueId().equals(worldId) && contains(location.getBlockPosition());
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
