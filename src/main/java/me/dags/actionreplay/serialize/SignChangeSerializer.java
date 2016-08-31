package me.dags.actionreplay.serialize;

import com.google.common.reflect.TypeToken;
import me.dags.actionreplay.event.SignChange;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class SignChangeSerializer implements TypeSerializer<SignChange> {

    @Override
    public SignChange deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
        BlockSnapshot snapshot = node.getNode("block").getValue(Tokens.BLOCK_SNAPSHOT, BlockSnapshot.NONE);
        List<Text> lines = node.getNode("lines").getValue(Tokens.TEXT_LIST, Collections.emptyList());
        return new SignChange(snapshot, lines);
    }

    @Override
    public void serialize(TypeToken<?> type, SignChange signChange, ConfigurationNode node) throws ObjectMappingException {
        node.getNode("block").setValue(Tokens.BLOCK_SNAPSHOT, signChange.getBlockSnapshot());
        node.getNode("lines").setValue(Tokens.TEXT_LIST, signChange.getLines());
    }
}
