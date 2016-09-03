package me.dags.actionreplay;

import com.google.inject.Inject;
import me.dags.actionreplay.animation.Animation;
import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.animation.Recorder;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.command.RecordCommands;
import me.dags.actionreplay.command.ReplayCommands;
import me.dags.actionreplay.event.BlockTransaction;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.ChangeBuilder;
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

import java.nio.file.Path;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(name = "ActionReplay", id = "actionreplay", version = "1.0", description = "A time-lapse plugin")
public class ActionReplay {

    private static final SpawnCause SPAWN_CAUSE = SpawnCause.builder().type(SpawnTypes.PLUGIN).build();
    private static final Logger LOGGER = LoggerFactory.getLogger("ActionReplay");
    private static ActionReplay instance;

    private final Database database = new Database(this);
    private final String dbString;
    private final Path configDir;

    private Format format = Format.DEFAULT;
    private Config config = new Config();
    private Recorder recorder = Recorder.EMPTY;
    private Animation animation = Animation.EMPTY;

    public String getDBString() {
        return dbString;
    }

    @Inject
    public ActionReplay(@ConfigDir(sharedRoot = false) Path configDir) {
        ActionReplay.instance = this;
        this.configDir = configDir;
        this.dbString = "jdbc:h2:" + configDir.resolve("recordings").toAbsolutePath();
    }

    @Listener
    public void pre(GamePreInitializationEvent event) {
        DataManager manager = Sponge.getDataManager();
        manager.registerBuilder(Frame.class, new Frame.Builder());
        manager.registerBuilder(Change.class, new ChangeBuilder());
        manager.registerBuilder(AvatarSnapshot.class, new AvatarSnapshot.Builder());
        manager.registerBuilder(BlockTransaction.class, new BlockTransaction.Builder());

        logger().info("Loading config");
        config = Config.load(configDir.resolve("config.conf"));
    }

    @Listener
    public void init(GameInitializationEvent event) {
        CommandBus.builder().logger(logger()).build().register(RecordCommands.class).register(ReplayCommands.class).submit(this);

        database.init();
        format = Format.builder().info(TextColors.YELLOW).stress(TextColors.GREEN).error(TextColors.GRAY).warn(TextColors.RED).build();
        recorder = config.recorderSettings.getRecorder().orElse(Recorder.EMPTY);

        if (this.recorder.isPresent()) {
            Task.builder().execute(() -> {
                logger().info("Starting recorder...");
                recorder.start(this);
            }).submit(this);
        }
    }

    @Listener
    public void stop(GameStoppingEvent event) {
        if (animation.isPresent()) {
            animation.stop();
        }
        saveConfig();
    }

    public Format getFormat() {
        return format;
    }

    public Animation getAnimation() {
        return animation;
    }

    public Recorder getRecorder() {
        return recorder;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public void setRecorder(Recorder recorder) {
        this.recorder = recorder;
    }

    public Config getConfig() {
        return config;
    }

    public void saveConfig() {
        Config.save(getConfig(), configDir.resolve("config.conf"));
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

    public static Database getDatabase() {
        return instance.database;
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
