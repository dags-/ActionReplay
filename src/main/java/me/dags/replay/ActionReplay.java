package me.dags.replay;

import com.google.inject.Inject;
import java.io.File;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.fmt.Fmt;
import me.dags.commandbus.fmt.Format;
import me.dags.replay.data.NodeFactory;
import me.dags.replay.event.RecordEvent;
import me.dags.replay.event.ReplayEvent;
import me.dags.replay.frame.selector.Selector;
import me.dags.replay.manager.Manager;
import me.dags.replay.replay.ReplayFile;
import me.dags.replay.util.PluginSupport;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "replay", name = "ActionReplay", version = "1.0", authors = "dags", description = "Time lapse thing")
public class ActionReplay {

    private static ActionReplay instance;

    private final File configDir;

    private Manager manager;
    private Selector selector;
    private NodeFactory nodeFactory;

    @Inject
    public ActionReplay(@ConfigDir(sharedRoot = false) File configDir) {
        this.configDir = configDir;
        instance = this;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        // init manager
        manager = new Manager(this, configDir);

        Fmt.init(Format.builder()
                .info(TextColors.AQUA)
                .stress(TextColors.DARK_AQUA)
                .subdued(TextColors.GRAY, TextStyles.ITALIC)
                .error(TextColors.RED, TextStyles.ITALIC)
                .warn(TextColors.DARK_RED, TextStyles.BOLD)
                .build());

        nodeFactory = PluginSupport.getNodeFactory();
        selector = PluginSupport.getSelector();
        selector.register(this);

        // register RegistryModule & Commands
        Sponge.getRegistry().registerModule(ReplayFile.class, manager.getRegistry());
        CommandBus.create(this).register(Commands.class).submit();

        // load registry
        manager.getRegistry().update();
        manager.loadFromConfig();
    }

    @Listener
    public void startRecording(RecordEvent.Start event) {
        World world = event.getRecorder().getOrigin().getExtent();
        AABB bounds = event.getRecorder().getBounds();
        MessageChannel channel = Sponge.getServer().getBroadcastChannel();
        Fmt.info("Recording started in ").stress(world.getName()).info(" at ").stress(bounds).tell(channel);
    }

    @Listener
    public void stopRecording(RecordEvent.Stop event) {
        World world = event.getRecorder().getOrigin().getExtent();
        MessageChannel channel = Sponge.getServer().getBroadcastChannel();
        Fmt.info("Recording stopped in ").stress(world.getName()).info(" stopped").tell(channel);
    }

    @Listener
    public void startReplay(ReplayEvent.Start event) {
        String name = event.getReplay().getName();
        World world = event.getMeta().getOrigin().getExtent();
        MessageChannel channel = Sponge.getServer().getBroadcastChannel();
        Fmt.info("Replay ").stress(name).info(" started in world ").stress(world.getName()).tell(channel);
    }

    @Listener
    public void stopReplay(ReplayEvent.Stop event) {
        String name = event.getReplay().getName();
        MessageChannel channel = Sponge.getServer().getBroadcastChannel();
        Fmt.info("Replay ").stress(name).info(" stopped").tell(channel);
    }

    public static Manager getManager() {
        return instance.manager;
    }

    public static Selector getSelector() {
        return instance.selector;
    }

    public static NodeFactory getNodeFactory() {
        return instance.nodeFactory;
    }
}
