package me.dags.replay.worldedit;

import com.sk89q.jnbt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;
import me.dags.replay.util.OptionalValue;

/**
 * @author dags <dags@dags.me>
 */
public class NbtNode extends Node {

    private Map<String, Tag> backing;

    NbtNode() {
        this(new LinkedHashMap<>());
    }

    private NbtNode(Map<String, Tag> backing) {
        this.backing = backing;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public CompoundTag build() {
        return new CompoundTag(backing);
    }

    @Override
    public Node newNode() {
        return new NbtNode(new LinkedHashMap<>());
    }

    @Override
    public Node getChild(String key) {
        Tag tag = backing.get(key);
        if (tag == null || !(tag instanceof CompoundTag)) {
            return Node.EMPTY;
        }
        return new NbtNode(((CompoundTag) tag).getValue());
    }

    @Override
    public <T extends OptionalValue> List<T> getList(String key, Serializer<T> serializer) {
        Tag tag = backing.get(key);
        if (tag == null || !(tag instanceof ListTag)) {
            return Collections.emptyList();
        }

        List<T> list = new LinkedList<>();
        ListTag<?> listTag = (ListTag) tag;
        for (Tag el : listTag.getValue()) {
            if (el instanceof CompoundTag) {
                NbtNode node = new NbtNode(((CompoundTag) el).getValue());
                T t = serializer.deserialize(node);
                if (t.isPresent()) {
                    list.add(t);
                }
            }
        }

        return list;
    }

    @Override
    public void put(String key, byte[] value) {
        backing.put(key, new ByteArrayTag(value));
    }

    @Override
    public void put(String key, int[] value) {
        backing.put(key, new IntArrayTag(value));
    }

    @Override
    public void put(String key, int value) {
        backing.put(key, new IntTag(value));
    }

    @Override
    public void put(String key, double value) {
        backing.put(key, new DoubleTag(value));
    }

    @Override
    public void put(String key, boolean value) {
        backing.put(key, new ByteTag(value ? (byte) 1 : 0));
    }

    @Override
    public void put(String key, String value) {
        backing.put(key, new StringTag(value));
    }

    @Override
    public void put(String key, Node child) {
        if (child instanceof NbtNode) {
            backing.put(key, ((NbtNode) child).build());
        }
    }

    @Override
    public <T extends OptionalValue> void put(String key, List<T> list, Serializer<T> serializer) {
        List<CompoundTag> tagList = new LinkedList<>();
        for (T t : list) {
            NbtNode node = new NbtNode();
            serializer.serialize(t, node);
            tagList.add(node.build());
        }
        backing.put(key, new ListTag<>(CompoundTag.class, tagList));
    }

    @Override
    public void write(OutputStream out) {
        try (NBTOutputStream nbt = new NBTOutputStream(new GZIPOutputStream(out))) {
            nbt.writeNamedTag("", build());
            nbt.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Node read(InputStream in) throws IOException {
        try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(in))) {
            NamedTag tag = nbt.readNamedTag();
            if (tag.getTag() instanceof CompoundTag) {
                CompoundTag compound = (CompoundTag) tag.getTag();
                backing = compound.getValue();
                return this;
            }
        }
        return Node.EMPTY;
    }

    @Override
    protected Object get(String key, Object def) {
        Tag tag = backing.get(key);
        return tag != null ? tag.getValue() : def;
    }
}
