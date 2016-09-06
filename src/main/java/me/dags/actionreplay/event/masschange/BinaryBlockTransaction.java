package me.dags.actionreplay.event.masschange;

import me.dags.actionreplay.io.ByteArrayDataOutputStream;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface BinaryBlockTransaction {

    void writeTo(ByteArrayDataOutputStream outputStream) throws IOException;
}
