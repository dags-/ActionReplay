package me.dags.actionreplay;

import me.dags.actionreplay.animation.Frame;
import me.dags.sqlutils.AbstractDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.sql.SqlService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class Database extends AbstractDB {

    private static final Logger logger = LoggerFactory.getLogger("ARDB");

    private final ActionReplay plugin;
    private SpongeExecutorService service;
    private DataSource dataSource;

    public Database(ActionReplay plugin) {
        this.plugin = plugin;
    }

    private DataSource getDataSource() throws SQLException {
        if (dataSource == null) {
            dataSource = Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource(plugin.getDBString());
        }
        return dataSource;
    }

    public void writeFrame(String table, Frame... frames) {
        execute(() -> {
            try (Connection connection = getConnection()) {
                for (Frame frame : frames) {
                    Blob blob = connection.createBlob();
                    try (OutputStream outputStream = blob.setBinaryStream(1)) {
                        DataFormats.NBT.writeTo(outputStream, frame.toContainer());
                        outputStream.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    PreparedStatement statement = connection.prepareStatement(Queries.insetBlob(table));
                    statement.setBlob(1, blob);
                    statement.executeUpdate();
                    debug("Writing {} Frames", frames.length);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void init() {
        try {
            dataSource = Sponge.getServiceManager().provideUnchecked(SqlService.class).getDataSource(plugin.getDBString());
            service = Sponge.getScheduler().createAsyncExecutor(plugin);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        try {
            debug("Shutting down executor service");
            service.shutdown();

            debug("Awaiting termination");
            service.awaitTermination(1, TimeUnit.SECONDS);
        } catch (Throwable t) {
            log("Error during shutdown:\n{}", t.getMessage());
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    @Override
    public boolean debugging() {
        return false;
    }

    @Override
    public void execute(Runnable runnable) {
        if (service != null) {
            service.execute(runnable);
        }
    }

    @Override
    public <T> void scheduleCallback(T t, Consumer<T> consumer) {
        Task.builder().execute(() -> consumer.accept(t)).submit(plugin);
    }

    @Override
    public void log(String s, Object... objects) {
        logger.info(s, objects);
    }
}
