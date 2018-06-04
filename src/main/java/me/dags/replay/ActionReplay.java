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

    private final File configDir;
    private ReplayManager manager;

    @Inject
    public ActionReplay(@ConfigDir(sharedRoot = false) File configDir) {
        this.configDir = configDir;
    }

    @Listener
    public void init(GameInitializationEvent event) {
        if (WEAPI.get().isAbsent()) {
            throw new IllegalStateException("WorldEdit/FAWE not detected. ActionReplay cannot function without!");
        }

        manager = new ReplayManager(this, configDir);
        Sponge.getRegistry().registerModule(ReplayFile.class, manager);
        CommandBus.create(this).register(this).submit();

        Task.builder().execute(() -> {
            manager.updateRegistry();
            if (manager.loadFromConfig()) {
                manager.startRecorder();
            }
        }).submit(this);
    }

    @Command("ar create <name>")
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

        if (selection == WEAPI.INVALID_BOX) {
            Fmt.error("Your selection is too small").tell(player);
            return;
        }

        Text text = manager.createRecorder(name, player.getWorld(), selection);
        player.sendMessage(text);
    }

    @Command("ar stop")
    public void stopRecorder(@Src CommandSource source) {
        Text text;
        if (manager.isRecording()) {
            text = manager.stopRecorder();
        } else if (manager.isplaying()) {
            text = manager.stopReplay();
        } else {
            text = Fmt.error("No recorder or replay is active right now").build();
        }
        if (text != Text.EMPTY) {
            source.sendMessage(text);
        }
    }

    @Command("ar load <replay>")
    public void loadReplay(@Src Player player, ReplayFile replay) {
        Text text = manager.loadReplay(player.getLocation(), replay);
        if (text != Text.EMPTY) {
            player.sendMessage(text);
        }
    }

    @Command("ar start <interval>")
    public void startReplay(@Src Player player) {
        Text text = manager.startReplay(5);
        if (text != Text.EMPTY) {
            player.sendMessage(text);
        }
    }

    @Command("ar start <interval>")
    public void startReplay(@Src Player player, int interval) {
        Text text = manager.startReplay(interval);
        if (text != Text.EMPTY) {
            player.sendMessage(text);
        }
    }

    @Command("ar delete <replay>")
    public void deleteReplay(@Src CommandSource source, ReplayFile replay) {
        Text text = manager.delete(replay);
        if (text != Text.EMPTY) {
            source.sendMessage(text);
        }
    }
}
