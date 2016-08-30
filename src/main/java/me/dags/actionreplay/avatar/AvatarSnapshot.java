package me.dags.actionreplay.avatar;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class AvatarSnapshot {

    public final UUID worldId;
    public final UUID playerId;
    public final String playerName;
    public final Vector3d position;
    public final Vector3d rotation;
    public final ItemStackSnapshot inHand;
    private final boolean terminal;

    private AvatarSnapshot(UUID id) {
        worldId = UUID.randomUUID();
        playerId = id;
        playerName = "";
        position = Vector3d.ZERO;
        rotation = Vector3d.ZERO;
        inHand = ItemStackSnapshot.NONE;
        terminal = true;
    }

    public AvatarSnapshot(Player player) {
        worldId = player.getWorld().getUniqueId();
        playerId = player.getUniqueId();
        playerName = player.getName();
        position = player.getTransform().getPosition();
        rotation = player.getTransform().getRotation();
        inHand = player.getItemInHand().map(ItemStack::createSnapshot).orElse(ItemStackSnapshot.NONE);
        terminal = false;
    }

    public UUID getUUID() {
        return playerId;
    }

    public AvatarSnapshot getUpdatedCopy() {
        return Sponge.getServer().getPlayer(playerId).map(AvatarSnapshot::new).orElse(new AvatarSnapshot(playerId));
    }

    public boolean isTerminal() {
        return terminal;
    }

    @Override
    public int hashCode() {
        return playerId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof AvatarSnapshot && o.hashCode() == hashCode();
    }
}
