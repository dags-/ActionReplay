package me.dags.replay.serialize;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.*;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class TagBuilder {

    private final Map<String, Tag> backing = new HashMap<>();

    public TagBuilder put(String key, Tag tag) {
        backing.put(key, tag);
        return this;
    }

    public TagBuilder put(String key, byte[] value) {
        backing.put(key, new ByteArrayTag(value));
        return this;
    }

    public TagBuilder put(String key, int value) {
        backing.put(key, new IntTag(value));
        return this;
    }

    public TagBuilder put(String key, double value) {
        backing.put(key, new DoubleTag(value));
        return this;
    }

    public TagBuilder put(String key, boolean value) {
        backing.put(key, new ByteTag(value ? (byte) 1 : 0));
        return this;
    }

    public TagBuilder put(String key, String value) {
        backing.put(key, new StringTag(value));
        return this;
    }

    public TagBuilder put(String x, String y, String z, Vector3i vec)  {
        put(x, vec.getX());
        put(y, vec.getY());
        put(z, vec.getZ());
        return this;
    }

    public CompoundTag build() {
        return new CompoundTag(backing);
    }
}
