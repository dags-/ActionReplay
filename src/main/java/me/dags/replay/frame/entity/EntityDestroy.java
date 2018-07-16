package me.dags.replay.frame.entity;

import java.util.UUID;
import me.dags.replay.data.Serializer;
import me.dags.replay.data.Serializers;
import org.jnbt.CompoundTag;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class EntityDestroy implements EntityChange {

    private final UUID id;

    public EntityDestroy(UUID id) {
        this.id = id;
    }

    @Override
    public void apply(Location<World> origin) {
        origin.getExtent().getEntity(EntityTracker.lookup(id)).ifPresent(Entity::remove);
    }

    @Override
    public String getType() {
        return "entity.destroy";
    }

    public static Serializer<EntityDestroy> SERIALIZER = new Serializer<EntityDestroy>() {
        @Override
        public EntityDestroy deserialize(CompoundTag root) {
            UUID uuid = Serializers.uuid(root, "id0", "id1");
            return new EntityDestroy(uuid);
        }

        @Override
        public void serialize(EntityDestroy destroy, CompoundTag root) {
            Serializers.uuid(root, "id0", "id1", destroy.id);
        }
    };
}
