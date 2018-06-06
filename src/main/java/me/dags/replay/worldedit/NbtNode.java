package me.dags.replay.worldedit;

import com.sk89q.jnbt.*;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class NbtNode extends Node {

    private static final NbtNode EMPTY = new NbtNode(Collections.emptyMap()) {
        @Override
        public boolean isPresent() {
            return false;
        }
    };

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
    public NbtNode newNode() {
        return new NbtNode(new LinkedHashMap<>());
    }

    @Override
    public NbtNode getChild(String key) {
        Tag tag = backing.get(key);
        if (tag == null || !(tag instanceof CompoundTag)) {
            return EMPTY;
        }
        return new NbtNode(((CompoundTag) tag).getValue());
    }

    @Override
    public <T> List<T> getList(String key, Serializer<T> serializer) {
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
                if (t != null) {
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
    public <T> void put(String key, List<T> list, Serializer<T> serializer) {
        List<CompoundTag> tagList = new LinkedList<>();
        for (T t : list) {
            NbtNode node = newNode();
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
    public NbtNode read(InputStream in) throws IOException {
        try (NBTInputStream nbt = new NBTInputStream(new GZIPInputStream(in))) {
            NamedTag tag = nbt.readNamedTag();
            if (tag.getTag() instanceof CompoundTag) {
                CompoundTag compound = (CompoundTag) tag.getTag();
                backing = compound.getValue();
                return this;
            }
        }
        return EMPTY;
    }

    @Override
    protected Object get(String key, Object def) {
        Tag tag = backing.get(key);
        return tag != null ? tag.getValue() : def;
    }
}
