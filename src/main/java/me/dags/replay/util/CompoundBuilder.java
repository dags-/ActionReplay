package me.dags.replay.util;

import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.ByteTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.DoubleTag;
import com.sk89q.jnbt.IntTag;
import com.sk89q.jnbt.StringTag;
import com.sk89q.jnbt.Tag;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class CompoundBuilder {

    private final Map<String, Tag> backing = new HashMap<>();

    public CompoundBuilder put(String key, Tag tag) {
        backing.put(key, tag);
        return this;
    }

    public CompoundBuilder put(String key, byte[] value) {
        backing.put(key, new ByteArrayTag(value));
        return this;
    }

    public CompoundBuilder put(String key, int value) {
        backing.put(key, new IntTag(value));
        return this;
    }

    public CompoundBuilder put(String key, double value) {
        backing.put(key, new DoubleTag(value));
        return this;
    }

    public CompoundBuilder put(String key, boolean value) {
        backing.put(key, new ByteTag(value ? (byte) 1 : 0));
        return this;
    }

    public CompoundBuilder put(String key, String value) {
        backing.put(key, new StringTag(value));
        return this;
    }

    public CompoundTag build() {
        return new CompoundTag(backing);
    }
}
