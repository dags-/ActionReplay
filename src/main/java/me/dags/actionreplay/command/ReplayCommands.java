package me.dags.actionreplay.command;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.Recorder;
import me.dags.actionreplay.persistant.SQLAnimation;
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
    public Optional<Animation> load(@Caller Player player, @One("name") String name) {
        if (getAnimation().isPresent() && getAnimation().isPlaying()) {
            format().error("A replay is currently playing").tell(player);
            return Optional.empty();
        }
        Vector3i pos = ActionReplay.getInstance().getConfig().replaySettings.get(name);
        if (pos == null) {
            format().error("No replay found by that name").tell(player);
            return Optional.empty();
        }
        Animation animation = new SQLAnimation(name, player.getWorld().getLocation(pos));
        setAnimation(animation);
        format().info("Loaded animation ").stress(name).info("at ").stress(pos).tell(player);
        return Optional.of(animation);
    }

    @Command(aliases = "here", parent = "replay load", perm = @Permission(id = "actionreplay.recorder", description = ""))
    public void loadHere(@Caller Player player, @One("name") String name) {
        Optional<Animation> replay = load(player, name);
        if (replay.isPresent()) {
            Location<World> location = player.getLocation();
            replay.get().setCenter(location);
            format().info("Set the replay's position to ").stress(location.getBlockPosition()).tell(player);
        }
    }

    @Command(aliases = "start", parent = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void start(@Caller Player player, @One("ticks") int ticks) {
        if (getRecorder().isRecording()) {
            format().error("Recorder is currently recording").tell(player);
        } else if (!getAnimation().isPresent()) {
            format().error("Replay has not been created yet").tell(player);
        } else if (getAnimation().isPlaying()) {
            format().error("Animation is currently running").tell(player);
        } else {
            getAnimation().play(ActionReplay.getInstance(), ticks);
            format().error("Playing...").tell(player);
        }
    }

    @Command(aliases = "stop", parent = "replay", perm = @Permission(id = "actionreplay.replay", description = ""))
    public void stop(@Caller Player player) {
        if (getAnimation().isPlaying()) {
            getAnimation().stop();
            setAnimation(Animation.EMPTY);
            format().info("Stopping animation").tell(player);
        } else {
            format().error("No animation playing").tell(player);
        }
    }
    
    private Format format() {
        return ActionReplay.getInstance().getFormat();
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
}
