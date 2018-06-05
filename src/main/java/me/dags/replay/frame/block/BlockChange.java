package me.dags.replay.frame.block;

import me.dags.replay.serialize.InterfaceSerializer;
import me.dags.replay.serialize.Typed;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import javax.annotation.Nonnull;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockChange extends Typed {

    @Nonnull
    void apply(Location<World> origin);

    InterfaceSerializer<BlockChange> SERIALIZER = new InterfaceSerializer<BlockChange>()
            .register("single", SingleBlockChange.SERIALIZER)
            .register("mass", MassBlockChange.SERIALIZER);
}
