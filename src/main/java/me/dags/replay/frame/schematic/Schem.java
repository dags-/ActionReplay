package me.dags.replay.frame.schematic;

import me.dags.replay.serialize.InterfaceSerializer;
import me.dags.replay.serialize.Typed;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Schem extends Typed, OptionalValue {

    void apply(Location<World> location);

    byte[] getBytes();

    InterfaceSerializer<Schem> SERIALIZER = new InterfaceSerializer<Schem>()
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
