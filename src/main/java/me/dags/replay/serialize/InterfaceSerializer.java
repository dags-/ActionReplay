package me.dags.replay.serialize;

import com.sk89q.jnbt.CompoundTag;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import me.dags.replay.util.CompoundBuilder;

/**
 * @author dags <dags@dags.me>
 */
public class InterfaceSerializer<T> implements Serializer<T> {

    private final Class<T> type;
    private final Map<Class, Entry> serializers = new HashMap<>();
    private final Map<String, Entry> deserializers = new HashMap<>();

    InterfaceSerializer(Builder<T> builder) {
        this.type = builder.type;
        for (Entry entry : builder.children) {
            serializers.put(entry.type, entry);
            deserializers.put(entry.id, entry);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(T p, CompoundBuilder builder) {
        Entry entry = serializers.get(p.getClass());
        if (entry != null) {
            entry.serializer.serialize(p, builder);
            builder.put("type", entry.id);
        }
    }

    @Override
    public T deserialize(CompoundTag tag) {
        String type = tag.getString("type");
        Entry entry = deserializers.get(type);
        if (entry != null) {
            Object o = entry.serializer.deserialize(tag);
            if (this.type.isInstance(o)) {
                return this.type.cast(o);
            }
        }
        return null;
    }

    private static class Entry<T> {

        private final String id;
        private final Class type;
        private final Serializer<T> serializer;

        private Entry(String id, Class type, Serializer<T> serializer) {
            this.id = id;
            this.type = type;
            this.serializer = serializer;
        }
    }

    public static <T> Builder<T> builder(Class<T> type) {
        return new Builder<>(type);
    }

    public static class Builder<T> {

        private final Class<T> type;
        private final List<Entry> children = new LinkedList<>();

        public Builder(Class<T> type) {
            this.type = type;
        }

        public <V extends T> Builder<T> add(String id, Class<V> type, Serializer<V> serializer) {
            children.add(new Entry<>(id, type, serializer));
            return this;
        }

        public InterfaceSerializer<T> build() {
            return new InterfaceSerializer<>(this);
        }
    }
}
