package me.dags.actionreplay.replay.avatar;

import com.flowpowered.math.vector.Vector3d;
import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.replay.Meta;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class AvatarInstance extends Avatar {

    private final UUID uuid;
    private WeakReference<Human> human = new WeakReference<>(null);

    public AvatarInstance(UUID uuid) {
        this.uuid = uuid;
    }

    public void sync(AvatarSnapshot snapshot, Location<World> relative) {
        Human human = getEntity(snapshot, relative);
        if (human != null && !human.isRemoved()) {
            if (snapshot.isTerminal()) {
                human.remove();
            } else {
                World world = relative.getExtent();
                Vector3d pos = relative.getPosition();
                Transform<World> transform = new Transform<>(world, snapshot.position.add(pos), snapshot.rotation);

                // ItemStack itemStack = snapshot.inHand.createStack();
                // human.setItemInHand(itemStack);
                human.setTransform(transform);
                human.setHeadRotation(snapshot.rotation);
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

    private Human getEntity(AvatarSnapshot snapshot, Location<World> relative) {
        Human human = this.human.get();
        if (human == null) {
            Entity entity = relative.getExtent().createEntity(EntityTypes.HUMAN, relative.getPosition());

            if (!Human.class.isInstance(entity)) {
                return null;
            }

            human = Human.class.cast(entity);
            human.offer(Keys.SKIN_UNIQUE_ID, snapshot.playerId);
            human.offer(Keys.DISPLAY_NAME, Text.of(snapshot.playerName));

            if (relative.getExtent().spawnEntity(human, ActionReplay.spawnCause())) {
                this.human = new WeakReference<>(human);
            }
        }
        return human;
    }

    public UUID getEntityId() {
        Human human = this.human.get();
        if (human != null) {
            return human.getUniqueId();
        }
        return Meta.DUMMY_ID;
    }

    @Override
    UUID getUUID() {
        return uuid;
    }
}
