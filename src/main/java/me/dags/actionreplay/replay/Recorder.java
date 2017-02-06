package me.dags.actionreplay.replay;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.blockchange.BlockChange;
import me.dags.actionreplay.event.blockchange.BlockTransaction;
import me.dags.actionreplay.replay.avatar.AvatarSnapshot;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Recorder implements Runnable {

    public static final Recorder EMPTY = new Recorder() {
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
    private final Set<AvatarSnapshot> avatars = new HashSet<>();
    private final Set<BlockTransaction> transactions = new HashSet<>();

    private Task task = null;
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

    public String getWorldName() {
        return Sponge.getServer().getWorld(worldId).map(World::getName).orElse("");
    }

    @Override
    public void run() {
        if (transactions.size() > 0) {
            BlockChange change = new BlockChange(transactions);
            addNextFrame(avatars, change);
            avatars.clear();
            transactions.clear();
        }
    }

    @Listener(order = Order.POST)
    public void onBlockChange(ChangeBlockEvent event, @Root Player player) {
        if (activeLocation(player.getLocation())) {
            AvatarSnapshot snapshot = new AvatarSnapshot(player, center.toDouble());
            avatars.add(snapshot);
            for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
                Vector3i position = transaction.getOriginal().getPosition().sub(center);
                BlockState from = transaction.getOriginal().getExtendedState();
                BlockState to = transaction.getFinal().getExtendedState();
                BlockTransaction blockTransaction = new BlockTransaction(position, from, to);
                transactions.add(blockTransaction);
            }
        }
    }

    public Vector3i center() {
        return center;
    }

    public void start(Object plugin) {
        stopTask();
        setRecording(true);
        Sponge.getEventManager().registerListeners(plugin, this);
        task = Task.builder().execute(this).intervalTicks(1L).delayTicks(1L).submit(plugin);
    }

    public void stop() {
        setRecording(false);
        Sponge.getEventManager().unregisterListeners(this);
        stopTask();
    }

    public void stopNow() {
        setRecording(false);
        Sponge.getEventManager().unregisterListeners(this);
        stopTask();
    }

    public void addNextFrame(Change change) {}

    public void addNextFrame(AvatarSnapshot snapshot, Change change) {}

    public void addNextFrame(Collection<AvatarSnapshot> snapshots, Change change) {}

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

    private void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private boolean activeLocation(Location<World> location) {
        return isRecording() && location.getExtent().getUniqueId().equals(worldId) && contains(location.getBlockPosition());
    }

    public boolean contains(int x, int y, int z) {
        return x >= min.getX() && x <= max.getX() && y >= min.getY() && y <= max.getY() && z >= min.getZ() && z <= max.getZ();
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
