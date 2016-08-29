package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.Human;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Avatar {

    private final Player player;
    private Human avatar;

    public Avatar(Player player) {
        this.player = player;
    }

    public PositionRecord getPosition() {
        return new PositionRecord(player);
    }

    public void suspend() {
        if (avatar != null) {
            avatar.setVelocity(Vector3d.ZERO);
        }
    }

    public void updateAvatar(PositionRecord positionRecord) {
        if (avatar == null) {
            Location<World> location = positionRecord.getLocation();
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
        if (avatar != null) {
            positionRecord.apply(avatar);
            avatar.offer(Keys.INVULNERABILITY_TICKS, Integer.MAX_VALUE);
            avatar.offer(Keys.FALL_DISTANCE, 0F);
            avatar.offer(Keys.FALL_TIME, 0);
        }
    }

    public void reset() {
        if (avatar != null) {
            avatar.remove();
            avatar = null;
        }
    }

    @Override
    public int hashCode() {
        return player.getUniqueId().hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return other != null && (other instanceof UUID && other.equals(player.getUniqueId())) || (other instanceof Avatar && other.hashCode() == hashCode());
    }
}
