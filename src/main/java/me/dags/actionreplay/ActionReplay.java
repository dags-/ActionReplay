package me.dags.actionreplay;

import com.flowpowered.math.vector.Vector3i;
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
    private Recorder recorder;
    private Format format;

    @Listener
    public void init(GameInitializationEvent event) {
        instance = this;
        format = CommandBus.newFormatBuilder().build();
        CommandBus.newInstance().register(this).submit(this);
    }

    @Listener
    public void stop(GameStoppingEvent event) {
        if (recorder != null) {
            recorder.stop();
        }
    }

    @Command(aliases = {"actionreplay", "replay", "ar"})
    public void alias() {}

    @Command(aliases = "record", parent = "actionreplay", perm = "actionreplay.record")
    public void startRecorder(@Caller Player player, @One("radius") int radius) {
        if (recorder != null) {
            format.error("Recorder is in use").tell(player);
            return;
        }

        Vector3i position = player.getLocation().getBlockPosition();
        recorder = new Recorder(player.getWorld().getUniqueId(), position, radius);
        Sponge.getEventManager().registerListeners(this, recorder);

        format.info("Recording block around ").stress(position).tell(player);
    }

    @Command(aliases = "stop", parent = "actionreplay", perm = "actionreplay.record")
    public void stopRecorder(@Caller Player player) {
        if (recorder != null) {
            Sponge.getEventManager().registerListeners(this, recorder);
            format.info("Recording stopped").tell(player);
        } else {
            format.error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "reset", parent = "actionreplay", perm = "actionreplay.reset")
    public void resetRecorder(@Caller Player player) {
        if (recorder != null) {
            Sponge.getEventManager().unregisterListeners(recorder);
            recorder.stop();
            recorder = null;
            format.info("Reset recorder").tell(player);
        } else {
            format.error("Recorder has not been set up yet").tell(player);
        }
    }

    @Command(aliases = "play", parent = "actionreplay", perm = "actionreplay.playback")
    public void playback(@Caller Player player, @One("interval ticks") int ticks) {
        if (recorder == null) {
            format.error("Recorder has not been set up yet").tell(player);
            return;
        }
        if (recorder.isRecording()) {
            format.error("Recorder is currently recording. Use ").stress("/actionreplay stop").tell(player);
            return;
        }
        format.info("Playing back at ").stress(ticks).info(" ticks per block change").tell(player);
        recorder.playBack(this, ticks);
    }

    public static Cause spawnCause() {
        return Cause.source(SPAWN_CAUSE).owner(instance).build();
    }
}
