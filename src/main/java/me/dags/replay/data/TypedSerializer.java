package me.dags.replay.data;

import java.util.HashMap;
import java.util.Map;
import org.jnbt.CompoundTag;

/**
 * @author dags <dags@dags.me>
 */
public class TypedSerializer<T extends Serializer.Type> implements Serializer<T> {

    private final Map<String, Serializer<? extends T>> serializers = new HashMap<>();
    private final T empty;

    public TypedSerializer(T empty) {
        this.empty = empty;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(T t, CompoundTag root) {
        Serializer serializer = serializers.get(t.getType());
        if (serializer != null) {
            serializer.serialize(t, root);
            root.put("_type", t.getType());
        }
    }

    @Override
    public T deserialize(CompoundTag root) {
        String type = root.getString("_type");
        Serializer<? extends T> serializer = serializers.get(type);
        if (serializer != null) {
            return serializer.deserialize(root);
        }
        return empty;
    }

    public TypedSerializer<T> register(String type, Serializer<? extends T> serializer) {
        serializers.put(type, serializer);
        return this;
    }
}
