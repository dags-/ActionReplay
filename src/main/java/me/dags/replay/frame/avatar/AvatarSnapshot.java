package me.dags.replay.frame.avatar;

import com.flowpowered.math.vector.Vector3d;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class AvatarSnapshot {

    private final UUID uuid;
    private final String name;
    private final Vector3d offset;
    private final Vector3d rotation;
    private final ItemStack held;
    private final boolean flying;

    public AvatarSnapshot(UUID uuid, String name, Vector3d offset, Vector3d rotation, ItemStack held, boolean flying) {
        this.uuid = uuid;
        this.name = name;
        this.offset = offset;
        this.rotation = rotation;
        this.held = held;
        this.flying = flying;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Avatar create(Location<World> origin) {
        try {
            Vector3d position = origin.getPosition().add(offset);
            Entity entity = origin.getExtent().createEntity(EntityTypes.HUMAN, position);
            entity.offer(Keys.SKIN_UNIQUE_ID, uuid);
            entity.offer(Keys.INVULNERABLE, true);
            entity.offer(Keys.IS_FLYING, flying);
            entity.offer(Keys.HAS_GRAVITY, !flying);
            entity.offer(Keys.DISPLAY_NAME, Text.of(name));

            if (origin.getExtent().spawnEntity(entity)) {
                return new Avatar((Human) entity);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return Avatar.NONE;
    }

    public void applyTo(Location<World> origin, Avatar avatar) {
        Location<World> location = origin.add(offset);
        avatar.getHuman().setLocationAndRotation(location, rotation);
        avatar.getHuman().setHeadRotation(rotation);
        avatar.getHuman().setItemInHand(HandTypes.MAIN_HAND, held);
        avatar.getHuman().offer(Keys.IS_FLYING, flying);
        avatar.setLocation(location);
    }

    public static final Serializer<AvatarSnapshot> SERIALIZER = new Serializer<AvatarSnapshot>() {
        @Override
        public void serialize(AvatarSnapshot avatar, Node node) {
            node.put("id", avatar.uuid.toString());
            node.put("name", avatar.name);
            node.put("flying", avatar.flying);
            node.put("held", serializeStack(avatar.held));
            node.put("x", "y", "z", avatar.offset);
            node.put("rx", "ry", "rz", avatar.rotation);
        }

        @Override
        public AvatarSnapshot deserialize(Node node) {
            String id = node.getString("id");
            String name = node.getString("name");
            boolean flying = node.getBool("flying");
            byte[] bytes = node.getBytes("held");
            ItemStack held = deserializeStack(bytes);
            Vector3d offset = node.getVec3d("x", "y", "z");
            Vector3d rotation = node.getVec3d("rx", "ry", "rz");
            return new AvatarSnapshot(UUID.fromString(id), name, offset, rotation, held, flying);
        }

        private byte[] serializeStack(ItemStack stack) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try {
                DataFormats.NBT.writeTo(out, stack.toContainer());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return out.toByteArray();
        }

        private ItemStack deserializeStack(byte[] bytes) {
            if (bytes.length == 0) {
                return ItemStack.empty();
            }

            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            try {
                DataContainer container = DataFormats.NBT.readFrom(in);
                return ItemStack.builder().fromContainer(container).build();
            } catch (IOException e) {
                e.printStackTrace();
                return ItemStack.empty();
            }
        }
    };
}
