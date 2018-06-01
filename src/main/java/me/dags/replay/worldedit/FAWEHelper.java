package me.dags.replay.worldedit;

import com.boydti.fawe.FaweAPI;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class FAWEHelper extends WEHelper {

    @Override
    protected com.sk89q.worldedit.LocalSession getLocalSession(Player player) {
        return FaweAPI.wrapPlayer(player).getSession();
    }

    @Override
    protected com.sk89q.worldedit.world.World getWorld(World world) {
        return FaweAPI.getWorld(world.getName());
    }
}
