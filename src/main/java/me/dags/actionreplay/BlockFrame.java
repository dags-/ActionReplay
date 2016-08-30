package me.dags.actionreplay;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.block.ChangeBlockEvent;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BlockFrame extends KeyFrame.TargetAvatar {

    private final List<Transaction<BlockSnapshot>> transactions;

    public BlockFrame(Avatar avatar, ChangeBlockEvent event) {
        super(avatar);
        this.transactions = event.getTransactions();
    }

    @Override
    public void restore() {
        restoreBlocks();
    }

    @Override
    public void reset() {
        resetBlocks();
    }

    private void restoreBlocks() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getFinal().getExtendedState()));
        }
    }

    private void resetBlocks() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getOriginal().getExtendedState()));
        }
    }
}
