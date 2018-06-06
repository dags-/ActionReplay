package me.dags.replay.data;

import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.function.Function;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class SpongeNode extends Node {

    private static final Function<String, DataQuery> QUERY = DataQuery::of;
    private static final Map<String, DataQuery> QUERY_CACHE = new HashMap<>();
    private static final SpongeNode EMPTY = new SpongeNode(DataContainer.createNew()) {
        @Override
        public boolean isPresent() {
            return false;
        }
    };

    private DataContainer backing;

    public SpongeNode() {
        this(DataContainer.createNew());
    }

    private SpongeNode(DataContainer backing) {
        this.backing = backing;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public DataContainer build() {
        return backing;
    }

    @Override
    public SpongeNode newNode() {
        return new SpongeNode(DataContainer.createNew());
    }

    @Override
    public Node getChild(String key) {
        DataView view = backing.getView(key(key)).orElse(null);
        if (view == null) {
            return EMPTY;
        }
        return new SpongeNode(view.getContainer());
    }

    @Override
    public <T> List<T> getList(String key, Serializer<T> serializer) {
        List<T> list = new LinkedList<>();
        List<DataView> containers = backing.getViewList(key(key)).orElse(Collections.emptyList());
        for (DataView view : containers) {
            SpongeNode node = new SpongeNode(view.getContainer());
            T t = serializer.deserialize(node);
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    @Override
    public void put(String key, byte[] value) {
        backing.set(key(key), value);
    }

    @Override
    public void put(String key, int value) {
        backing.set(key(key), value);
    }

    @Override
    public void put(String key, double value) {
        backing.set(key(key), value);
    }

    @Override
    public void put(String key, boolean value) {
        backing.set(key(key), value);
    }

    @Override
    public void put(String key, String value) {
        backing.set(key(key), value);
    }

    @Override
    public void put(String key, Node child) {
        if (child instanceof  SpongeNode) {
            SpongeNode node = (SpongeNode) child;
            backing.set(key(key), node.backing);
        }
    }

    @Override
    public <T> void put(String key, List<T> list, Serializer<T> serializer) {
        List<DataContainer> containers = new LinkedList<>();
        for (T t : list) {
            SpongeNode node = newNode();
            serializer.serialize(t, node);
            containers.add(node.build());
        }
        backing.set(key(key), containers);
    }

    @Override
    public void write(OutputStream out) throws IOException {
        try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
            DataFormats.NBT.writeTo(gzip, backing);
        }
    }

    @Override
    public Node read(InputStream in) throws IOException {
        try (GZIPInputStream gzip = new GZIPInputStream(in)) {
            this.backing = DataFormats.NBT.readFrom(gzip);
            return this;
        }
    }

    @Override
    protected Object get(String key, Object def) {
        return backing.get(key(key)).orElse(def);
    }

    private static DataQuery key(String key) {
        return QUERY_CACHE.computeIfAbsent(key, QUERY);
    }
}
