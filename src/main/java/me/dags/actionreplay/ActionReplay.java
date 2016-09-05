package me.dags.actionreplay;

import com.google.inject.Inject;
import me.dags.actionreplay.command.RecordCommands;
import me.dags.actionreplay.command.ReplayCommands;
import me.dags.actionreplay.event.BlockTransaction;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.ChangeBuilder;
import me.dags.actionreplay.replay.Meta;
import me.dags.actionreplay.replay.Recorder;
import me.dags.actionreplay.replay.Replay;
import me.dags.actionreplay.replay.avatar.AvatarSnapshot;
import me.dags.actionreplay.replay.frame.Frame;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.utils.Format;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
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
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.format.TextColors;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(name = "ActionReplay", id = "actionreplay", version = "1.0", description = "A time-lapse plugin")
public class ActionReplay {

    private static final SpawnCause SPAWN_CAUSE = SpawnCause.builder().type(SpawnTypes.PLUGIN).build();
    private static final Logger LOGGER = LoggerFactory.getLogger("ActionReplay");
    private static ActionReplay instance;

    private final Path configDir;
    private final Path recordingsDir;

    private Format format = Format.DEFAULT;
    private Config config = new Config();
    private Recorder recorder = Recorder.EMPTY;
    private Replay animation = Replay.EMPTY;

    @Inject
    public ActionReplay(@ConfigDir(sharedRoot = false) Path configDir) {
        ActionReplay.instance = this;
        this.configDir = configDir;
        this.recordingsDir = configDir.resolve("recordings");
        if (!Files.exists(recordingsDir)) {
            try {
                Files.createDirectories(recordingsDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Listener
    public void pre(GamePreInitializationEvent event) {
        DataManager manager = Sponge.getDataManager();
        manager.registerBuilder(Frame.class, new Frame.Builder());
        manager.registerBuilder(Change.class, new ChangeBuilder());
        manager.registerBuilder(AvatarSnapshot.class, new AvatarSnapshot.Builder());
        manager.registerBuilder(BlockTransaction.class, new BlockTransaction.Builder());

        logger().info("Loading config");
        config = NodeUtils.loadConfig(configDir.resolve("config.conf"));
    }

    @Listener
    public void init(GameInitializationEvent event) {
        CommandBus.builder().logger(logger()).build().register(RecordCommands.class).register(ReplayCommands.class).submit(this);

        format = Format.builder().info(TextColors.AQUA).stress(TextColors.GREEN).error(TextColors.GRAY).warn(TextColors.RED).subdued(TextColors.GOLD).build();
        recorder = NodeUtils.loadMeta(config.lastRecorder).filter(meta -> meta.recording).flatMap(Meta::getRecorder).orElse(Recorder.EMPTY);

        if (this.recorder.isPresent()) {
            Task.builder().execute(() -> {
                logger().info("Starting recorder...");
                recorder.start(this);
            }).submit(this);
        }
    }

    @Listener
    public void stop(GameStoppingEvent event) {
        saveConfig();
        if (animation.isPresent() && animation.isPlaying()) {
            animation.stopNow();
        }
        if (recorder.isPresent() && recorder.isRecording()) {
            recorder.stopNow();
        }
    }

    public Format getFormat() {
        return format;
    }

    public Replay getAnimation() {
        return animation;
    }

    public Recorder getRecorder() {
        return recorder;
    }

    public void setAnimation(Replay animation) {
        this.animation = animation;
    }

    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }

    public Config getConfig() {
        return config;
    }

    public void saveConfig() {
        NodeUtils.saveConfig(getConfig(), configDir.resolve("config.conf"));
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static ActionReplay getInstance() {
        return instance;
    }

    public static Cause spawnCause() {
        return Cause.source(ActionReplay.SPAWN_CAUSE).owner(ActionReplay.instance).build();
    }

    public static Path resolve(String string) {
        return instance.configDir.resolve(string);
    }

    public static File getRecordingFile(String name) {
        return getRecordingFile(name, true);
    }

    public static File getRecordingFile(String name, boolean create) {
        File file = instance.recordingsDir.resolve(name).resolve(name + ".dat").toFile();
        try {
            if (create && file.createNewFile()) {
                logger().info("Creating file {}", file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static void sendHelp(Player player) {
        getInstance().getFormat().stress("General Steps For Using ActionReplay: ").tell(player);
        getInstance().getFormat().info("1. /recorder create <name> <radius> <height>").tell(player);
        getInstance().getFormat().info("2. Build stuff").tell(player);
        getInstance().getFormat().info("3. /recorder stop").tell(player);
        getInstance().getFormat().info("4. /replay load <name>").tell(player);
        getInstance().getFormat().info("5. /replay start <ticks per block>").tell(player);
    }
}
