package me.dags.replay.serialize;

import com.sk89q.jnbt.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class InterfaceSerializer<T extends Typed> implements Serializer<T> {

    private final Map<String, Serializer<? extends T>> serializers = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(T t, TagBuilder builder) {
        Serializer serializer = serializers.get(t.getType());
        if (serializer != null) {
            serializer.serialize(t, builder);
            builder.put("_type", t.getType());
        }
    }

    @Override
    public T deserialize(CompoundTag tag) {
        String type = tag.getString("_type");
        Serializer<? extends T> serializer = serializers.get(type);
        if (serializer != null) {
            return serializer.deserialize(tag);
        }
        return null;
    }

    public InterfaceSerializer<T> register(String type, Serializer<? extends T> serializer) {
        serializers.put(type, serializer);
        return this;
    }
}
