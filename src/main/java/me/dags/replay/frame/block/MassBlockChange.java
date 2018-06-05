package me.dags.replay.frame.block;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.CompoundTag;
import me.dags.replay.frame.schematic.Schem;
import me.dags.replay.serialize.Serializer;
import me.dags.replay.serialize.Serializers;
import me.dags.replay.serialize.TagBuilder;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class MassBlockChange implements BlockChange {

    private final Vector3i offset;
    private final Schem schematic;

    public MassBlockChange(Vector3i offset, Schem schematic) {
        this.offset = offset;
        this.schematic = schematic;
    }

    @Override
    public String getType() {
        return "mass";
    }

    @Override
    public void apply(Location<World> origin) {
        if (schematic == null) {
            return;
        }
        schematic.apply(origin);
    }

    public static final Serializer<MassBlockChange> SERIALIZER = new Serializer<MassBlockChange>() {
        @Override
        public void serialize(MassBlockChange change, TagBuilder builder) {
            TagBuilder schem = new TagBuilder();
            Schem.SERIALIZER.serialize(change.schematic, schem);
            builder.put("x", "y", "z", change.offset);
            builder.put("schem", schem.build());
        }

        @Override
        public MassBlockChange deserialize(CompoundTag tag) {
            Vector3i offset = Serializers.vector3i(tag, "x", "y", "z");
            CompoundTag schem = (CompoundTag) tag.getValue().get("schem");
            Schem schematic = Schem.SERIALIZER.deserialize(schem);
            return new MassBlockChange(offset, schematic);
        }
    };
}
