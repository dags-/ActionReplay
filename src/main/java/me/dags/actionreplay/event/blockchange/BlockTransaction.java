package me.dags.actionreplay.event.blockchange;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.event.Positional;
import me.dags.actionreplay.event.Transactional;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataSerializable;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.MemoryDataContainer;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class BlockTransaction  implements DataSerializable, Positional, Transactional {

    private final Vector3i position;
    private final BlockState from;
    private final BlockState to;

    public BlockTransaction(Vector3i position, BlockState from, BlockState to) {
        this.position = position;
        this.from = from;
        this.to = to;
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    public Vector3i getPosition() {
        return position;
    }

    public BlockState getFrom() {
        return from;
    }

    public BlockState getTo() {
        return to;
    }

    @Override
    public DataContainer toContainer() {
        return new MemoryDataContainer()
                .set(POS, BlockTransaction.vecTo(this.position))
                .set(FROM, getFrom())
                .set(TO, getTo());
    }

    private static DataContainer vecTo(Vector3i vector3i) {
        DataContainer container = new MemoryDataContainer();
        container.set(X, vector3i.getX());
        container.set(Y, vector3i.getY());
        container.set(Z, vector3i.getZ());
        return container;
    }

    private static Optional<Vector3i> vecFrom(DataView view) {
        Optional<Integer> x = view.getInt(X);
        Optional<Integer> y = view.getInt(Y);
        Optional<Integer> z = view.getInt(Z);
        if (x.isPresent() && y.isPresent() && z.isPresent()) {
            return Optional.of(new Vector3i(x.get(), y.get(), z.get()));
        }
        return Optional.empty();
    }

    public static class Builder extends AbstractDataBuilder<BlockTransaction> {

        public Builder() {
            super(BlockTransaction.class, 0);
        }

        @Override
        protected Optional<BlockTransaction> buildContent(DataView view) throws InvalidDataException {
            Optional<Vector3i> pos = view.getView(POS).flatMap(BlockTransaction::vecFrom);
            Optional<BlockState> from = view.getSerializable(FROM, BlockState.class);
            Optional<BlockState> to = view.getSerializable(TO, BlockState.class);
            if (pos.isPresent() && from.isPresent() && to.isPresent()) {
                return Optional.of(new BlockTransaction(pos.get(), from.get(), to.get()));
            }
            return Optional.empty();
        }
    }
}
