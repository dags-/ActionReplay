package me.dags.replay.frame;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.WorldEdit;
import me.dags.commandbus.fmt.Fmt;
import me.dags.replay.ActionReplay;
import me.dags.replay.event.RecordEvent;
import me.dags.replay.frame.avatar.AvatarSnapshot;
import me.dags.replay.frame.block.BlockChange;
import me.dags.replay.frame.block.MassBlockChange;
import me.dags.replay.frame.block.SingleBlockChange;
import me.dags.replay.frame.schematic.Schem;
import me.dags.replay.io.BufferedSink;
import me.dags.replay.replay.ReplayMeta;
import me.dags.replay.util.CancellableTask;
import me.dags.replay.util.OptionalActivity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class FrameRecorder extends CancellableTask implements OptionalActivity {

    private final AABB bounds;
    private final Location<World> origin;
    private final BufferedSink sink;

    private boolean recording = false;
    private List<BlockChange> changes = new LinkedList<>();

    private FrameRecorder() {
        this.origin = null;
        this.bounds = null;
        this.sink = null;
    }

    public FrameRecorder(ReplayMeta meta, FrameSink sink) {
        this.origin = meta.getOrigin();
        this.bounds = meta.getActualBounds();
        this.sink = new BufferedSink(sink);
    }

    @Override
    public String getName() {
        return "recorder";
    }

    @Override
    public boolean isActive() {
        return recording;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public void run() {
        if (!changes.isEmpty()) {
            Frame frame = new Frame(changes, recordAvatars());
            sink.write(frame);
            changes = new LinkedList<>();
        }
    }

    public void start(Object plugin) {
        // register listeners
        Sponge.getEventManager().registerListeners(plugin, this);
        // start io thread
        sink.start(plugin);
        // start recorder task
        startSyncTask(plugin, 1, 1);
        // the first frame of the build
        captureAll();
        // mark as recording
        recording = true;

        RecordEvent event = new RecordEvent.Start(this, Sponge.getCauseStackManager().getCurrentCause());
        Sponge.getEventManager().post(event);

        Fmt.info("Recording started at ").stress(bounds.getMin()).info(" : ")
                .stress(bounds.getMax()).tell(Sponge.getServer().getBroadcastChannel());
    }

    @Override
    public void close() {
        // unregister listeners
        WorldEdit.getInstance().getEventBus().unregister(this);
        Sponge.getEventManager().unregisterListeners(this);
        // the final frame of the build
        captureAll();
        // stop & flush the io thread
        sink.stop();
        // unmark as recording
        recording = false;

        RecordEvent event = new RecordEvent.Stop(this, Sponge.getCauseStackManager().getCurrentCause());
        Sponge.getEventManager().post(event);

        Fmt.subdued("Recording stopped").tell(Sponge.getServer().getBroadcastChannel());
    }

    public AABB getBounds() {
        return bounds;
    }

    public Location<World> getOrigin() {
        return origin;
    }

    public void stop() {
        cancel();
    }

    @Listener(order = Order.POST)
    @Exclude({ChangeBlockEvent.Pre.class, ChangeBlockEvent.Post.class})
    public void onBlockChange(ChangeBlockEvent event) {
        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            // ignore filtered transactions
            if (!transaction.isValid()) {
                continue;
            }

            Location<World> location = transaction.getFinal().getLocation().orElse(null);
            if (location == null) {
                continue;
            }

            // ignore transactions in different worlds
            if (location.getExtent() != origin.getExtent()) {
                continue;
            }

            // ignore transactions outside of the bounds
            if (!bounds.contains(location.getBlockPosition())) {
                continue;
            }

            // record the change
            Vector3i offset = location.getBlockPosition().sub(origin.getBlockPosition());
            BlockState state = transaction.getFinal().getState();
            record(new SingleBlockChange(state, offset));
        }
    }

    public void record(BlockChange change) {
        changes.add(change);
    }

    private List<AvatarSnapshot> recordAvatars() {
        List<AvatarSnapshot> avatars = new LinkedList<>();
        for (Entity entity : origin.getExtent().getIntersectingEntities(bounds, Player.class::isInstance)) {
            Player player = (Player) entity;
            UUID uuid = player.getUniqueId();
            String name = player.getName();
            Vector3d position = player.getLocation().getPosition().sub(origin.getX(), origin.getY(), origin.getZ());
            Vector3d rotation = player.getRotation();
            boolean flying = player.get(Keys.IS_FLYING).orElse(false);
            ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).map(ItemStack::copy).orElse(ItemStack.empty());
            AvatarSnapshot snapshot = new AvatarSnapshot(uuid, name, position, rotation, stack, flying);
            avatars.add(snapshot);
        }
        return avatars;
    }

    private void captureAll() {
        Schem schem = ActionReplay.getSelector().createSchematic(origin, bounds);
        if (schem.isAbsent()) {
            return;
        }
        MassBlockChange change = new MassBlockChange(origin.getBlockPosition(), schem);
        sink.write(new Frame(change, Collections.emptyList()));
    }

    public static final FrameRecorder NONE = new FrameRecorder() {

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void start(Object plugin) {

        }

        @Override
        public void stop() {

        }

        @Override
        public void run() {

        }
    };
}
