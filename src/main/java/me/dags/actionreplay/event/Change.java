package me.dags.actionreplay.event;

import com.flowpowered.math.vector.Vector3i;

/**
 * @author dags <dags@dags.me>
 */
public interface Change {

    String BLOCK = "block";
    String ENTITY = "entity";
    String SIGN = "sign";

    void restore(Vector3i relative);

    void undo(Vector3i relative);
}
