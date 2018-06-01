package me.dags.replay;

import com.google.inject.Inject;
import java.io.File;
import java.util.Optional;
import me.dags.commandbus.CommandBus;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.replay.worldedit.WEAPI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
@Plugin(id = "replay", name = "ActionReplay", version = "1.0", authors = "dags", description = "Time lapse thing")
public class ActionReplay {

    private final ReplayManager manager;

    @Inject
    public ActionReplay(@ConfigDir(sharedRoot = false) File configDir) {
        this.manager = new ReplayManager(this, configDir);
    }

    @Listener
    public void init(GameInitializationEvent event) {
        if (WEAPI.get().isAbsent()) {
            throw new IllegalStateException("WorldEdit/FAWE not detected. ActionReplay cannot function without!");
        }

        Sponge.getRegistry().registerModule(ReplayFile.class, manager);
        CommandBus.create(this).register(this).submit();
        Task.builder().execute(() -> {
            manager.updateRegistry();
            if (manager.loadFromConfig()) {
                manager.startRecorder();
            }
        }).submit(this);
    }

    @Command("recorder create <name>")
    public void createRecorder(@Src Player player, String name) {
        Optional<World> world = WEAPI.get().getSelectionWorld(player);
        if (!world.isPresent()) {
            Fmt.error("Unable to retrieve your WorldEdit session").tell(player);
            return;
        }

        AABB selection = WEAPI.get().getSelection(player, world.get());
        if (selection == WEAPI.NULL_BOX) {
            Fmt.error("Unable to retrieve your selection").tell(player);
            return;
        }

        Text text = manager.createRecorder(name, player.getWorld(), selection);
        player.sendMessage(text);
    }

    @Command("recorder start")
    public void startRecorder(@Src CommandSource source) {
        Text text = manager.startRecorder();
        source.sendMessage(text);
    }

    @Command("recorder stop")
    public void stopRecorder(@Src CommandSource source) {
        Text text = manager.stopRecorder();
        source.sendMessage(text);
    }

    @Command("replay start <name> <interval>")
    public void startReplay(@Src Player player, ReplayFile replay, int interval) {
        Text text = manager.startReplay(player.getLocation(), replay, interval);
        player.sendMessage(text);
    }

    @Command("replay stop")
    public void stopReplay(@Src CommandSource source) {
        Text text = manager.stopReplay();
        source.sendMessage(text);
    }

    @Command("replay delete")
    public void deleteReplay(@Src CommandSource source, ReplayFile replay) {
        Text text = manager.delete(replay);
        source.sendMessage(text);
    }
}
