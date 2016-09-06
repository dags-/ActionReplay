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

    @Command(aliases = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void help(@Caller Player player) {
        ActionReplay.sendHelp(player);
    }

    @Command(aliases = "load", parent = "replay", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public Optional<Replay> load(@Caller Player player, @One("name") String name) {
        if (getAnimation().isPresent() && getAnimation().isPlaying()) {
            format().error("A replay is currently playing").tell(player);
            return Optional.empty();
        }

        Optional<Meta> meta = NodeUtils.loadMeta(name);
        if (meta.isPresent()) {
            Replay animation = new FileReplay(name, player.getWorld().getLocation(meta.get().center));
            setAnimation(animation);
            format().info("Loaded replay ").stress(name).info(" at ").stress(meta.get().center).tell(player);
            return Optional.of(animation);
        }
        return Optional.empty();
    }

    @Command(aliases = "here", parent = "replay load", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void loadHere(@Caller Player player, @One("name") String name) {
        Optional<Replay> replay = load(player, name);
        if (replay.isPresent()) {
            Location<World> location = player.getLocation();
            replay.get().setCenter(location);
            format().info("Set the replay's position to ").stress(location.getBlockPosition()).tell(player);
        }
    }

    @Command(aliases = "start", parent = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void start(@Caller Player player, @One("interval ticks") int ticks, @One("changes per tick") int changes) {
        if (getRecorder().isRecording()) {
            format().error("Recorder is currently recording").tell(player);
        } else if (!getAnimation().isPresent()) {
            format().error("Replay has not been created yet").tell(player);
        } else if (getAnimation().isPlaying()) {
            format().error("Replay is currently running").tell(player);
        } else {
            getAnimation().play(ActionReplay.getInstance(), ticks, changes);
            format().info("Playing...").tell(player);
        }
    }

    @Command(aliases = "stop", parent = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void stop(@Caller Player player) {
        if (getAnimation().isPlaying()) {
            getAnimation().stop();
            format().info("Stopping replay").tell(player);
        } else {
            format().error("No replay playing").tell(player);
        }
    }

    @Command(aliases = "reset", parent = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void reset(@Caller Player player) {
        if (getAnimation().isPresent()) {
            setAnimation(Replay.EMPTY);
            format().info("Discarding replay").tell(player);
        } else {
            format().error("No replay playing").tell(player);
        }
    }

    @Command(aliases = "restore", parent = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void restore(@Caller Player player) {
        if (getAnimation().isPresent()) {
            if (getAnimation().isPlaying()) {
                format().error("The replay is currently playing, you must stop it first").tell(player);
                return;
            }
            format().info("Restoring all frames...").tell(player);
            getAnimation().redoAllFrames(() -> {});
        } else {
            format().error("No replays are currently loaded").tell(player);
        }
    }

    @Command(aliases = "undo", parent = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void undo(@Caller Player player) {
        if (getAnimation().isPresent()) {
            if (getAnimation().isPlaying()) {
                format().error("The replay is currently playing, you must stop it first").tell(player);
                return;
            }
            format().info("Undoing all frames...").tell(player);
            getAnimation().undoAllFrames(() -> {});
        } else {
            format().error("No replays are currently loaded").tell(player);
        }
    }
    
    private Format format() {
        return ActionReplay.getInstance().getFormat();
    }

    private Replay getAnimation() {
        return ActionReplay.getInstance().getAnimation();
    }

    private Recorder getRecorder() {
        return ActionReplay.getInstance().getRecorder();
    }

    private void setAnimation(Replay animation) {
        ActionReplay.getInstance().setAnimation(animation);
    }
}
