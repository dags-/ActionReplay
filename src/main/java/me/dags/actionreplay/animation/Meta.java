package me.dags.actionreplay.animation;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.NodeUtils;
import me.dags.actionreplay.impl.FileRecorder;
import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;

import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Meta {

    public static final UUID DUMMY_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    public String name = "";
    public UUID worldId = DUMMY_ID;
    public Vector3i center = Vector3i.ZERO;
    public int radius = 256;
    public int height = 256;
    public boolean recording = false;

    public boolean isPresent() {
        return !name.isEmpty() && worldId != DUMMY_ID && !center.equals(Vector3i.ZERO);
    }

    public Optional<Recorder> getRecorder() {
        return isPresent() ? Optional.of(new FileRecorder(this)) : Optional.empty();
    }

    public static class Adapter implements NodeTypeAdapter<Meta> {

        @Override
        public Node toNode(Meta meta) {
            NodeObject object = new NodeObject();
            object.put("name", meta.name);
            object.put("world_id", meta.worldId.toString());
            object.put("center", NodeUtils.vectorToNode(meta.center));
            object.put("radius", meta.radius);
            object.put("height", meta.height);
            object.put("recording", meta.recording);
            return object;
        }

        @Override
        public Meta fromNode(Node node) {
            NodeObject object = node.asNodeObject();
            Meta meta = new Meta();
            object.ifPresent("name", name -> meta.name = name.asString());
            object.ifPresent("world_id", id -> meta.worldId = UUID.fromString(id.asString()));
            object.ifPresent("center", center -> meta.center = NodeUtils.vectorFromNode(center));
            object.ifPresent("radius", radius -> meta.radius = radius.asNumber().intValue());
            object.ifPresent("height", height -> meta.height = height.asNumber().intValue());
            object.ifPresent("recording", recording -> meta.recording = recording.asBoolean());
            return meta;
        }
    }
}
