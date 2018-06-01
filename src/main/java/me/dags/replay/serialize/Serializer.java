package me.dags.replay.serialize;

import com.sk89q.jnbt.CompoundTag;
import me.dags.replay.util.CompoundBuilder;
import me.dags.replay.util.OptionalValue;

/**
 * @author dags <dags@dags.me>
 */
public interface Serializer<T> extends OptionalValue {

    void serialize(T t, CompoundBuilder builder);

    T deserialize(CompoundTag tag);

    @Override
    default boolean isPresent() {
        return true;
    }

    Serializer NONE = new Serializer() {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void serialize(Object o, CompoundBuilder builder) {

        }

        @Override
        public Object deserialize(CompoundTag tag) {
            return null;
        }
    };
}