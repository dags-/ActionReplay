package me.dags.replay.frame.schematic;

import me.dags.replay.data.Serializer;
import me.dags.replay.data.TypedSerializer;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Schem extends Serializer.Type, OptionalValue {

    void apply(Location<World> location);

    byte[] getBytes();

    TypedSerializer<Schem> SERIALIZER = new TypedSerializer<Schem>()
            .register("sponge", SpongeSchematic.SERIALIZER);

    Schem NONE = new Schem() {
        @Override
        public void apply(Location<World> location) {

        }

        @Override
        public byte[] getBytes() {
            return new byte[0];
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
