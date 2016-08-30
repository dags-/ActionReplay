package me.dags.actionreplay.event;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BlockChange implements Change {

    private final List<Transaction<BlockSnapshot>> transactions;

    public BlockChange(ChangeBlockEvent event) {
        this.transactions = event.getTransactions();
    }

    @Override
    public void restore() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getFinal().getExtendedState()));
        }
    }

    @Override
    public void undo() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getOriginal().getExtendedState()));
        }
    }
}
