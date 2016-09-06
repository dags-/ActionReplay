package me.dags.actionreplay.replay.avatar;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Avatar {

    abstract UUID getUUID();

    @Override
    public int hashCode() {
        return getUUID().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof Avatar && o.hashCode() == hashCode();
    }
}
