package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.animation.Recorder;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.BlockTransaction;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.ChangeBuilder;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.Format;
import me.dags.commandbus.annotation.Caller;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.One;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.DataManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnCause;
import org.spongepowered.api.event.cause.entity.spawn.SpawnTypes;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
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
    public void pre(GamePreInitializationEvent event) {
        DataManager manager = Sponge.getDataManager();
        manager.registerBuilder(Frame.class, new Frame.Builder());
        manager.registerBuilder(Change.class, new ChangeBuilder());
        manager.registerBuilder(Animation.class, new Animation.Builder());
        manager.registerBuilder(AvatarSnapshot.class, new AvatarSnapshot.Builder());
        manager.registerBuilder(BlockTransaction.class, new BlockTransaction.Builder());
    }

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

    @Command(aliases = "create", parent = "recorder", perm = "actionreplay.recorder")
    public void createRecorder(@Caller Player player, @One("radius") int radius, @One("height") int height) {
        if (recorder.isPresent()) {
            format.error("A recorder is already in use").tell(player);
        } else {
            Vector3i position = player.getLocation().getBlockPosition();
            recorder = new Recorder(player.getWorld().getUniqueId(), position, radius, height);
            animation = Animation.EMPTY;
            format.info("Created recorder at ").stress(position).tell(player);
            startRecorder(player);
        }
    }

    @Command(aliases = "start", parent = "recorder", perm = "actionreplay.recorder")
    public void startRecorder(@Caller Player player) {
        if (recorder.isPresent()) {
            if (recorder.isRecording()) {
                format.error("Recorder is already recording").tell(player);
            } else {
                recorder.setRecording(true);
                Sponge.getEventManager().registerListeners(this, recorder);
                format.info("Now recording...").tell(player);
            }
        } else {
            format.error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "stop", parent = "recorder", perm = "actionreplay.recorder")
    public void stopRecorder(@Caller Player player) {
        if (recorder.isPresent() && recorder.isRecording()) {
            Sponge.getEventManager().unregisterListeners(recorder);
            recorder.setRecording(false);
            format.info("Recording stopped").tell(player);
        } else {
            format.error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "reset", parent = "recorder", perm = "actionreplay.recorder")
    public void resetRecorder(@Caller Player player) {
        if (recorder.isPresent()) {
            Sponge.getEventManager().unregisterListeners(recorder);
            animation.stop();
            recorder = Recorder.EMPTY;
            format.info("Cleared recorder").tell(player);
        } else {
            format.error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "create", parent = "replay", perm = "actionreplay.replay")
    public void replayCreate(@Caller Player player) {
        if (recorder.isPresent()) {
            if (animation.isPlaying() && animation.stop()) {
                format.subdued("Stopped current animation").tell(player);
            }
            animation = recorder.getAnimation();
            format.info("Created new replay").tell(player);
        } else {
            format.error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "here", parent = "replay create", perm = "actionreplay.replay")
    public void replayCreateHere(@Caller Player player) {
        if (recorder.isPresent()) {
            replayCreate(player);
            animation.setCenter(player.getLocation().getBlockPosition());
            format.info("Set animation position to ").tell(player);
        } else {
            format.error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "start", parent = "replay", perm = "actionreplay.replay")
    public void replayStart(@Caller Player player, @One("ticks") int ticks) {
        replayStart(player, ticks, true);
    }

    @Command(aliases = "start", parent = "replay", perm = "actionreplay.replay")
    public void replayStart(@Caller Player player, @One("ticks") int ticks, @One("avatars") boolean show) {
        if (!recorder.isPresent()) {
            format.error("Nothing has been recorded yet").tell(player);
        } else if (recorder.isRecording()) {
            format.error("Recorder is currently recording").tell(player);
        } else if (!animation.isPresent()) {
            replayCreate(player);
            replayStart(player, ticks, show);
        } else if (animation.isPlaying()) {
            format.error("An animation is currently running").tell(player);
        } else {
            if (animation.play(this, ticks, show)) {
                format.info("Starting replay...").tell(player);
            } else {
                format.error("Something went wrong").tell(player);
            }
        }
    }

    @Command(aliases = "stop", parent = "replay", perm = "actionreplay.replay")
    public void replayStop(@Caller Player player) {
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
