package me.dags.replay.data;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Function;
import me.dags.replay.util.OptionalValue;

/**
 * @author dags <dags@dags.me>
 */
public abstract class Node implements OptionalValue {

    private static final byte[] empty = new byte[0];

    public int getInt(String key) {
        return get(key, 0, int.class);
    }

    public String getString(String key) {
        return get(key, "", String.class);
    }

    public double getDouble(String key) {
        return get(key, 0D, double.class);
    }

    public byte[] getBytes(String key) {
        return get(key, empty, byte[].class);
    }

    public boolean getBool(String key) {
        return get(key, (byte) 0, byte.class) == 1;
    }

    public Vector3i getVec3i(String x, String y, String z) {
        return new Vector3i(getInt(x), getInt(y), getInt(z));
    }

    public Vector3d getVec3d(String x, String y, String z) {
        return new Vector3d(getDouble(x), getDouble(y), getDouble(z));
    }

    public <T> T fromBytes(String key, Function<byte[], T> reader) {
        byte[] bytes = getBytes(key);
        return reader.apply(bytes);
    }

    public <T> void put(String key, T value, Function<T, byte[]> writer) {
        byte[] bytes = writer.apply(value);
        put(key, bytes);
    }

    public void put(String x, String y, String z, Vector3i vec) {
        put(x, vec.getX());
        put(y, vec.getY());
        put(z, vec.getZ());
    }

    public void put(String x, String y, String z, Vector3d vec) {
        put(x, vec.getX());
        put(y, vec.getY());
        put(z, vec.getZ());
    }

    private <T> T get(String key, T def, Class<T> type) {
        Object o = get(key, def);
        if (o == def) {
            return def;
        }
        if (!type.isInstance(o)) {
            return def;
        }
        return type.cast(o);
    }

    public abstract Object build();

    public abstract Node newNode();

    public abstract Node getChild(String key);

    public abstract <T> List<T> getList(String key, Serializer<T> serializer);

    public abstract void put(String key, byte[] value);

    public abstract void put(String key, int value);

    public abstract void put(String key, double value);

    public abstract void put(String key, boolean value);

    public abstract void put(String key, String value);

    public abstract void put(String key, Node child);

    public abstract <T> void put(String key, List<T> list, Serializer<T> serializer);

    public abstract void write(OutputStream out) throws IOException;

    public abstract Node read(InputStream in) throws IOException;

    protected abstract Object get(String key, Object def);
}
