package me.dags.actionreplay.avatar;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;

import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class AvatarSnapshot implements DataSerializable {

    private static final DataQuery PLAYER_ID = DataQuery.of("PLAYER_ID");
    private static final DataQuery PLAYER_NAME = DataQuery.of("PLAYER_NAME");
    private static final DataQuery POSITION = DataQuery.of("POSITION");
    private static final DataQuery ROTATION = DataQuery.of("ROTATION");
    private static final DataQuery ITEM = DataQuery.of("ITEM");

    final UUID playerId;
    final String playerName;
    final Vector3d position;
    final Vector3d rotation;
    final ItemStackSnapshot inHand;
    private final boolean terminal;

    private AvatarSnapshot(UUID id) {
        playerId = id;
        playerName = "";
        position = Vector3d.ZERO;
        rotation = Vector3d.ZERO;
        inHand = ItemStackSnapshot.NONE;
        terminal = true;
    }

    private AvatarSnapshot(Mutable mutable) {
        this.playerId = mutable.playerId;
        this.playerName = mutable.playerName;
        this.position = mutable.position;
        this.rotation = mutable.rotation;
        this.inHand = mutable.inHand;
        this.terminal = false;
    }

    public AvatarSnapshot(Player player, Vector3d relative) {
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

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(PLAYER_ID, playerId.toString())
                .set(PLAYER_NAME, playerName)
                .set(POSITION, position)
                .set(ROTATION, rotation)
                .set(ITEM, inHand);
    }

    private static class Mutable {

        UUID worldId = UUID.randomUUID();
        UUID playerId = UUID.randomUUID();
        String playerName = "";
        Vector3d position = Vector3d.ZERO;
        Vector3d rotation = Vector3d.ZERO;
        ItemStackSnapshot inHand = ItemStackSnapshot.NONE;

        AvatarSnapshot build() {
            return new AvatarSnapshot(this);
        }
    }

    public static class Builder extends AbstractDataBuilder<AvatarSnapshot> {

        public Builder() {
            super(AvatarSnapshot.class, 0);
        }

        @Override
        public Optional<AvatarSnapshot> buildContent(DataView container) throws InvalidDataException {
            Optional<UUID> playerId = container.getString(PLAYER_ID).map(UUID::fromString);
            Optional<String> playerName = container.getString(PLAYER_NAME);
            Optional<Vector3d> position = container.getObject(POSITION, Vector3d.class);
            Optional<Vector3d> rotation = container.getObject(ROTATION, Vector3d.class);
            Optional<ItemStackSnapshot> item = container.getSerializable(ITEM, ItemStackSnapshot.class);
            if (playerId.isPresent() && playerName.isPresent() && position.isPresent() && rotation.isPresent() && item.isPresent()) {
                Mutable mutable = new Mutable();
                mutable.playerId = playerId.get();
                mutable.playerName = playerName.get();
                mutable.position = position.get();
                mutable.rotation = rotation.get();
                mutable.inHand = item.get();
                return Optional.of(mutable.build());
            }
            return Optional.empty();
        }
    }
}
