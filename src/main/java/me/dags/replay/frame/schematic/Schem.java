package me.dags.replay.frame.schematic;

import me.dags.replay.data.Serializer;
import me.dags.replay.data.TypedSerializer;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Schem extends Serializer.Type {

    void apply(Location<World> location);

    @Override
    default boolean isPresent() {
        return this != NONE;
    }

    TypedSerializer<Schem> SERIALIZER = new TypedSerializer<>(Schem.NONE)
            .register("sponge", SpongeSchematic.SERIALIZER);

    Schem NONE = new Schem() {
        @Override
        public void apply(Location<World> location) {

        }

        @Override
        public String getType() {
            return "none";
        }

        @Override
        public boolean isPresent() {
            return false;
        }
    };
}
