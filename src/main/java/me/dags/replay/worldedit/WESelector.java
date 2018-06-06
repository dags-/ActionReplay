package me.dags.replay.worldedit;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import me.dags.replay.event.RecordEvent;
import me.dags.replay.frame.FrameRecorder;
import me.dags.replay.frame.schematic.Schem;
import me.dags.replay.frame.schematic.SpongeSchematic;
import me.dags.replay.frame.selector.Selector;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

/**
 * @author dags <dags@dags.me>
 */
public class WESelector implements Selector {

    protected FrameRecorder recorder = null;

    public WESelector() {
//        Schem.SERIALIZER.register("we", FaweSchematic.SERIALIZER);
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

    @Override
    public final void register(Object plugin) {

    }

    @Override
    public final void pos1(Player player, Vector3i pos) {
        com.sk89q.worldedit.entity.Player wePlayer = getPlayer(player);
        if (wePlayer == null) {
            return;
        }

        LocalSession session = getSession(player);
        if (session == null) {
            return;
        }

        Vector position = vec(pos);
        RegionSelector selector = session.getRegionSelector(wePlayer.getWorld());
        if (selector.selectPrimary(position, null)) {
            selector.explainPrimarySelection(wePlayer, session, position);
        }
    }

    @Override
    public final void pos2(Player player, Vector3i pos) {
        com.sk89q.worldedit.entity.Player wePlayer = getPlayer(player);
        if (wePlayer == null) {
            return;
        }

        LocalSession session = getSession(player);
        if (session == null) {
            return;
        }

        Vector position = vec(pos);
        RegionSelector selector = session.getRegionSelector(wePlayer.getWorld());
        if (selector.selectSecondary(position, null)) {
            selector.explainSecondarySelection(wePlayer, session, position);
        }
    }

    @Override
    public final AABB getSelection(Player player) {
        com.sk89q.worldedit.world.World world = getWorld(player.getWorld());
        if (world == null) {
            return Selector.NULL_BOX;
        }

        LocalSession session = getSession(player);
        if (session == null) {
            return Selector.NULL_BOX;
        }

        try {
            Region selection = session.getSelection(world);
            return getSelectionBox(selection);
        } catch (IncompleteRegionException e) {
            return Selector.INVALID_BOX;
        }
    }

    @Override
    public Schem createSchematic(Location<World> origin, AABB bounds) {
        Vector3i min = bounds.getMin().toInt();
        Vector3i max = bounds.getMax().toInt();
        ArchetypeVolume volume = origin.getExtent().createArchetypeVolume(min, max, origin.getBlockPosition());
        Schematic schematic = Schematic.builder().paletteType(BlockPaletteTypes.LOCAL).volume(volume).build();
        return new SpongeSchematic(schematic);
    }

    protected com.sk89q.worldedit.entity.Player getPlayer(Player player) {
        return new WEPlayer(player.getName(), player.getUniqueId());
    }

    protected com.sk89q.worldedit.world.World getWorld(World world) {
        for (com.sk89q.worldedit.world.World w : WorldEdit.getInstance().getServer().getWorlds()) {
            if (w.getName().equals(world.getName())) {
                return w;
            }
        }
        return null;
    }

    protected LocalSession getSession(Player player) {
        return WorldEdit.getInstance().getSessionManager().findByName(player.getName());
    }

    protected static Vector3i vec3i(Vector vector) {
        return new Vector3i(vector.getX(), vector.getY(), vector.getZ());
    }

    protected static Vector vec(Vector3i vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    protected static AABB getSelectionBox(Region region) {
        Vector min = region.getMinimumPoint().toBlockPoint();
        Vector max = region.getMaximumPoint().toBlockPoint();
        return Selector.getBounds(vec3i(min), vec3i(max));
    }
}
