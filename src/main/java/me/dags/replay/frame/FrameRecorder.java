package me.dags.replay.frame;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.PasteEvent;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import me.dags.config.Node;
import me.dags.replay.ReplayManager;
import me.dags.replay.avatar.AvatarSnapshot;
import me.dags.replay.block.BlockChange;
import me.dags.replay.block.MassBlockChange;
import me.dags.replay.block.SingleBlockChange;
import me.dags.replay.io.BufferedFrameSink;
import me.dags.replay.util.OptionalValue;
import me.dags.replay.worldedit.WEHelper;
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
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class FrameRecorder implements OptionalValue {

    private final UUID world;
    private final AABB bounds;
    private final Vector3i origin;
    private final BufferedFrameSink sink;
    private final ReplayManager manager;

    private boolean recording = false;

    private FrameRecorder() {
        this.world = null;
        this.bounds = null;
        this.origin = null;
        this.sink = null;
        this.manager = null;
    }

    public FrameRecorder(World world, AABB bounds, Vector3i origin, FrameSink recorder, ReplayManager manager) {
        this.world = world.getUniqueId();
        this.sink = new BufferedFrameSink(recorder);
        this.bounds = bounds;
        this.origin = origin;
        this.manager = manager;
    }

    public boolean isRecording() {
        return recording;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    public void start(Object plugin) {
        // register listeners
        WorldEdit.getInstance().getEventBus().register(this);
        Sponge.getEventManager().registerListeners(plugin, this);
        // start io thread
        sink.start(plugin);
        // the first frame of the build
        captureAll();
        // mark as recording
        recording = true;
        // notify manager
        manager.onRecorderStarted();
    }

    public void stop() {
        // unregister listeners
        WorldEdit.getInstance().getEventBus().unregister(this);
        Sponge.getEventManager().unregisterListeners(this);
        // the final frame of the build
        captureAll();
        // stop & flush the io thread
        sink.stop();
        // unmark as recording
        recording = false;
        // notify manager
        manager.onRecorderStopped();
    }

    public void writeTo(Node node) {
        String world = Sponge.getServer().getWorld(this.world).map(World::getName).orElse("");
        if (world.isEmpty()) {
            return;
        }
        node.set("world", world);
        writeVec(origin, node.node("origin"));
        writeVec(bounds.getMin().toInt(), node.node("min"));
        writeVec(bounds.getMax().toInt(), node.node("max"));
    }

    @Subscribe
    public void onPaste(PasteEvent event) {
        // only interested in pastes to the world
        if (event.getExtent() instanceof com.sk89q.worldedit.world.World) {
            String name = ((com.sk89q.worldedit.world.World) event.getExtent()).getName();
            Optional<World> world = Sponge.getServer().getWorld(name);

            // make sure world is valid and the one we're recording in
            if (!world.isPresent() || world.get().getUniqueId() != this.world) {
                return;
            }

            Vector pos = event.getPosition();
            Vector3i offset = new Vector3i(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).sub(origin);
            Schematic schematic = new Schematic(event.getClipboard());
            MassBlockChange change = new MassBlockChange(offset, schematic);
            List<AvatarSnapshot> avatars = getAvatars(world.get());
            sink.accept(new Frame(change, avatars));
        }
    }

    @Listener(order = Order.POST)
    public void onBlockChange(ChangeBlockEvent event) {
        List<BlockChange> changes = Collections.emptyList();
        Location<World> location = null;

        for (Transaction<BlockSnapshot> transaction : event.getTransactions()) {
            // ignore filtered transactions
            if (!transaction.isValid()) {
                continue;
            }

            location = transaction.getFinal().getLocation().orElse(null);
            if (location == null) {
                continue;
            }

            // ignore transactions in different worlds
            if (location.getExtent().getUniqueId() != world) {
                continue;
            }

            // ignore transactions outside of the bounds
            if (!bounds.contains(location.getBlockPosition())) {
                continue;
            }

            // init the changes list
            if (changes.isEmpty()) {
                changes = new LinkedList<>();
            }

            // record the change
            Vector3i offset = location.getBlockPosition().sub(origin);
            BlockState state = transaction.getFinal().getState();
            BlockChange change = new SingleBlockChange(state, offset);
            changes.add(change);
        }

        // no applicable changes to record
        if (changes.isEmpty() || location == null) {
            return;
        }

        // capture the active players and queue the frame
        List<AvatarSnapshot> avatars = getAvatars(location.getExtent());
        sink.accept(new Frame(changes, avatars));
    }

    private List<AvatarSnapshot> getAvatars(World world) {
        List<AvatarSnapshot> avatars = Collections.emptyList();
        for (Entity entity : world.getIntersectingEntities(bounds, Player.class::isInstance)) {
            Player player = (Player) entity;

            if (avatars.isEmpty())  {
                avatars = new LinkedList<>();
            }

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
        String worldName = Sponge.getServer().getWorld(this.world).map(World::getName).orElse("");
        if (worldName.isEmpty()) {
            return;
        }

        Vector min = WEHelper.toVec(bounds.getMin().toInt());
        Vector max = WEHelper.toVec(bounds.getMax().toInt());
        com.sk89q.worldedit.world.World world = FaweAPI.getWorld(worldName);
        CuboidRegion region = new CuboidRegion(world, min, max);
        Schematic schematic = new Schematic(region);
        MassBlockChange change = new MassBlockChange(origin, schematic);
        sink.accept(new Frame(change, Collections.emptyList()));
    }

    private static void writeVec(Vector3i vec, Node node) {
        node.set("x", vec.getX());
        node.set("y", vec.getY());
        node.set("z", vec.getZ());
    }

    public static final FrameRecorder NONE = new FrameRecorder() {

        @Override
        public boolean isRecording() {
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
        public void writeTo(Node node) {

        }
    };
}
