package me.dags.actionreplay.event;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.Queries;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class BlockChange implements Change {

    static final DataQuery TRANSACTIONS = DataQuery.of("TRANSACTIONS");

    private final List<BlockTransaction> transactions;

    public BlockChange(List<BlockTransaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public void restore(Vector3i relative) {
        for (BlockTransaction transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.add(relative).setBlock(transaction.getTo().getExtendedState()));
        }
    }

    @Override
    public void undo(Vector3i relative) {
        for (BlockTransaction transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.add(relative).setBlock(transaction.getFrom().getExtendedState()));
        }
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return versionedContainer()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(BlockChange.TRANSACTIONS, transactions);
    }
}
