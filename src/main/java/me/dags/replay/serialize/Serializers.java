package me.dags.replay.serialize;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class Serializers {

    @SuppressWarnings("unchecked")
    public static void list(TagBuilder builder, String name, List list, Serializer serializer) {
        List<CompoundTag> tagList = new ArrayList<>(list.size());
        for (Object o : list) {
            TagBuilder child = new TagBuilder();
            serializer.serialize(o, child);
            tagList.add(child.build());
        }
        builder.put(name, new ListTag<>(CompoundTag.class, tagList));
    }

    @SuppressWarnings("unchecked")
    public static void list(List<CompoundTag> tagList, List list, Serializer serializer) {
        for (CompoundTag tag : tagList) {
            list.add(serializer.deserialize(tag));
        }
    }

    public static void vector3i(TagBuilder builder, Vector3i vec, String i0, String i1, String i2) {
        builder.put(i0, vec.getX());
        builder.put(i1, vec.getY());
        builder.put(i2, vec.getZ());
    }

    public static void vector3d(TagBuilder builder, Vector3d vec, String d0, String d1, String d2) {
        builder.put(d0, vec.getX());
        builder.put(d1, vec.getY());
        builder.put(d2, vec.getZ());
    }

    public static void itemStack(TagBuilder builder, String name, ItemStack stack) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DataFormats.NBT.writeTo(out, stack.toContainer());
            builder.put(name, new ByteArrayTag(out.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ItemStack itemStack(CompoundTag tag, String name) {
        byte[] data = tag.getByteArray(name);
        try (ByteArrayInputStream in = new ByteArrayInputStream(data)) {
            DataContainer container = DataFormats.NBT.readFrom(in);
            return ItemStack.builder().fromContainer(container).build();
        } catch (IOException e) {
            return ItemStack.empty();
        }
    }

    public static Vector3d vector3d(CompoundTag tag, String d0, String d1, String d2) {
        double x = tag.getDouble(d0);
        double y = tag.getDouble(d1);
        double z = tag.getDouble(d2);
        return new Vector3d(x, y, z);
    }

    public static Vector3i vector3i(CompoundTag tag, String i0, String i1, String i2) {
        int x = tag.getInt(i0);
        int y = tag.getInt(i1);
        int z = tag.getInt(i2);
        return new Vector3i(x, y, z);
    }
}
