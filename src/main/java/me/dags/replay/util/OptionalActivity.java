package me.dags.replay.util;

/**
 * @author dags <dags@dags.me>
 */
public interface OptionalActivity extends OptionalValue {

    String getName();

    boolean isActive();
}
