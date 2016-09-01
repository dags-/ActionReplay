package me.dags.actionreplay.event;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.entity.EntitySnapshot;

import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class EntityChange implements Change {

    public EntityChange(List<EntitySnapshot> entities) {}

    @Override
    public void restore(Vector3i relative) {}

    @Override
    public void undo(Vector3i relative) {}

    @Override
    public int getContentVersion() {
        return 0;
    }

    @Override
    public DataContainer toContainer() {
        return versionedContainer()
                .set(Change.TYPE, Change.ENTITY);
    }
}
