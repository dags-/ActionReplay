package me.dags.replay.data;

import me.dags.replay.ActionReplay;
import me.dags.replay.util.OptionalValue;

/**
 * @author dags <dags@dags.me>
 */
public interface Serializer<T> extends OptionalValue {

    Serializer NONE = new Serializer() {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void serialize(Object o, Node node) {

        }

        @Override
        public Object deserialize(Node node) {
            return null;
        }
    };

    T deserialize(Node node);

    void serialize(T t, Node node);

    default Node serialize(T t) {
        Node root = ActionReplay.getNodeFactory().newNode();
        serialize(t, root);
        return root;
    }

    @Override
    default boolean isPresent() {
        return true;
    }

    interface Type {

        String getType();
    }
}
