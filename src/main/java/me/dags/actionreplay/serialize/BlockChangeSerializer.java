package me.dags.actionreplay.serialize;

import com.google.common.reflect.TypeToken;
import me.dags.actionreplay.event.BlockChange;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BlockChangeSerializer implements TypeSerializer<BlockChange> {

    @Override
    public BlockChange deserialize(TypeToken<?> typeToken, ConfigurationNode node) throws ObjectMappingException {
        List<Transaction<BlockSnapshot>> transactions = new ArrayList<>();
        for (ConfigurationNode child : node.getChildrenList()) {
            BlockSnapshot from = child.getNode("from").getValue(Tokens.BLOCK_SNAPSHOT, BlockSnapshot.NONE);
            BlockSnapshot to = child.getNode("to").getValue(Tokens.BLOCK_SNAPSHOT, BlockSnapshot.NONE);
            Transaction<BlockSnapshot> transaction = new Transaction<>(from, to);
            transactions.add(transaction);
        }
        return new BlockChange(Collections.unmodifiableList(transactions));
    }

    @Override
    public void serialize(TypeToken<?> typeToken, BlockChange blockChange, ConfigurationNode node) throws ObjectMappingException {
        List<ConfigurationNode> transactions = new ArrayList<>();
        for (Transaction<BlockSnapshot> transaction : blockChange.getTransactions()) {
            ConfigurationNode child = SimpleConfigurationNode.root();
            child.getNode("from").setValue(Tokens.BLOCK_SNAPSHOT, transaction.getDefault());
            child.getNode("to").setValue(Tokens.BLOCK_SNAPSHOT, transaction.getFinal());
            transactions.add(child);
        }
        node.setValue(transactions);
    }
}
