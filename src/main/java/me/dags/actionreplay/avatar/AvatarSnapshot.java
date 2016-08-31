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

    private AvatarSnapshot(Mutable mutable) {
        this.worldId = mutable.worldId;
        this.playerId = mutable.playerId;
        this.playerName = mutable.playerName;
        this.position = mutable.position;
        this.rotation = mutable.rotation;
        this.inHand = mutable.inHand;
        this.terminal = false;
    }

    public AvatarSnapshot(Player player, Vector3d relative) {
        worldId = player.getWorld().getUniqueId();
        playerId = player.getUniqueId();
        playerName = player.getName();
        position = player.getTransform().getPosition().sub(relative);
        rotation = player.getTransform().getRotation();
        inHand = player.getItemInHand().map(ItemStack::createSnapshot).orElse(ItemStackSnapshot.NONE);
        terminal = false;
    }

    public UUID getUUID() {
        return playerId;
    }

    public AvatarSnapshot getUpdatedCopy(Vector3d relative) {
        return Sponge.getServer().getPlayer(playerId)
                .map(player -> new AvatarSnapshot(player, relative))
                .orElse(new AvatarSnapshot(playerId));
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

    public static Mutable mutable() {
        return new Mutable();
    }

    public static class Mutable {

        public UUID worldId = UUID.randomUUID();
        public UUID playerId = UUID.randomUUID();
        public String playerName = "";
        public Vector3d position = Vector3d.ZERO;
        public Vector3d rotation = Vector3d.ZERO;
        public ItemStackSnapshot inHand = ItemStackSnapshot.NONE;

        public AvatarSnapshot build() {
            return new AvatarSnapshot(this);
        }
    }
}
