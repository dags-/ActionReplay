package me.dags.actionreplay.serialize;

import com.google.common.reflect.TypeToken;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

/**
 * @author dags <dags@dags.me>
 */
public class AvatarSnapshotSerializer implements TypeSerializer<AvatarSnapshot> {

    @Override
    public AvatarSnapshot deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
        AvatarSnapshot.Mutable mutable = AvatarSnapshot.mutable();
        mutable.worldId = node.getNode("world_id").getValue(Tokens.UUID, mutable.worldId);
        mutable.playerId = node.getNode("player_id").getValue(Tokens.UUID, mutable.playerId);
        mutable.playerName = node.getNode("player_name").getString("");
        mutable.position = node.getNode("position").getValue(Tokens.VECTOR_3D, mutable.position);
        mutable.rotation = node.getNode("rotation").getValue(Tokens.VECTOR_3D, mutable.rotation);
        mutable.inHand = node.getNode("item").getValue(Tokens.ITEM_STACK_SNAPSHOT, mutable.inHand);
        return mutable.build();
    }

    @Override
    public void serialize(TypeToken<?> typeToken, AvatarSnapshot snapshot, ConfigurationNode node) throws ObjectMappingException {
        node.getNode("world_id").setValue(Tokens.UUID, snapshot.worldId);
        node.getNode("player_id").setValue(Tokens.UUID, snapshot.playerId);
        node.getNode("player_name").setValue(snapshot.playerName);
        node.getNode("position").setValue(Tokens.VECTOR_3D, snapshot.position);
        node.getNode("rotation").setValue(Tokens.VECTOR_3D, snapshot.rotation);
        node.getNode("item").setValue(Tokens.ITEM_STACK_SNAPSHOT, snapshot.inHand);
    }
}
