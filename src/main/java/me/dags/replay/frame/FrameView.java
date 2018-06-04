package me.dags.replay.frame;

import com.sk89q.jnbt.CompoundTag;
import me.dags.replay.replay.ReplayContext;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameView extends OptionalValue {

    CompoundTag toData();

    void apply(Location<World> origin, ReplayContext context);
}
