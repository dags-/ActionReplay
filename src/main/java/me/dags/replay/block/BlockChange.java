package me.dags.replay.block;

import me.dags.replay.serialize.InterfaceSerializer;
import me.dags.replay.serialize.Serializer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockChange {

    void apply(Location<World> origin);

    Serializer<BlockChange> SERIALIZER = InterfaceSerializer.builder(BlockChange.class)
            .add("single", SingleBlockChange.class, SingleBlockChange.SERIALIZER)
            .add("mass", MassBlockChange.class, MassBlockChange.SERIALIZER)
            .build();
}
