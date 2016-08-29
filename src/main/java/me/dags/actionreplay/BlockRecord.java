package me.dags.actionreplay;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BlockRecord {

    private final List<Transaction<BlockSnapshot>> transactions;

    public BlockRecord(List<Transaction<BlockSnapshot>> transactions) {
        this.transactions = transactions;
    }

    public void restore() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation().ifPresent(loc -> loc.setBlock(transaction.getFinal().getExtendedState()));
        }
    }

    public void reset() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation().ifPresent(loc -> loc.setBlock(transaction.getOriginal().getExtendedState()));
        }
    }
}
