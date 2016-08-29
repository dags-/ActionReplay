package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Avatar {

    private final WeakReference<Player> reference;
    private final UUID uuid;
    private Human avatar;

    public Avatar(Player player) {
        this.reference = new WeakReference<>(player);
        this.uuid = player.getUniqueId();
    }

    public Transform<World> getTransform() {
        Player player = getPlayer();
        return player != null ? player.getTransform() : null;
    }

    public Player getPlayer() {
        Player player = this.reference.get();
        if (player == null) {
            remove();
        }
        return player;
    }

    public void updateAvatar(Transform<World> transform) {
        initAvatar(transform);

        if (avatar != null) {
            avatar.setTransform(transform);
            avatar.offer(Keys.INVULNERABILITY_TICKS, Integer.MAX_VALUE);
            avatar.offer(Keys.FALL_DISTANCE, 0F);
            avatar.offer(Keys.FALL_TIME, 0);
        }
    }

    public void pause() {
        if (avatar != null) {
            avatar.setVelocity(Vector3d.ZERO);
        }
    }

    public void remove() {
        if (avatar != null) {
            avatar.remove();
            avatar = null;
        }
    }

    private void initAvatar(Transform<World> transform) {
        if (avatar == null) {
            Player player = getPlayer();

            if (player == null) {
                return;
            }

            Location<World> location = transform.getLocation();
            avatar = location.getExtent().createEntity(EntityTypes.HUMAN, location.getPosition())
                    .filter(Human.class::isInstance)
                    .map(Human.class::cast)
                    .orElse(null);

            avatar.offer(Keys.DISPLAY_NAME, Text.of(player.getName()));
            avatar.offer(Keys.INVULNERABILITY_TICKS, Integer.MAX_VALUE);
            avatar.offer(Keys.SKIN_UNIQUE_ID, player.getUniqueId());
            avatar.offer(Keys.CAN_FLY, true);

            if (!location.getExtent().spawnEntity(avatar, ActionReplay.spawnCause())) {
                avatar = null;
            }
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other != null && (other instanceof Avatar && other.hashCode() == hashCode());
    }
}
