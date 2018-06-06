package me.dags.replay.data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class TypedSerializer<T extends Serializer.Type> implements Serializer<T> {

    private final Map<String, Serializer<? extends T>> serializers = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public void serialize(T t, Node node) {
        Serializer serializer = serializers.get(t.getType());
        if (serializer != null) {
            serializer.serialize(t, node);
            node.put("_type", t.getType());
        }
    }

    @Override
    public T deserialize(Node node) {
        String type = node.getString("_type");
        Serializer<? extends T> serializer = serializers.get(type);
        if (serializer != null) {
            return serializer.deserialize(node);
        }
        return null;
    }

    public TypedSerializer<T> register(String type, Serializer<? extends T> serializer) {
        serializers.put(type, serializer);
        return this;
    }
}
