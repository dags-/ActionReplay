package me.dags.replay;

import com.flowpowered.math.vector.Vector3i;
import me.dags.commandbus.annotation.Command;
import me.dags.commandbus.annotation.Description;
import me.dags.commandbus.annotation.Permission;
import me.dags.commandbus.annotation.Src;
import me.dags.commandbus.fmt.Fmt;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.frame.selector.Selector;
import me.dags.replay.replay.ReplayFile;
import me.dags.replay.replay.ReplayMeta;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.File;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class Commands {

    private static final String CREATE = "actionreplay.command.control.create";
    private static final String RECORD = "actionreplay.command.control.record";
    private static final String LOAD = "actionreplay.command.control.load";
    private static final String PLAY = "actionreplay.command.control.play";
    private static final String STOP = "actionreplay.command.control.stop";
    private static final String DELETE = "actionreplay.command.delete";
    private static final String SELECT = "actionreplay.command.select";

    @Command("ar create <name>")
    @Permission(Commands.CREATE)
    @Description("Create a new replay recording using your current selection")
    public void create(@Src Player player, String name) {
        AABB bounds = ActionReplay.getSelector().getSelection(player);
        if (bounds == Selector.NULL_BOX) {
            Fmt.error("Unable to retrieve your selection").tell(player);
            return;
        }

        if (bounds == Selector.INVALID_BOX) {
            Fmt.error("Your selection is too small").tell(player);
            return;
        }

        File file = new File(name);
        if (file.exists()) {
            Fmt.error("File already exists").tell(player);
            return;
        }

        Location<World> origin = player.getLocation();
        Vector3i sub = origin.getBlockPosition().mul(-1);
        AABB relative = bounds.offset(sub);

        try {
            ReplayFile replay = ActionReplay.getManager().getRegistry().newReplayFile(name);
            ReplayMeta meta = new ReplayMeta(origin, relative);
            try (FrameSink sink = replay.getSink()) {
                sink.writeHeader(meta);
            }
            ActionReplay.getManager().attachReplayFile(player, replay);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Command("ar load <replay>")
    @Permission(Commands.LOAD)
    @Description("Load the given replay")
    public void load(@Src CommandSource source, ReplayFile file) {
        ActionReplay.getManager().attachReplayFile(source, file);
    }

    @Command("ar load here <replay>")
    @Permission(Commands.LOAD)
    @Description("Load the given replay")
    public void loadHere(@Src Player source, ReplayFile file) {
        ActionReplay.getManager().attachReplayFile(source, file);
        ReplayMeta meta = ActionReplay.getManager().getActive().getMeta();
        Location<World> origin = source.getLocation();
        AABB bounds = meta.getRelativeBounds().offset(origin.getBlockPosition());
        ReplayMeta here = new ReplayMeta(origin, bounds);
        ActionReplay.getManager().getActive().setMeta(here);
        ActionReplay.getManager().attachReplay(source);
    }

    @Command("ar record")
    @Permission(Commands.RECORD)
    @Description("Start the currently loaded recorder")
    public void startRecorder(@Src CommandSource source) {
        ActionReplay.getManager().attachRecorder(source);
        ActionReplay.getManager().startRecorder(source);
    }

    @Command("ar play <ticks>")
    @Permission(Commands.PLAY)
    @Description("Start the currently loaded replay")
    public void startReplay(@Src CommandSource source, int ticks) {
        ActionReplay.getManager().attachReplay(source);
        ActionReplay.getManager().startReplay(source, ticks);
    }

    @Command("ar stop")
    @Permission(Commands.STOP)
    @Description("Stop the currently active recorder or replay")
    public void stop(@Src CommandSource source) {
        ActionReplay.getManager().stop(source);
    }

    @Command("ar delete <replay>")
    @Permission(Commands.DELETE)
    @Description("Delete the given replay")
    public void delete(@Src CommandSource source, ReplayFile replay) {
        ActionReplay.getManager().delete(source, replay);
    }

    @Command("ar pos1")
    @Permission(Commands.SELECT)
    @Description("Set pos1 of your selection")
    public void pos1(@Src Player player) {
        ActionReplay.getSelector().pos1(player, player.getLocation().getBlockPosition());
    }

    @Command("ar pos2")
    @Permission(Commands.SELECT)
    @Description("Set pos2 of your selection")
    public void pos2(@Src Player player) {
        ActionReplay.getSelector().pos2(player, player.getLocation().getBlockPosition());
    }

    @Command("ar expand")
    @Permission(Commands.SELECT)
    @Description("Expand your selection vertically")
    public void expand(@Src Player player) {
        AABB selection = ActionReplay.getSelector().getSelection(player);
        if (selection == Selector.NULL_BOX || selection == Selector.INVALID_BOX) {
            Fmt.error("Invalid selection box").tell(player);
            return;
        }

        Vector3i min = selection.getMin().toInt().mul(1, 0, 1);
        Vector3i max = selection.getMax().toInt().mul(1, 0, 1).add(0, 255, 0);
        ActionReplay.getSelector().pos1(player, min);
        ActionReplay.getSelector().pos2(player, max);
    }
}
