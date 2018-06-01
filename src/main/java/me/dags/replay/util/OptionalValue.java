package me.dags.replay.util;

/**
 * @author dags <dags@dags.me>
 */
public interface OptionalValue {

    boolean isPresent();

    default boolean isAbsent() {
        return !isPresent();
    }
}
