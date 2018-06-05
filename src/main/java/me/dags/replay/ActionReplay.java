package me.dags.replay;

import com.google.inject.Inject;
import me.dags.commandbus.CommandBus;
import me.dags.replay.frame.selector.Selector;
import me.dags.replay.manager.Manager;
import me.dags.replay.replay.ReplayFile;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "replay", name = "ActionReplay", version = "1.0", authors = "dags", description = "Time lapse thing")
public class ActionReplay {

    private static ActionReplay instance;

    private final File configDir;

    private Manager manager;
    private Selector selector;

    @Inject
    public ActionReplay(@ConfigDir(sharedRoot = false) File configDir) {
        this.configDir = configDir;
        instance = this;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        // init manager
        manager = new Manager(this, configDir);

        // init selector & register
        Map<String, String> apis = new LinkedHashMap<>();
        apis.put("com.boydti.fawe.FaweAPI", "me.dags.replay.fawe.FaweSelector");
        selector = Selector.init(apis);
        selector.register(this);

        // register RegistryModule & Commands
        Sponge.getRegistry().registerModule(ReplayFile.class, manager.getRegistry());
        CommandBus.create(this).register(Commands.class).submit();

        // load registry
        manager.getRegistry().update();
        manager.loadFromConfig();
    }

    public static Manager getManager() {
        return instance.manager;
    }

    public static Selector getSelector() {
        return instance.selector;
    }
}
