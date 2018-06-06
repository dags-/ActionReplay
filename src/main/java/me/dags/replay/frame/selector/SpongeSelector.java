package me.dags.replay.frame.selector;

import com.flowpowered.math.vector.Vector3i;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.dags.commandbus.fmt.Fmt;
import me.dags.replay.frame.schematic.Schem;
import me.dags.replay.frame.schematic.SpongeSchematic;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.extent.ArchetypeVolume;
import org.spongepowered.api.world.schematic.BlockPaletteTypes;
import org.spongepowered.api.world.schematic.Schematic;

/**
 * @author dags <dags@dags.me>
 */
public class SpongeSelector implements Selector {

    private final Map<UUID, PlayerSelection> selections = new HashMap<>();

    @Listener
    public void onQuit(ClientConnectionEvent.Disconnect event) {
        selections.remove(event.getTargetEntity().getUniqueId());
    }

    @Override
    public void register(Object plugin) {
        Sponge.getEventManager().registerListeners(plugin, this);
    }

    @Override
    public AABB getSelection(Player player) {
        PlayerSelection selection = selections.get(player.getUniqueId());
        if (selection == null) {
            return Selector.NULL_BOX;
        }
        return Selector.getBounds(selection.pos1, selection.pos2);
    }

    @Override
    public Schem createSchematic(Location<World> origin, AABB bounds) {
        Vector3i min = bounds.getMin().toInt();
        Vector3i max = bounds.getMax().toInt();
        ArchetypeVolume volume = origin.getExtent().createArchetypeVolume(min, max, origin.getBlockPosition());
        Schematic schematic = Schematic.builder().paletteType(BlockPaletteTypes.LOCAL).volume(volume).build();
        return new SpongeSchematic(schematic);
    }

    @Override
    public void pos1(Player player, Vector3i pos) {
        PlayerSelection selection = selections.computeIfAbsent(player.getUniqueId(), PlayerSelection::new);
        selection.pos1 = pos;
        Fmt.info("Set pos1 ").stress(pos).tell(player);
    }

    @Override
    public void pos2(Player player, Vector3i pos) {
        PlayerSelection selection = selections.computeIfAbsent(player.getUniqueId(), PlayerSelection::new);
        selection.pos2 = pos;
        Fmt.info("Set pos2 ").stress(pos).tell(player);
    }

    private static class PlayerSelection {

        private Vector3i pos1 = Vector3i.ZERO;
        private Vector3i pos2 = Vector3i.ZERO;

        private PlayerSelection(UUID uuid) {}
    }
}
