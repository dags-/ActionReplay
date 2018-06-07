package me.dags.replay.worldedit;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Supplier;
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

    private final LinkedList<WEChange> changes = new LinkedList<>();
    protected FrameRecorder recorder = null;

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

        LocalSession session = getSession(wePlayer);
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

        LocalSession session = getSession(wePlayer);
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
    public void tick(FrameRecorder recorder) {
        Location<World> origin = recorder.getOrigin();
        Iterator<WEChange> iterator = changes.iterator();
        while (iterator.hasNext()) {
            WEChange change = iterator.next();
            if (change.hasExpired()) {
                iterator.remove();
                continue;
            }

            if (change.isComplete()) {
                iterator.remove();
                if (change.isValid()) {
                    Vector3i min = change.getBlockMin();
                    Vector3i max = change.getBlockMax();
                    Schem schem = createSchematic(origin, min, max);
                    recorder.onSchematic(schem, origin.getBlockPosition());
                }
            }
        }
    }

    @Override
    public final AABB getSelection(Player player) {
        com.sk89q.worldedit.entity.Player wePlayer = getPlayer(player);
        if (wePlayer == null) {
            return Selector.NULL_BOX;
        }

        LocalSession session = getSession(wePlayer);
        if (session == null) {
            return Selector.NULL_BOX;
        }

        try {
            Region selection = session.getSelection(wePlayer.getWorld());
            return getSelectionBox(selection);
        } catch (IncompleteRegionException e) {
            return Selector.INVALID_BOX;
        }
    }

    @Override
    public Schem createSchematic(Location<World> origin, Vector3i min, Vector3i max) {
        ArchetypeVolume volume = origin.getExtent().createArchetypeVolume(min, max, origin.getBlockPosition());
        Schematic schematic = Schematic.builder().paletteType(BlockPaletteTypes.LOCAL).volume(volume).build();
        return new SpongeSchematic(schematic);
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        com.sk89q.worldedit.world.World world = event.getWorld();
        if (world == null) {
            return;
        }

        if (!world.getName().equals(recorder.getOrigin().getExtent().getName())) {
            return;
        }

        Supplier<Boolean> completion = getCompletionListener(event.getEditSession());
        WEChange extent = new WEChange(event.getExtent(), recorder.getBounds(), completion);
        event.setExtent(extent);
        changes.add(extent);
    }

    protected com.sk89q.worldedit.entity.Player getPlayer(Player player) {
        return WEPlayerMatcher.match(player).orElse(null);
    }

    protected com.sk89q.worldedit.world.World getWorld(World world) {
        for (com.sk89q.worldedit.world.World w : WorldEdit.getInstance().getServer().getWorlds()) {
            if (w.getName().equals(world.getName())) {
                return w;
            }
        }
        return null;
    }

    protected Supplier<Boolean> getCompletionListener(EditSession session) {
        return WEChange.FALSE;
    }

    protected LocalSession getSession(com.sk89q.worldedit.entity.Player player) {
        return WorldEdit.getInstance().getSessionManager().getIfPresent(player);
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
