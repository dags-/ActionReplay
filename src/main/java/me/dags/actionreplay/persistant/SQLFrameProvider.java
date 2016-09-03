package me.dags.actionreplay.persistant;

import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.animation.FrameProvider;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dags <dags@dags.me>
 */
public class SQLFrameProvider implements FrameProvider {

    private final Connection connection;
    private final ResultSet provider;
    private final Frame.Builder frameBuilder = new Frame.Builder();
    private boolean forward = true;

    public SQLFrameProvider(Connection connection, ResultSet provider) {
        this.connection = connection;
        this.provider = provider;
    }

    @Override
    public boolean hasNext() throws SQLException {
        return forward ? provider.next() : provider.previous();
    }

    @Override
    public void close() throws SQLException {
        connection.close();
    }

    @Override
    public void forward() throws SQLException {
        this.forward = true;
        provider.beforeFirst();
    }

    @Override
    public void backward() throws SQLException {
        this.forward = false;
        provider.afterLast();
    }

    @Override
    public Frame nextFrame() throws SQLException {
        Frame frame = null;
        try (InputStream inputStream = provider.getBinaryStream(2)) {
            DataContainer container = DataFormats.NBT.readFrom(inputStream);
            frame = frameBuilder.fastBuild(container);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return frame;
    }
}
