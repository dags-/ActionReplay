package me.dags.actionreplay.event;

import org.spongepowered.api.data.*;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public interface Change extends DataSerializable {

    DataQuery ID = DataQuery.of("ID");

    void restore(Location<World> location);

    void undo(Location<World> location);

    byte getId();

    default DataContainer versionedContainer() {
        return new MemoryDataContainer().set(Change.ID, getId());
    }

    interface Builder {

        Optional<Change> from(DataView view) throws InvalidDataException;
    }
}
