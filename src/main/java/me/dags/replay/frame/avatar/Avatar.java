package me.dags.replay.frame.avatar;

import com.flowpowered.math.vector.Vector3d;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class Avatar implements OptionalValue {

    public static final Avatar NONE = new Avatar(null);

    private final Human human;
    private Location<World> location;

    Avatar(Human human) {
        this.human = human;
    }

    @Override
    public boolean isPresent() {
        return this != NONE;
    }

    public Human getHuman() {
        return human;
    }

    public void dispose() {
        human.remove();
    }

    public void tick() {
        human.setLocation(location);
        human.setVelocity(Vector3d.ZERO);
    }

    public void setLocation(Location<World> location) {
        this.location = location;
    }
}
