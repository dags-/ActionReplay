package me.dags.replay.worldedit;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.IncompleteRegionException;
import com.sk89q.worldedit.LocalSession;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.regions.Region;
import java.util.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class WEHelper implements WEAPI {

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public Optional<World> getSelectionWorld(Player player) {
        LocalSession session = getLocalSession(player);
        if (session == null) {
            return Optional.empty();
        }
        return Sponge.getServer().getWorld(session.getSelectionWorld().getName());
    }

    @Override
    public AABB getSelection(Player player, World world) {
        try {
            LocalSession session = getLocalSession(player);
            if (session == null) {
                return NULL_BOX;
            }

            com.sk89q.worldedit.world.World sessionWorld = getWorld(world);
            if (sessionWorld == null) {
                return NULL_BOX;
            }

            Region selection = session.getSelection(sessionWorld);
            if (selection == null) {
                return NULL_BOX;
            }

            Vector3i min = toVec3i(selection.getMinimumPoint());
            Vector3i max = toVec3i(selection.getMaximumPoint());
            return new AABB(min, max);
        } catch (IncompleteRegionException e) {
            return NULL_BOX;
        }
    }

    protected com.sk89q.worldedit.world.World getWorld(World world) {
        for (com.sk89q.worldedit.world.World w : WorldEdit.getInstance().getServer().getWorlds()) {
            if (w.getName().equals(world.getName())) {
                return w;
            }
        }
        return null;
    }

    protected LocalSession getLocalSession(Player player) {
        return WorldEdit.getInstance().getSessionManager().findByName(player.getName());
    }

    public static Vector3i toVec3i(Vector vector) {
        return new Vector3i(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
    }

    public static Vector3d toVec3d(Vector vector) {
        return new Vector3d(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector toVec(Vector3i vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }

    public static Vector toVec(Vector3d vector) {
        return new Vector(vector.getX(), vector.getY(), vector.getZ());
    }
}
