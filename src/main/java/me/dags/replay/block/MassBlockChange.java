package me.dags.replay.block;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.Vector;
import me.dags.replay.serialize.Serializer;
import me.dags.replay.serialize.Serializers;
import me.dags.replay.util.DataBuilder;
import me.dags.replay.worldedit.WEHelper;
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
        if (schematic == null) {
            return;
        }
        Vector3i position = origin.getBlockPosition().add(offset);
        System.out.println("PASTING AT " + position);
        com.sk89q.worldedit.world.World world = FaweAPI.getWorld(origin.getExtent().getName());
        Vector vector = WEHelper.toVec(position);
        schematic.paste(world, vector, false, true, null).flushQueue();
    }

    public static final Serializer<MassBlockChange> SERIALIZER = new Serializer<MassBlockChange>() {
        @Override
        public void serialize(MassBlockChange change, DataBuilder builder) {
            Serializers.vector3i(builder, change.offset, "x", "y", "z");
            Serializers.schem(builder, change.schematic, "schem2");
        }

        @Override
        public MassBlockChange deserialize(CompoundTag tag) {
            Vector3i offset = Serializers.vector3i(tag, "x", "y", "z");
            Schematic schematic = Serializers.schem(tag);
            return new MassBlockChange(offset, schematic);
        }
    };
}
