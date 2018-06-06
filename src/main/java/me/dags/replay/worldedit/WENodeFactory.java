package me.dags.replay.worldedit;

import me.dags.replay.data.Node;
import me.dags.replay.data.NodeFactory;

/**
 * @author dags <dags@dags.me>
 */
public class WENodeFactory implements NodeFactory {

    @Override
    public Node newNode() {
        return new NbtNode();
    }
}
