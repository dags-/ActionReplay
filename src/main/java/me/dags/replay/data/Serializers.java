package me.dags.replay.data;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import me.dags.replay.util.OptionalValue;
import org.jnbt.CompoundTag;
import org.jnbt.ListTag;
import org.jnbt.Nbt;
import org.jnbt.TagType;

/**
 * @author dags <dags@dags.me>
 */
public class Serializers {

    public static Vector3i vec3i(CompoundTag tag, String x, String y, String z) {
        return new Vector3i(tag.getInt(x), tag.getInt(y), tag.getInt(x));
    }

    public static void vec3i(CompoundTag tag, String x, String y, String z, Vector3i vec) {
        tag.put(x, vec.getX());
        tag.put(y, vec.getY());
        tag.put(z, vec.getZ());
    }

    public static Vector3d vec3d(CompoundTag tag, String x, String y, String z) {
        return new Vector3d(tag.getInt(x), tag.getInt(y), tag.getInt(x));
    }

    public static void vec3d(CompoundTag tag, String x, String y, String z, Vector3d vec) {
        tag.put(x, vec.getX());
        tag.put(y, vec.getY());
        tag.put(z, vec.getZ());
    }

    public static UUID uuid(CompoundTag tag, String bits0, String bits1) {
        long least = tag.getLong(bits0);
        long most = tag.getLong(bits1);
        return new UUID(least, most);
    }

    public static void uuid(CompoundTag tag, String bits0, String bits1, UUID uuid) {
        tag.put(bits0, uuid.getLeastSignificantBits());
        tag.put(bits1, uuid.getMostSignificantBits());
    }

    public static <T extends OptionalValue> List<T> list(CompoundTag tag, String key, Serializer<T> serializer) {
        ListTag<CompoundTag> elements = tag.getListTag(key, TagType.COMPOUND);
        List<T> list = new ArrayList<>(elements.getBacking().size());
        for (CompoundTag ct : elements) {
            T t = serializer.deserialize(ct);
            if (t.isPresent()) {
                list.add(t);
            }
        }
        return list;
    }

    public static <T extends OptionalValue> void list(CompoundTag tag, String key, Iterable<T> elements, Serializer<T> serializer) {
        ListTag<CompoundTag> list = Nbt.list(TagType.COMPOUND);
        for (T t : elements) {
            CompoundTag ct = serializer.serialize(t);
            if (ct.isPresent()) {
                list.add(ct);
            }
        }
        tag.put(key, list);
    }
}
