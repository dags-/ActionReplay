package me.dags.replay.data;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class SerializationException extends IOException {

    public SerializationException(String message) {
        super(message);
    }
}
