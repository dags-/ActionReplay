package me.dags.actionreplay.event;

import org.spongepowered.api.data.DataQuery;

/**
 * @author dags <dags@dags.me>
 */
public interface Transactional {

    DataQuery TRANSACTIONS = DataQuery.of("TRANSACTIONS");
    DataQuery FROM = DataQuery.of("FROM");
    DataQuery TO = DataQuery.of("TO");
}
