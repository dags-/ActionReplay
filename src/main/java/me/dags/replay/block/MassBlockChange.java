package me.dags.replay.block;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.BlockArrayClipboard;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.regions.CuboidRegion;
import me.dags.replay.util.CompoundBuilder;
import me.dags.replay.serialize.Serializer;
import me.dags.replay.serialize.Serializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class MassBlockChange implements BlockChange {

    private final Vector3i offset;
    private final Schematic schematic;

    public MassBlockChange(Vector3i offset, Schematic schematic) {
        this.offset = offset;
        this.schematic = schematic;
    }

    @Override
    public void apply(Location<World> origin) {
        Vector3i position = origin.getBlockPosition().add(offset);
        Extent extent = FaweAPI.getWorld(origin.getExtent().getName());
        Vector vector = new Vector(position.getX(), position.getY(), position.getZ());
        schematic.paste(extent, vector, true);
    }

    public static final Serializer<MassBlockChange> SERIALIZER = new Serializer<MassBlockChange>() {
        @Override
        public void serialize(MassBlockChange change, CompoundBuilder builder) {
            Serializers.vector3i(builder, change.offset, "x", "y", "z");
            Serializers.schem(builder, change.schematic.getClipboard(), "schem2");
        }

        @Override
        public MassBlockChange deserialize(CompoundTag tag) {
            Vector3i offset = Serializers.vector3i(tag, "x", "y", "z");
            Clipboard clipboard = Serializers.schem(tag);
            if (clipboard == null) {
                clipboard = new BlockArrayClipboard(new CuboidRegion(Vector.ZERO, Vector.ZERO));
            }
            return new MassBlockChange(offset, new Schematic(clipboard));
        }
    };
}
