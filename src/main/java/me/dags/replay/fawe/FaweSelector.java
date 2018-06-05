package me.dags.replay.fawe;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import me.dags.replay.event.RecordEvent;
import me.dags.replay.frame.FrameRecorder;
import me.dags.replay.frame.schematic.Schem;
import me.dags.replay.frame.selector.Selector;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class FaweSelector implements Selector {

    private FrameRecorder recorder = null;

    public FaweSelector() {
        Schem.SERIALIZER.register("fawe", FaweSchematic.SERIALIZER);
    }

    @Override
    public void register(Object plugin) {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Override
    public AABB getSelection(Player player) {
        try {
            LocalSession session = FaweAPI.wrapPlayer(player).getSession();
            if (session == null) {
                return NULL_BOX;
            }

            com.sk89q.worldedit.world.World sessionWorld = FaweAPI.getWorld(player.getWorld().getName());
            if (sessionWorld == null) {
                return NULL_BOX;
            }

            Region selection = session.getSelection(sessionWorld);
            if (selection == null) {
                return NULL_BOX;
            }

            return getSelectionBox(selection);
        } catch (IncompleteRegionException e) {
            return NULL_BOX;
        }
    }

    @Override
    public Schem createSchematic(Location<World> origin, AABB bounds) {
        Vector3i p0 = bounds.getMin().toInt();
        Vector3i p1 = bounds.getMax().toInt();
        Vector3i p2 = origin.getBlockPosition();
        Vector min = new Vector(p0.getX(), p0.getY(), p0.getZ());
        Vector max = new Vector(p1.getX(), p1.getY(), p1.getZ());
        Vector pos = new Vector(p2.getX(), p2.getY(), p2.getZ());
        CuboidRegion region = new CuboidRegion(FaweAPI.getWorld(origin.getExtent().getName()), min, max);
        Schematic schematic = new Schematic(region);
        schematic.getClipboard().setOrigin(pos);
        return new FaweSchematic(schematic);
    }

    @Override
    public void pos1(Player player, Vector3i pos) {

    }

    @Override
    public void pos2(Player player, Vector3i pos) {

    }

    @Listener
    public void onStartRecording(RecordEvent.Start event) {
        this.recorder = event.getRecorder();
        WorldEdit.getInstance().getEventBus().register(this);
    }

    @Listener
    public void onStopRecording(RecordEvent.Stop event) {
        this.recorder = null;
        WorldEdit.getInstance().getEventBus().unregister(this);
    }
//
//    @Subscribe
//    public void onPaste(PasteEvent event) {
//        if (event.getExtent() instanceof com.sk89q.worldedit.world.World) {
//            String name = ((com.sk89q.worldedit.world.World) event.getExtent()).getName();
//            if (!name.equals(recorder.getOrigin().getExtent().getName())) {
//                return;
//            }
//
//            AABB bounds = getSelectionBox(event.getClipboard().getRegion());
//            if (!bounds.intersects(recorder.getBounds())) {
//                return;
//            }
//
//            Schematic schematic = new Schematic(event.getClipboard());
//            FaweSchematic schem = new FaweSchematic(schematic);
//            MassBlockChange change = new MassBlockChange(recorder.getOrigin().getBlockPosition(), schem);
//            recorder.record(change);
//        }
//    }

    private static AABB getSelectionBox(Region region) {
        Vector min = region.getMinimumPoint().toBlockPoint();
        Vector max = region.getMaximumPoint().toBlockPoint();
        return Selector.getBounds(vec3i(min), vec3i(max));
    }

    private static Vector3i vec3i(Vector vector) {
        return new Vector3i(vector.getX(), vector.getY(), vector.getZ());
    }

    private static Vector vec(Vector3i vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }
}
