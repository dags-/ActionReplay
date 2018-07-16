package me.dags.replay.data;

import me.dags.replay.util.OptionalValue;
import org.jnbt.CompoundTag;
import org.jnbt.Nbt;

/**
 * @author dags <dags@dags.me>
 */
public interface Serializer<T extends OptionalValue> {

    T deserialize(CompoundTag root);

    void serialize(T t, CompoundTag root);

    default CompoundTag serialize(T t) {
        CompoundTag root = Nbt.compound();
        serialize(t, root);
        return root;
    }

    interface Type extends OptionalValue {

        String getType();
    }
}
