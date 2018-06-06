package me.dags.replay.data;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author dags <dags@dags.me>
 */
public interface NodeFactory {

    Node newNode();

    default Node read(InputStream in) throws IOException {
        return newNode().read(in);
    }
}
