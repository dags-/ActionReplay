package me.dags.actionreplay.command;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.NodeUtils;
import me.dags.actionreplay.impl.FileReplay;
import me.dags.actionreplay.replay.Meta;
import me.dags.actionreplay.replay.Recorder;
import me.dags.actionreplay.replay.Replay;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.utils.Format;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayCommands {

    @Command(aliases = "replay", perm = @Permission("actionreplay.replay"))
    public void help(@Caller Player player) {
        ActionReplay.sendHelp(player);
    }

    @Command(aliases = "load", parent = "replay", perm = @Permission("actionreplay.recorder"))
    public Optional<Replay> load(@Caller Player player, @One("name") String name) {
        if (getReplay().isPresent() && getReplay().isPlaying()) {
            format().error("A replay is currently playing").tell(player);
            return Optional.empty();
        }

        Optional<Meta> meta = NodeUtils.loadMeta(name);
        if (meta.isPresent()) {
            Replay replay = new FileReplay(name, player.getWorld().getLocation(meta.get().center));
            setReplay(replay);
            format().info("Loaded replay ").stress(name).info(" at ").stress(meta.get().center).tell(player);
            return Optional.of(replay);
        }
        return Optional.empty();
    }

    @Command(aliases = "here", parent = "replay load", perm = @Permission("actionreplay.recorder"))
    public void loadHere(@Caller Player player, @One("name") String name) {
        Optional<Replay> replay = load(player, name);
        if (replay.isPresent()) {
            Location<World> location = player.getLocation();
            replay.get().setCenter(location);
            format().info("Set the replay's position to ").stress(location.getBlockPosition()).tell(player);
        }
    }

    @Command(aliases = "start", parent = "replay", perm = @Permission("actionreplay.replay"))
    public void start(@Caller Player player, @One("interval ticks") int ticks, @One("changes per tick") int changes) {
        if (getRecorder().isRecording()) {
            format().error("Recorder is currently recording").tell(player);
        } else if (!getReplay().isPresent()) {
            format().error("Replay has not been created yet").tell(player);
        } else if (getReplay().isPlaying()) {
            format().error("Replay is currently running").tell(player);
        } else {
            getReplay().play(ActionReplay.getInstance(), ticks, changes);
            format().info("Playing...").tell(player);
        }
    }

    @Command(aliases = "stop", parent = "replay", perm = @Permission("actionreplay.replay"))
    public void stop(@Caller Player player) {
        if (getReplay().isPlaying()) {
            getReplay().stop();
            format().info("Stopping replay").tell(player);
        } else {
            format().error("No replay playing").tell(player);
        }
    }

    @Command(aliases = "reset", parent = "replay", perm = @Permission("actionreplay.replay"))
    public void reset(@Caller Player player) {
        if (getReplay().isPresent()) {
            setReplay(Replay.EMPTY);
            format().info("Discarding replay").tell(player);
        } else {
            format().error("No replay playing").tell(player);
        }
    }

    @Command(aliases = "restore", parent = "replay", perm = @Permission("actionreplay.replay"))
    public void restore(@Caller Player player) {
        if (getReplay().isPresent()) {
            if (getReplay().isPlaying()) {
                format().error("The replay is currently playing, you must stop it first").tell(player);
                return;
            }
            format().info("Restoring all frames...").tell(player);
            getReplay().redoAllFrames(() -> {});
        } else {
            format().error("No replays are currently loaded").tell(player);
        }
    }

    @Command(aliases = "undo", parent = "replay", perm = @Permission("actionreplay.replay"))
    public void undo(@Caller Player player) {
        if (getReplay().isPresent()) {
            if (getReplay().isPlaying()) {
                format().error("The replay is currently playing, you must stop it first").tell(player);
                return;
            }
            format().info("Undoing all frames...").tell(player);
            getReplay().undoAllFrames(() -> {});
        } else {
            format().error("No replays are currently loaded").tell(player);
        }
    }
    
    private Format format() {
        return ActionReplay.getInstance().getFormat();
    }

    private Replay getReplay() {
        return ActionReplay.getInstance().getReplay();
    }

    private Recorder getRecorder() {
        return ActionReplay.getInstance().getRecorder();
    }

    private void setReplay(Replay replay) {
        ActionReplay.getInstance().setReplay(replay);
    }
}
