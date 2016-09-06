package me.dags.actionreplay.event.blockchange;

import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.Ids;
import me.dags.actionreplay.event.Transactional;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BlockChange implements Change, Transactional {

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
    public byte getId() {
        return Ids.BLOCK_CHANGE;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return versionedContainer()
                .set(BlockChange.TRANSACTIONS, transactions);
    }

    private boolean validLocation(Location<World> location) {
        return location.getBlockY() >= 0 && location.getBlockY() < 256;
    }

    public static class Builder implements Change.Builder {

        @Override
        public Optional<Change> from(DataView view) {
            return view.getSerializableList(TRANSACTIONS, BlockTransaction.class).map(BlockChange::new);
        }
    }
}
