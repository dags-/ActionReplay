package me.dags.actionreplay.event;

import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class ChangeBuilder extends AbstractDataBuilder<Change> {

    public ChangeBuilder() {
        super(Change.class, 0);
    }

    @Override
    public Optional<Change> buildContent(DataView view) throws InvalidDataException {
        return view.getString(Change.TYPE).flatMap(type -> {
            if (type.equals(Change.BLOCK)) {
                return toBlockChange(view);
            }
            return Optional.empty();
        });
    }

    private Optional<Change> toBlockChange(DataView view) throws InvalidDataException {
        return view.getSerializableList(BlockChange.TRANSACTIONS, BlockTransaction.class).map(BlockChange::new);
    }
}
