package me.dags.replay.util;

import com.sk89q.jnbt.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class DataBuilder {

    private final Map<String, Tag> backing = new HashMap<>();

    public DataBuilder put(String key, Tag tag) {
        backing.put(key, tag);
        return this;
    }

    public DataBuilder put(String key, byte[] value) {
        backing.put(key, new ByteArrayTag(value));
        return this;
    }

    public DataBuilder put(String key, int value) {
        backing.put(key, new IntTag(value));
        return this;
    }

    public DataBuilder put(String key, double value) {
        backing.put(key, new DoubleTag(value));
        return this;
    }

    public DataBuilder put(String key, boolean value) {
        backing.put(key, new ByteTag(value ? (byte) 1 : 0));
        return this;
    }

    public DataBuilder put(String key, String value) {
        backing.put(key, new StringTag(value));
        return this;
    }

    public CompoundTag build() {
        return new CompoundTag(backing);
    }
}
