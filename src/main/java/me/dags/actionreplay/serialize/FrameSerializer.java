package me.dags.actionreplay.serialize;

import com.google.common.reflect.TypeToken;
import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.BlockChange;
import me.dags.actionreplay.event.Change;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.SimpleConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class FrameSerializer implements TypeSerializer<Frame> {

    @Override
    public Frame deserialize(TypeToken<?> type, ConfigurationNode node) throws ObjectMappingException {
        Frame.Mutable mutable = Frame.mutable();
        for (ConfigurationNode child : node.getNode("avatars").getChildrenList()) {
            AvatarSnapshot avatar = child.getValue(Tokens.AVATAR);
            mutable.avatars.add(avatar);
        }
        mutable.change = deserializeChange(node.getNode("change"));
        return mutable.build();
    }

    @Override
    public void serialize(TypeToken<?> type, Frame frame, ConfigurationNode node) throws ObjectMappingException {
        List<ConfigurationNode> avatars = new ArrayList<>();
        for (AvatarSnapshot snapshot : frame.getAvatars()) {
            avatars.add(SimpleConfigurationNode.root().setValue(Tokens.AVATAR, snapshot));
        }
        node.getNode("avatars").setValue(avatars);
        serializeChange(frame.getChange(), node.getNode("change"));
    }

    private Change deserializeChange(ConfigurationNode node) throws ObjectMappingException {
        String type = node.getNode("type").getString();
        if (type.equals(Change.BLOCK)) {
            return node.getNode("change").getValue(Tokens.BLOCK_CHANGE);
        }
        return null;
    }

    private void serializeChange(Change change, ConfigurationNode node) throws ObjectMappingException {
        if (change instanceof BlockChange) {
            node.getNode("type").setValue(Change.BLOCK);
            node.getNode("change").setValue(Tokens.BLOCK_CHANGE, (BlockChange) change);
        }
    }
}
