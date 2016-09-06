package me.dags.actionreplay.event.masschange;

import me.dags.actionreplay.event.Change;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class MassChangeBuilder implements Change.Builder {

    private static final MassChange.Builder[] builders = new MassChange.Builder[1];

    public static void register(int id, MassChange.Builder builder) {
        if (id < builders.length && builders[id] == null) {
            builders[id] = builder;
        }
    }

    @Override
    public Optional<Change> from(DataView view) throws InvalidDataException {
        Optional<Byte> id = view.getByte(MassChange.MASS_ID);
        if (id.isPresent() && id.get() < builders.length) {
            MassChange.Builder builder = builders[id.get()];
            if (builder != null) {
                return builder.from(view);
            }
        }
        return Optional.empty();
    }
}
