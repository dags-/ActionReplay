package me.dags.replay.data;

import me.dags.replay.ActionReplay;
import me.dags.replay.util.OptionalValue;

/**
 * @author dags <dags@dags.me>
 */
public interface Serializer<T extends OptionalValue> {

    T deserialize(Node node);

    void serialize(T t, Node node);

    default Node serialize(T t) {
        Node root = ActionReplay.getNodeFactory().create();
        serialize(t, root);
        return root;
    }

    default Node serializeChecked(T t) throws SerializationException {
        if (t.isAbsent()) {
            throw new SerializationException("value not present");
        }
        return serialize(t);
    }

    default void serializeChecked(T t, Node node) throws SerializationException {
        if (t.isAbsent()) {
            throw new SerializationException("value not present");
        }
        serialize(t, node);
    }

    default T deserializeChecked(Node node) throws SerializationException {
        if (node.isAbsent()) {
            throw new SerializationException("node not present");
        }
        return deserialize(node);
    }

    interface Type extends OptionalValue {

        String getType();
    }
}
