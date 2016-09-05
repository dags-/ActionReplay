package me.dags.actionreplay.command;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.NodeUtils;
import me.dags.actionreplay.impl.FileRecorder;
import me.dags.actionreplay.replay.Meta;
import me.dags.actionreplay.replay.Recorder;
import me.dags.actionreplay.replay.Replay;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.utils.Format;
import org.spongepowered.api.entity.living.player.Player;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class RecordCommands {

    @Command(aliases = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void help(@Caller Player player) {
        ActionReplay.sendHelp(player);
    }

    @Command(aliases = "load", parent = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void load(@Caller Player player, @One("name") String name) {
        if (getRecorder().isPresent()) {
            format().error("A recorder is already in use").tell(player);
            return;
        }

        Optional<Meta> meta = NodeUtils.loadMeta(name);
        if (meta.isPresent()) {
            Recorder recorder = new FileRecorder(meta.get());
            setRecorder(recorder);
            setAnimation(Replay.EMPTY);

            format().info("Loaded recorder ").stress(meta.get().name).tell(player);
            start(player);
        } else {
            format().error("Recorder {} was not recognised", name).tell(player);
        }
    }


    @Command(aliases = "create", parent = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void create(@Caller Player player, @One("name") String name, @One("radius") int radius, @One("height") int height) {
        if (getRecorder().isPresent()) {
            format().error("A recorder is already in use").tell(player);
            return;
        }

        Meta meta = NodeUtils.loadMeta(name).orElse(new Meta());
        meta.name = name;
        meta.worldId = player.getWorld().getUniqueId();
        meta.center = player.getLocation().getBlockPosition();
        meta.radius = radius;
        meta.height = height;

        NodeUtils.saveMeta(meta);
        ActionReplay.getInstance().getConfig().lastRecorder = name;
        ActionReplay.getInstance().saveConfig();

        setRecorder(new FileRecorder(meta));
        setAnimation(Replay.EMPTY);

        format().info("Created new recorder ").stress(name).tell(player);
        start(player);
    }

    @Command(aliases = "start", parent = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void start(@Caller Player player) {
        if (getRecorder().isPresent()) {
            if (getRecorder().isRecording()) {
                format().error("Recorder is already recording").tell(player);
            } else {
                getRecorder().start(ActionReplay.getInstance());
                format().info("Now recording...").tell(player);
            }
        } else {
            format().error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "stop", parent = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void stop(@Caller Player player) {
        if (getRecorder().isPresent() && getRecorder().isRecording()) {
            getRecorder().stop();
            format().info("Recording stopped").tell(player);
        } else {
            format().error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "reset", parent = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void reset(@Caller Player player) {
        if (getRecorder().isPresent()) {
            if (getRecorder().isRecording()) {
                getRecorder().stop();
            }
            if (getAnimation().isPlaying()) {
                getAnimation().stop();
            }
            setRecorder(Recorder.EMPTY);
            format().info("Cleared recorder").tell(player);
        } else {
            format().error("Recorder has not been set up yet").tell(player);
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

    private void setRecorder(Recorder recorder) {
        ActionReplay.getInstance().setRecorder(recorder);
    }
}
