package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.animation.Recorder;
import me.dags.actionreplay.persistant.SQLRecorder;
import me.dags.data.NodeAdapter;
import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;
import org.spongepowered.api.Sponge;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    public int announceInterval = 60 * 5;
    public Map<String, Vector3i> replaySettings = new HashMap<>();
    public RecorderSettings recorderSettings = new RecorderSettings();

    public static class RecorderSettings {

        public String name = "";
        public UUID worldId = UUID.randomUUID();
        public Vector3i center = Vector3i.ZERO;
        public int radius = 256;
        public int height = 256;
        public boolean recording = false;

        public Optional<Recorder> getRecorder() {
            if (name.isEmpty() || !Sponge.getServer().getWorld(worldId).isPresent() || center == Vector3i.ZERO) {
                return Optional.empty();
            }
            return Optional.of(new SQLRecorder(name, worldId, center, radius, height));
        }
    }

    public static Config load(Path path) {
        Config config;
        Node node = NodeAdapter.hocon().from(path);
        if (node.isPresent()) {
            config = ConfigAdapter.instance.fromNode(node);
        } else {
            config = new Config();
            node = ConfigAdapter.instance.toNode(config);
            NodeAdapter.hocon().to(node, path);
        }
        return config;
    }

    public static void save(Config config, Path path) {
        Node node = ConfigAdapter.instance.toNode(config);
        NodeAdapter.hocon().to(node, path);
    }

    public static class ConfigAdapter implements NodeTypeAdapter<Config> {

        private static final ConfigAdapter instance = new ConfigAdapter();

        @Override
        public Node toNode(Config config) {
            NodeObject main = new NodeObject();
            main.put("announce_interval", config.announceInterval);
            main.put("current_recorder", recorderToNode(config.recorderSettings));
            main.put("replays", mapToNode(config.replaySettings));
            return main;
        }

        @Override
        public Config fromNode(Node node) {
            NodeObject object = node.asNodeObject();
            Config config = new Config();
            config.announceInterval = object.map("announce_interval", n -> n.asNumber().intValue(), 5 * 60);
            config.recorderSettings = recorderFromNode(object.get("current_recorder"));
            config.replaySettings = mapFromNode(object.get("replays"));
            return config;
        }

        public Node mapToNode(Map<String, Vector3i> stringVector3iMap) {
            NodeObject object = new NodeObject();
            for (Map.Entry<String, Vector3i> entry : stringVector3iMap.entrySet()) {
                object.put(Node.of(entry.getKey()), vectorToNode(entry.getValue()));
            }
            return object;
        }

        public static NodeObject vectorToNode(Vector3i vector3i) {
            NodeObject vec = new NodeObject();
            vec.put("x", vector3i.getX());
            vec.put("y", vector3i.getY());
            vec.put("z", vector3i.getZ());
            return vec;
        }

        public Map<String, Vector3i> mapFromNode(Node node) {
            NodeObject object = node.asNodeObject();
            Map<String, Vector3i> map = new HashMap<>();
            for (Map.Entry<Node, Node> entry : object.entries()) {
                map.put(entry.getKey().asString(), vectorFromNode(entry.getValue()));
            }
            return map;
        }

        public static Vector3i vectorFromNode(Node node) {
            NodeObject vec = node.asNodeObject();
            int x = vec.map("x", n -> n.asNumber().intValue(), 0);
            int y = vec.map("y", n -> n.asNumber().intValue(), 0);
            int z = vec.map("z", n -> n.asNumber().intValue(), 0);
            return new Vector3i(x, y, z);
        }

        public Node recorderToNode(RecorderSettings recorderSettings) {
            NodeObject recorder = new NodeObject();
            recorder.put("name", recorderSettings.name);
            recorder.put("world_id", recorderSettings.worldId.toString());
            recorder.put("center", vectorToNode(recorderSettings.center));
            recorder.put("radius", recorderSettings.radius);
            recorder.put("height", recorderSettings.height);
            recorder.put("recording", recorderSettings.recording);
            return recorder;
        }

        public RecorderSettings recorderFromNode(Node node) {
            NodeObject object = node.asNodeObject();
            RecorderSettings recorderSettings = new RecorderSettings();
            object.ifPresent("name", name -> recorderSettings.name = name.asString());
            object.ifPresent("world_id", id -> recorderSettings.worldId = UUID.fromString(id.asString()));
            object.ifPresent("center", center -> recorderSettings.center = vectorFromNode(center));
            object.ifPresent("radius", radius -> recorderSettings.radius = radius.asNumber().intValue());
            object.ifPresent("height", height -> recorderSettings.height = height.asNumber().intValue());
            object.ifPresent("recording", recording -> recorderSettings.recording = recording.asBoolean());
            return recorderSettings;
        }
    }
}
