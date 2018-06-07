package me.dags.replay.worldedit.fawe;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.event.extent.PasteEvent;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import java.util.function.Supplier;
import me.dags.replay.frame.schematic.Schem;
import me.dags.replay.worldedit.WESelector;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class FaweSelector extends WESelector {

    public FaweSelector() {
        Schem.SERIALIZER.register("fawe", FaweSchematic.SERIALIZER);
    }

    @Override
    public Schem createSchematic(Location<World> origin, Vector3i blockMin, Vector3i blockMax) {
        Vector min = vec(blockMin);
        Vector max = vec(blockMax);
        Vector pos = vec(origin.getBlockPosition());
        com.sk89q.worldedit.world.World world = getWorld(origin.getExtent());
        CuboidRegion region = new CuboidRegion(world, min, max);
        Schematic schematic = new Schematic(region);
        if (schematic.getClipboard() != null) {
            schematic.getClipboard().setOrigin(pos);
        }
        return new FaweSchematic(schematic);
    }

    @Override
    protected com.sk89q.worldedit.entity.Player getPlayer(Player player) {
        return FaweAPI.wrapPlayer(player).getPlayer();
    }

    @Override
    protected com.sk89q.worldedit.world.World getWorld(World world) {
        return FaweAPI.getWorld(world.getName());
    }

    @Override
    protected LocalSession getSession(com.sk89q.worldedit.entity.Player player) {
        return FaweAPI.wrapPlayer(player).getSession();
    }

    @Override
    protected Supplier<Boolean> getCompletionListener(EditSession session) {
        FaweCallback callback = new FaweCallback();
        session.getQueue().setProgressTracker(callback);
        return callback;
    }

    @Subscribe
    public void onPaste(PasteEvent event) {
        if (event.getExtent() instanceof com.sk89q.worldedit.world.World) {
            String name = ((com.sk89q.worldedit.world.World) event.getExtent()).getName();
            if (!name.equals(recorder.getOrigin().getExtent().getName())) {
                return;
            }

            AABB bounds = getSelectionBox(event.getClipboard().getRegion());
            if (!bounds.intersects(recorder.getBounds())) {
                return;
            }

            Schematic schematic = new Schematic(event.getClipboard());
            recorder.onSchematic(new FaweSchematic(schematic), vec3i(event.getPosition()));
        }
    }


}
