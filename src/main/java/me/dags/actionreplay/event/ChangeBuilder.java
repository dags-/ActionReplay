package me.dags.actionreplay.event;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.text.Text;

import java.util.List;
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
            if (type.equals(Change.ENTITY)) {
                throw new UnsupportedOperationException("NOT SUPPORTED YET");
            }
            if (type.equals(Change.SIGN)) {
                return toSignChange(view);
            }
            return Optional.empty();
        });
    }

    private Optional<Change> toBlockChange(DataView view) throws InvalidDataException {
        return view.getSerializableList(BlockChange.TRANSACTIONS, BlockTransaction.class).map(BlockChange::new);
    }

    private Optional<Change> toSignChange(DataView view) throws InvalidDataException {
        Optional<BlockSnapshot> block = view.getSerializable(SignChange.BLOCK, BlockSnapshot.class);
        Optional<List<Text>> lines = view.getSerializableList(SignChange.LINES, Text.class);
        return Optional.ofNullable(block.isPresent() && lines.isPresent() ? new SignChange(block.get(), lines.get()) : null);
    }
}
