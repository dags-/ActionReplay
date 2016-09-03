package me.dags.actionreplay.event;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

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
    public void restore(Location<World> location) {
        for (BlockTransaction transaction : transactions) {
            Location<World> loc = location.add(transaction.getPosition());
            if (validLocation(loc)) {
                loc.setBlock(transaction.getTo());
            }
        }
    }

    @Override
    public void undo(Location<World> location) {
        for (BlockTransaction transaction : transactions) {
            Location<World> loc = location.add(transaction.getPosition());
            if (validLocation(loc)) {
                loc.setBlock(transaction.getFrom());
            }
        }
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return versionedContainer()
                .set(BlockChange.TYPE, Change.BLOCK)
                .set(BlockChange.TRANSACTIONS, transactions);
    }

    public boolean validLocation(Location<World> location) {
        return location.getBlockY() >= 0 && location.getBlockY() < 256;
    }
}
