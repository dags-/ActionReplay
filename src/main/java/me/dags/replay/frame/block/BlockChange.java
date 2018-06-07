package me.dags.replay.frame.block;

import javax.annotation.Nonnull;
import me.dags.replay.data.Serializer;
import me.dags.replay.data.TypedSerializer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockChange extends Serializer.Type {

    @Nonnull
    void apply(Location<World> origin);

    TypedSerializer<BlockChange> SERIALIZER = new TypedSerializer<BlockChange>()
            .register("single", SingleBlockChange.SERIALIZER)
            .register("mass", MassBlockChange.SERIALIZER);
}
