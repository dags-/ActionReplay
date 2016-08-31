package me.dags.actionreplay.event;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BlockChange implements Change {

    private final List<Transaction<BlockSnapshot>> transactions;

    public BlockChange(List<Transaction<BlockSnapshot>> transactions) {
        this.transactions = transactions;
    }

    public Iterable<Transaction<BlockSnapshot>> getTransactions() {
        return transactions;
    }

    @Override
    public void restore(Vector3i relative) {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.add(relative).setBlock(transaction.getFinal().getExtendedState()));
        }
    }

    @Override
    public void undo(Vector3i relative) {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.add(relative).setBlock(transaction.getOriginal().getExtendedState()));
        }
    }
}
