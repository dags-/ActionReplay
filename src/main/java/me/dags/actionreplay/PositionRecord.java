package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class PositionRecord {

    private final Location<World> location;
    private final Vector3d rotation;
    private final Vector3d headRotation;
    private final boolean flying;

    public PositionRecord(Player player) {
        this.location = player.getLocation();
        this.rotation = player.getRotation();
        this.headRotation = player.getHeadRotation();
        this.flying = true;
    }

    public Location<World> getLocation() {
        return location;
    }

    public void apply(Living entity) {
        entity.setLocation(location);
        entity.setRotation(rotation);
        entity.setHeadRotation(headRotation);
        entity.offer(Keys.IS_FLYING, flying);
    }
}
