package me.dags.actionreplay.command;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.Config;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.Recorder;
import me.dags.actionreplay.persistant.SQLRecorder;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.utils.Format;
import org.spongepowered.api.entity.living.player.Player;

/**
 * @author dags <dags@dags.me>
 */
public class RecordCommands {

    @Command(aliases = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void help(@Caller Player player) {
        ActionReplay.sendHelp(player);
    }

    @Command(aliases = "create", parent = "recorder", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void create(@Caller Player player, @One("name") String name, @One("radius") int radius, @One("height") int height) {
        if (getRecorder().isPresent()) {
            format().error("A recorder is already in use").tell(player);
        } else {
            Vector3i position = config().replaySettings.getOrDefault(name, player.getLocation().getBlockPosition());

            config().replaySettings.put(name, position);
            ActionReplay.getInstance().saveConfig();

            setRecorder(new SQLRecorder(name, player.getWorld().getUniqueId(), position, radius, height));
            setAnimation(Animation.EMPTY);
            ActionReplay.getInstance().getConfig().replaySettings.containsKey(name);
            format().info("Created new recorder ").stress(name).tell(player);
            start(player);
        }
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
            getRecorder().stop();
            getAnimation().stop();
            setRecorder(Recorder.EMPTY);
            format().info("Cleared recorder").tell(player);
        } else {
            format().error("Recorder has not been set up yet").tell(player);
        }
    }

    private Format format() {
        return ActionReplay.getInstance().getFormat();
    }

    private Config config() {
        return ActionReplay.getInstance().getConfig();
    }

    private Animation getAnimation() {
        return ActionReplay.getInstance().getAnimation();
    }

    private Recorder getRecorder() {
        return ActionReplay.getInstance().getRecorder();
    }

    private void setAnimation(Animation animation) {
        ActionReplay.getInstance().setAnimation(animation);
    }

    private void setRecorder(Recorder recorder) {
        ActionReplay.getInstance().setRecorder(recorder);
    }
}
