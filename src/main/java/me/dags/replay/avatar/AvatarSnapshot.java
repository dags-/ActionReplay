package me.dags.replay.avatar;

import com.flowpowered.math.vector.Vector3d;
import com.sk89q.jnbt.CompoundTag;
import java.util.UUID;
import me.dags.replay.util.CompoundBuilder;
import me.dags.replay.serialize.Serializer;
import me.dags.replay.serialize.Serializers;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class AvatarSnapshot {

    private final UUID uuid;
    private final Vector3d offset;
    private final Vector3d rotation;
    private final ItemStack held;
    private final boolean flying;

    public AvatarSnapshot(UUID uuid, Vector3d offset, Vector3d rotation, ItemStack held, boolean flying) {
        this.uuid = uuid;
        this.offset = offset;
        this.rotation = rotation;
        this.held = held;
        this.flying = flying;
    }

    public Avatar create(Location<World> origin) {
        Vector3d position = origin.getPosition().add(offset);
        Entity entity = origin.getExtent().createEntityNaturally(EntityTypes.HUMAN, position);
        entity.offer(Keys.SKIN_UNIQUE_ID, uuid);
        entity.offer(Keys.IS_FLYING, flying);

        if (origin.getExtent().spawnEntity(entity)) {
            return new Avatar((Human) entity);
        }

        return Avatar.NONE;
    }

    public void applyTo(Location<World> origin, Avatar avatar) {
        Location<World> location = origin.add(offset);
        avatar.getHuman().setLocationAndRotation(location, rotation);
        avatar.getHuman().setItemInHand(HandTypes.MAIN_HAND, held);
        avatar.getHuman().offer(Keys.IS_FLYING, flying);
        avatar.setLocation(location);
    }

    public UUID getUUID() {
        return uuid;
    }

    public ItemStack getHeld() {
        return held;
    }

    public Vector3d getOffset() {
        return offset;
    }

    public Vector3d getRotation() {
        return rotation;
    }

    public static final Serializer<AvatarSnapshot> SERIALIZER = new Serializer<AvatarSnapshot>() {
        @Override
        public void serialize(AvatarSnapshot avatar, CompoundBuilder builder) {
            builder.put("id", avatar.uuid.toString());
            builder.put("flying", avatar.flying);
            Serializers.itemStack(builder, "held", avatar.held);
            Serializers.vector3d(builder, avatar.offset, "x", "y", "z");
            Serializers.vector3d(builder, avatar.rotation, "rx", "ry", "rz");
        }

        @Override
        public AvatarSnapshot deserialize(CompoundTag tag) {
            String id = tag.getString("id");
            int flying = tag.getByte("flying");
            UUID uuid = UUID.fromString(id);
            ItemStack held = Serializers.itemStack(tag, "held");
            Vector3d offset = Serializers.vector3d(tag, "x", "y", "z");
            Vector3d rotation = Serializers.vector3d(tag, "rx", "ry", "rz");
            return new AvatarSnapshot(uuid, offset, rotation, held, flying == 1);
        }
    };
}
