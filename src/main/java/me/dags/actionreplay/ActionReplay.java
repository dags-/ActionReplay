package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.Recorder;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(name = "ActionReplay", id = "actionreplay", version = "1.0", description = "A time-lapse plugin")
public class ActionReplay {

    private static final SpawnCause SPAWN_CAUSE = SpawnCause.builder().type(SpawnTypes.PLUGIN).build();
    private static ActionReplay instance;

    private Recorder recorder = Recorder.EMPTY;
    private Animation animation = Animation.EMPTY;
    private Format format;

    @Listener
    public void init(GameInitializationEvent event) {
        ActionReplay.instance = this;
        format = CommandBus.newFormatBuilder().build();
        CommandBus.newInstance().register(this).submit(this);
    }

    @Listener
    public void stop(GameStoppingEvent event) {
        if (animation.isPresent()) {
            animation.stop();
        }
    }

    @Command(aliases = "start", parent = "recorder", perm = "actionreplay.recorder.start")
    public void startRecorder(@Caller Player player) {
        if (recorder.isPresent()) {
            format.error("Recorder has not been set up yet").tell(player);
        } else if (recorder.isRecording()) {
            format.error("Recorder is already recording").tell(player);
        } else {
            recorder.setRecording(true);
            Sponge.getEventManager().registerListeners(this, recorder);
            format.info("Now recording...").tell(player);
        }
    }

    @Command(aliases = "start", parent = "recorder", perm = "actionreplay.recorder.start")
    public void startRecorder(@Caller Player player, @One("radius") int radius, @One("height") int height) {
        if (recorder.isPresent()) {
            Vector3i position = player.getLocation().getBlockPosition();
            recorder = new Recorder(player.getWorld().getUniqueId(), position, radius, height);
            recorder.setRecording(true);
            animation = Animation.EMPTY;
            Sponge.getEventManager().registerListeners(this, recorder);
            format.info("Recording block around ").stress(position).tell(player);
        } else {
            startRecorder(player);
        }
    }

    @Command(aliases = "stop", parent = "recorder", perm = "actionreplay.recorder.stop")
    public void stopRecorder(@Caller Player player) {
        if (recorder.isPresent()) {
            format.error("Recorder has not been set up yet").tell(player);
        } else {
            Sponge.getEventManager().unregisterListeners(recorder);
            recorder.setRecording(false);
            format.info("Recording stopped").tell(player);
        }
    }

    @Command(aliases = "reset", parent = "recorder", perm = "actionreplay.recorder.reset")
    public void resetRecorder(@Caller Player player) {
        if (recorder.isPresent()) {
            format.error("Recorder has not been set up yet").tell(player);
        } else {
            Sponge.getEventManager().unregisterListeners(recorder);
            animation.stop();
            recorder = Recorder.EMPTY;
            format.info("Cleared recorder").tell(player);
        }
    }

    @Command(aliases = "create", parent = "replay", perm = "actionreplay.replay.create")
    public void replayCreate(@Caller Player player) {
        if (recorder.isPresent()) {
            format.error("Recorder has not been set up yet").tell(player);
        } else {
            if (animation.isPlaying() && animation.stop()) {
                format.subdued("Stopped current animation").tell(player);
            }
            animation = recorder.getAnimation();
            format.info("Created new replay").tell(player);
        }
    }

    @Command(aliases = "here", parent = "replay create", perm = "actionreplay.replay.create")
    public void replayCreateHere(@Caller Player player) {
        if (recorder.isPresent()) {
            format.error("Recorder has not been set up yet").tell(player);
        } else {
            replayCreate(player);
            animation.setCenter(player.getLocation().getBlockPosition());
            format.info("Set animation position to ").tell(player);
        }
    }

    @Command(aliases = "start", parent = "replay", perm = "actionreplay.replay.start")
    public void animationPlay(@Caller Player player, @One("ticks") int ticks) {
        animationPlay(player, ticks, true);
    }

    @Command(aliases = "start", parent = "replay", perm = "actionreplay.replay.start")
    public void animationPlay(@Caller Player player, @One("ticks") int ticks, @One("avatars") boolean show) {
        if (recorder.isPresent()) {
            format.error("Nothing has been recorded yet").tell(player);
        } else if (recorder.isRecording()) {
            format.error("Recorder is currently recording").tell(player);
        } else if (!animation.isPresent()) {
            format.error("An animation has not been created for this recording").tell(player);
        } else if (animation.isPlaying()) {
            format.error("An animation is currently running").tell(player);
        } else {
            animation.play(this, ticks, show);
            format.info("Playing back at ").stress(ticks).info(" ticks per block").tell(player);
        }
    }

    @Command(aliases = "stop", parent = "replay", perm = "actionreplay.replay.stop")
    public void animationStop(@Caller Player player) {
        if (animation.isPlaying()) {
            animation.stop();
            animation = Animation.EMPTY;
            format.info("Animation stopped").tell(player);
        } else {
            format.error("No animation playing").tell(player);
        }
    }

    public static Cause spawnCause() {
        return Cause.source(ActionReplay.SPAWN_CAUSE).owner(ActionReplay.instance).build();
    }
}
