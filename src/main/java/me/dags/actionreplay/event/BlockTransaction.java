package me.dags.actionreplay.event;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BlockTransaction extends Transaction<BlockSnapshot> implements DataSerializable {

    private static final DataQuery FROM = DataQuery.of("FROM");
    private static final DataQuery TO = DataQuery.of("TO");

    public BlockTransaction(BlockSnapshot original, BlockSnapshot defaultReplacement) {
        super(original, defaultReplacement);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        final DataContainer container = new MemoryDataContainer()
                .set(Queries.CONTENT_VERSION, getContentVersion())
                .set(FROM, this.getOriginal())
                .set(TO, this.getFinal());
        return container;
    }

    public static class Builder extends AbstractDataBuilder<BlockTransaction> {

        public Builder() {
            super(BlockTransaction.class, 0);
        }

        @Override
        protected Optional<BlockTransaction> buildContent(DataView view) throws InvalidDataException {
            Optional<BlockSnapshot> from = view.getSerializable(FROM, BlockSnapshot.class);
            Optional<BlockSnapshot> to = view.getSerializable(TO, BlockSnapshot.class);
            return Optional.ofNullable(from.isPresent() && to.isPresent() ? new BlockTransaction(from.get(), to.get()) : null);
        }
    }
}
