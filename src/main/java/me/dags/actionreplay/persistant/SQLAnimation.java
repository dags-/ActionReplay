package me.dags.actionreplay.persistant;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.Database;
import me.dags.actionreplay.Queries;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.FrameProvider;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author dags <dags@dags.me>
 */
public class SQLAnimation extends Animation {

    private final Database database = ActionReplay.getDatabase();
    private final String name;

    public SQLAnimation(String name, Location<World> center) {
        super(center);
        this.name = name;
    }

    @Override
    public void undoAllFrames(Runnable callback) {
        try (Connection connection = ActionReplay.getDatabase().getConnection()) {
            ResultSet resultSet = connection
                    .createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)
                    .executeQuery(Queries.selectReplay(name).getStatement());
            SQLFrameProvider provider = new SQLFrameProvider(connection, resultSet);
            provider.backward();
            while (provider.hasNext()) {
                provider.nextFrame().getChange().undo(center);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        callback.run();
    }

    @Override
    public void redoAllFrames(Runnable callback) {
        try (Connection connection = ActionReplay.getDatabase().getConnection()) {
            ResultSet resultSet = connection.createStatement().executeQuery(Queries.selectReplay(name).getStatement());
            SQLFrameProvider provider = new SQLFrameProvider(connection, resultSet);
            while (provider.hasNext()) {
                provider.nextFrame().getChange().restore(center);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        callback.run();
    }

    @Override
    public void onFinish() {
        super.animationTask = null;
        super.playing = false;
    }

    @Override
    public FrameProvider getFrameProvider() throws SQLException {
        Connection connection = database.getConnection();
        ResultSet resultSet = connection.createStatement().executeQuery(Queries.selectReplay(name).getStatement());
        return new SQLFrameProvider(connection, resultSet);
    }
}
