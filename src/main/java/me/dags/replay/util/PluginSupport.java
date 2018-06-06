package me.dags.replay.util;

import java.util.LinkedHashMap;
import java.util.Map;
import me.dags.replay.data.NodeFactory;
import me.dags.replay.data.SpongeNode;
import me.dags.replay.frame.selector.Selector;
import me.dags.replay.frame.selector.SpongeSelector;

/**
 * @author dags <dags@dags.me>
 */
public final class PluginSupport {

    private PluginSupport() {

    }

    public static Selector getSelector() {
        return getSupport(Selector.class, new SpongeSelector(), new LinkedHashMap<String, String>() {{
            put("com.boydti.fawe.FaweAPI", "me.dags.replay.worldedit.fawe.FaweSelector");
            put("com.sk89q.worldedit.WorldEdit", "me.dags.replay.worldedit.WESelector");
        }});
    }

    public static NodeFactory getNodeFactory() {
        return getSupport(NodeFactory.class, SpongeNode::new, new LinkedHashMap<String, String>() {{
            put("com.sk89q.worldedit.WorldEdit", "me.dags.replay.worldedit.WENodeFactory");
        }});
    }

    private static <T> T getSupport(Class<T> type, T builtIn, Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            try {
                Class.forName(entry.getKey());
                Class c = Class.forName(entry.getValue());
                Object object = c.newInstance();
                if (type.isInstance(object)) {
                    return type.cast(object);
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ignored) {

            }
        }
        return builtIn;
    }
}
