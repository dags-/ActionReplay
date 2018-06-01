package me.dags.replay.serialize;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.jnbt.ListTag;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardWriter;
import com.sk89q.worldedit.world.registry.LegacyWorldData;
import com.sk89q.worldedit.world.registry.WorldData;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import me.dags.replay.util.CompoundBuilder;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.item.inventory.ItemStack;

/**
 * @author dags <dags@dags.me>
 */
public class Serializers {

    @SuppressWarnings("unchecked")
    public static void list(CompoundBuilder tag, String name, List list, Serializer serializer) {
        List<CompoundTag> tagList = new ArrayList<>(list.size());
        for (Object o : list) {
            CompoundBuilder builder = new CompoundBuilder();
            serializer.serialize(o, builder);
            tagList.add(builder.build());
        }
        tag.put(name, new ListTag<>(CompoundTag.class, tagList));
    }

    @SuppressWarnings("unchecked")
    public static void list(List<CompoundTag> tagList, List list, Serializer serializer) {
        for (CompoundTag tag : tagList) {
            list.add(serializer.deserialize(tag));
        }
    }

    public static void vector3i(CompoundBuilder map, Vector3i vec, String i0, String i1, String i2) {
        map.put(i0, vec.getX());
        map.put(i1, vec.getY());
        map.put(i2, vec.getZ());
    }

    public static void vector3d(CompoundBuilder map, Vector3d vec, String d0, String d1, String d2) {
        map.put(d0, vec.getX());
        map.put(d1, vec.getY());
        map.put(d2, vec.getZ());
    }

    public static void itemStack(CompoundBuilder map, String name, ItemStack stack) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            DataFormats.NBT.writeTo(out, stack.toContainer());
            map.put(name, new ByteArrayTag(out.toByteArray()));
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

    public static Clipboard schem(CompoundTag tag) {
        String formatName = tag.getString("format");
        byte[] bytes = tag.getByteArray("schematic");
        WorldData data = LegacyWorldData.getInstance();
        ClipboardFormat format = ClipboardFormat.findByAlias(formatName);
        if (format == null) {
            return null;
        }

        try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(bytes))) {
            return format.getReader(in).read(data);
        } catch (IOException e) {
            return null;
        }
    }

    public static void schem(CompoundBuilder map, Clipboard clipboard, String formatName) {
        ClipboardFormat format = ClipboardFormat.findByAlias(formatName);
        if (format == null) {
            formatName = ClipboardFormat.SCHEMATIC.name();
            format = ClipboardFormat.SCHEMATIC;
        }

        WorldData data = LegacyWorldData.getInstance();
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();) {
            try (ClipboardWriter writer = format.getWriter(new GZIPOutputStream(out))) {
                writer.write(clipboard, data);
            }
            map.put("format", formatName);
            map.put("schematic", out.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
