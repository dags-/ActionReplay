package me.dags.replay.frame;

import me.dags.replay.data.Serializer;
import me.dags.replay.data.TypedSerializer;
import me.dags.replay.frame.block.MassBlockChange;
import me.dags.replay.frame.block.SingleBlockChange;
import me.dags.replay.frame.entity.EntityCreate;
import me.dags.replay.frame.entity.EntityDestroy;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Change extends Serializer.Type {

    void apply(Location<World> origin);

    Change NONE = new Change() {
        @Override
        public void apply(Location<World> origin) {

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

    TypedSerializer<Change> SERIALIZER = new TypedSerializer<>(NONE)
            .register("block.single", SingleBlockChange.SERIALIZER)
            .register("block.mass", MassBlockChange.SERIALIZER)
            .register("entity.create", EntityCreate.SERIALIZER)
            .register("entity.destroy", EntityDestroy.SERIALIZER);
}
