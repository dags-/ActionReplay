package me.dags.actionreplay.event;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.*;

/**
 * @author dags <dags@dags.me>
 */
public interface Change extends DataSerializable {

    DataQuery TYPE = DataQuery.of("CHANGE_TYPE");
    String BLOCK = "block";
    String ENTITY = "entity";
    String SIGN = "sign";

    void restore(Vector3i relative);

    void undo(Vector3i relative);

    default DataContainer versionedContainer() {
        return new MemoryDataContainer().set(Queries.CONTENT_VERSION, getContentVersion());
    }
}
