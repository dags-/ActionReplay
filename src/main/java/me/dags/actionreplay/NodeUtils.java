package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.replay.Meta;
import me.dags.data.NodeAdapter;
import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class NodeUtils {

    private static final Meta.Adapter META_ADAPTER = new Meta.Adapter();
    private static final Config.Adapter CONFIG_ADAPTER = new Config.Adapter();

    public static Config loadConfig(Path path) {
        final Config config;
        Node node = NodeAdapter.hocon().from(path);
        if (node.isPresent()) {
            config = CONFIG_ADAPTER.fromNode(node);
        } else {
            config = new Config();
        }
        saveConfig(config, path);
        return config;
    }

    public static void saveConfig(Config config, Path path) {
        Node node = CONFIG_ADAPTER.toNode(config);
        NodeAdapter.hocon().to(node, path);
    }

    public static Optional<Meta> loadMeta(String name) {
        Path path = ActionReplay.resolve("recordings").resolve(name).resolve(name + ".conf");
        if (Files.exists(path)) {
            Node node = NodeAdapter.hocon().from(path);
            Meta meta = META_ADAPTER.fromNode(node);
            return Optional.of(meta);
        }
        return Optional.empty();
    }

    public static void saveMeta(Meta meta) {
        Path path = ActionReplay.resolve("recordings").resolve(meta.name).resolve(meta.name + ".conf");
        Node node = META_ADAPTER.toNode(meta);
        NodeAdapter.hocon().to(node, path);
    }

    public static NodeObject vectorToNode(Vector3i vector3i) {
        NodeObject vec = new NodeObject();
        vec.put("x", vector3i.getX());
        vec.put("y", vector3i.getY());
        vec.put("z", vector3i.getZ());
        return vec;
    }

    public static Vector3i vectorFromNode(Node node) {
        NodeObject vec = node.asNodeObject();
        int x = vec.map("x", n -> n.asNumber().intValue(), 0);
        int y = vec.map("y", n -> n.asNumber().intValue(), 0);
        int z = vec.map("z", n -> n.asNumber().intValue(), 0);
        return new Vector3i(x, y, z);
    }
}
