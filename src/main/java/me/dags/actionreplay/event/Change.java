package me.dags.actionreplay.event;

import org.spongepowered.api.data.*;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Change extends DataSerializable {

    DataQuery TYPE = DataQuery.of("CHANGE_TYPE");
    String BLOCK = "block";
    String ENTITY = "entity";
    String SIGN = "sign";

    void restore(Location<World> location);

    void undo(Location<World> location);

    default DataContainer versionedContainer() {
        return new MemoryDataContainer().set(Queries.CONTENT_VERSION, getContentVersion());
    }
}
