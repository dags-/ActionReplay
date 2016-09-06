package me.dags.actionreplay.event;

import org.spongepowered.api.data.DataQuery;

/**
 * @author dags <dags@dags.me>
 */
public interface Positional {

    DataQuery POS = DataQuery.of("POS");
    DataQuery X = DataQuery.of("X");
    DataQuery Y = DataQuery.of("Y");
    DataQuery Z = DataQuery.of("Z");
}
