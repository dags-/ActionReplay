package me.dags.replay.frame.entity;

import me.dags.replay.data.TypedSerializer;
import me.dags.replay.frame.Change;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface EntityChange extends Change {

    void apply(Location<World> origin);

    @Override
    default boolean isPresent() {
        return this != NONE;
    }

    EntityChange NONE = new EntityChange() {
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

    TypedSerializer<EntityChange> SERIALIZER = new TypedSerializer<>(NONE)
            .register("create", EntityCreate.SERIALIZER)
            .register("destroy", EntityDestroy.SERIALIZER);
}
