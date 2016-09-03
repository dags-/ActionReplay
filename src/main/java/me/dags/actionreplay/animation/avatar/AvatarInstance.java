package me.dags.actionreplay.animation.avatar;

import com.flowpowered.math.vector.Vector3d;
import me.dags.actionreplay.ActionReplay;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class AvatarInstance {

    private static final UUID DUMMY = UUID.fromString("00000000-0000-0000-0000-000000000000");

    private final UUID uuid;
    private WeakReference<World> world = new WeakReference<>(null);
    private WeakReference<Human> human = new WeakReference<>(null);

    public AvatarInstance(UUID uuid) {
        this.uuid = uuid;
    }

    public void sync(AvatarSnapshot snapshot, Vector3d relative) {
        Human human = getEntity(snapshot, relative);
        World world = getWorld(snapshot);
        if (human != null && !human.isRemoved() && world != null) {
            if (snapshot.isTerminal()) {
                human.remove();
            } else {
                Transform<World> transform = new Transform<>(world, snapshot.position.add(relative), snapshot.rotation);
                ItemStack itemStack = snapshot.inHand.createStack();

                human.setTransform(transform);
                human.setHeadRotation(snapshot.rotation);
                human.setItemInHand(itemStack);
            }
        }
    }

    public void pause() {
        Human human = this.human.get();
        if (human != null && !human.isRemoved()) {
            human.setVelocity(Vector3d.ZERO);
        }
    }

    public void remove() {
        Human human = this.human.get();
        if (human != null && !human.isRemoved()) {
            human.remove();
            this.human = new WeakReference<>(null);
        }
    }

    private Human getEntity(AvatarSnapshot snapshot, Vector3d relative) {
        Human human = this.human.get();
        World world = getWorld(snapshot);
        if (human == null && world != null) {
            human = world.createEntity(EntityTypes.HUMAN, snapshot.position.add(relative))
                    .filter(Human.class::isInstance)
                    .map(Human.class::cast)
                    .orElse(null);

            human.offer(Keys.SKIN_UNIQUE_ID, snapshot.playerId);
            human.offer(Keys.DISPLAY_NAME, Text.of(snapshot.playerName));

            if (world.spawnEntity(human, ActionReplay.spawnCause())) {
                this.human = new WeakReference<>(human);
            }
        }
        return human;
    }

    private World getWorld(AvatarSnapshot snapshot) {
        World world = this.world.get();
        if (world == null) {
            world = Sponge.getServer().getWorld(snapshot.worldId).orElse(null);
            this.world = new WeakReference<>(world);
        }
        return world;
    }

    public UUID getEntityId() {
        Human human = this.human.get();
        if (human != null) {
            return human.getUniqueId();
        }
        return DUMMY;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof AvatarInstance && o.hashCode() == hashCode();
    }
}
