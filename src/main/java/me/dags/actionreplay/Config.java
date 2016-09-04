package me.dags.actionreplay;

import me.dags.data.node.Node;
import me.dags.data.node.NodeObject;
import me.dags.data.node.NodeTypeAdapter;

/**
 * @author dags <dags@dags.me>
 */
public class Config {

    public int announceInterval = 60 * 5;
    public String lastRecorder = "";

    public static class Adapter implements NodeTypeAdapter<Config> {

        @Override
        public Node toNode(Config config) {
            NodeObject object = new NodeObject();
            object.put("announce_interval", config.announceInterval);
            object.put("last_recorder", config.lastRecorder);
            return object;
        }

        @Override
        public Config fromNode(Node node) {
            NodeObject object = node.asNodeObject();
            Config config = new Config();
            config.announceInterval = object.map("announce_interval", n -> n.asNumber().intValue(), 5 * 60);
            config.lastRecorder = object.map("last_recorder", Node::asString, "");
            return config;
        }
    }
}
