package me.dags.actionreplay.event;

import me.dags.actionreplay.event.blockchange.BlockChange;
import me.dags.actionreplay.event.masschange.MassChangeBuilder;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.AbstractDataBuilder;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class ChangeBuilder extends AbstractDataBuilder<Change> {

    private static final Change.Builder[] builders = init();

    static Change.Builder[] init() {
        Change.Builder[] builders = new Change.Builder[2];
        builders[Ids.BLOCK_CHANGE] = new BlockChange.Builder();
        builders[Ids.MASS_CHANGE] = new MassChangeBuilder();
        return builders;
    }

    public ChangeBuilder() {
        super(Change.class, 0);
    }

    @Override
    public Optional<Change> buildContent(DataView view) throws InvalidDataException {
        Optional<Byte> id = view.getByte(Change.ID);
        if (id.isPresent() && id.get() < builders.length) {
            Change.Builder builder = builders[id.get()];
            if (builder != null) {
                return builder.from(view);
            }
        }
        return Optional.empty();
    }
}
