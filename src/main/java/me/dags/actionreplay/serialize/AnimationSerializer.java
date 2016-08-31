package me.dags.actionreplay.serialize;

import com.google.common.reflect.TypeToken;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.Frame;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class AnimationSerializer implements TypeSerializer<Animation> {

    @Override
    public Animation deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
        List<Frame> frames = new ArrayList<>();
        for (ConfigurationNode child : node.getNode("frames").getChildrenList()) {
            frames.add(child.getValue(Tokens.FRAME));
        }
        return Animation.fromList(frames);
    }

    @Override
    public void serialize(TypeToken<?> type, Animation animation, ConfigurationNode node) throws ObjectMappingException {
        List<ConfigurationNode> frames = new ArrayList<>();
        for (Frame frame : Animation.toList(animation)) {
            frames.add(SimpleConfigurationNode.root().setValue(Tokens.FRAME, frame));
        }
        node.getNode("frames").setValue(frames);
    }
}
