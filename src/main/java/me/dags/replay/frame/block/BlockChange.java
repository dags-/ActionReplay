package me.dags.replay.frame.block;

import me.dags.replay.data.Serializer;
import me.dags.replay.data.TypedSerializer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockChange extends Serializer.Type {

    void apply(Location<World> origin);

    @Override
    default boolean isPresent() {
        return this != NONE;
    }

    BlockChange NONE = new BlockChange() {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void apply(Location<World> origin) {

        }

        @Override
        public String getType() {
            return "none";
        }
    };

    TypedSerializer<BlockChange> SERIALIZER = new TypedSerializer<>(NONE)
            .register("single", SingleBlockChange.SERIALIZER)
            .register("mass", MassBlockChange.SERIALIZER);
}
