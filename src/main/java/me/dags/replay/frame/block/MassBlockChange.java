package me.dags.replay.frame.block;

import com.flowpowered.math.vector.Vector3i;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;
import me.dags.replay.frame.schematic.Schem;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class MassBlockChange implements BlockChange {

    private final Vector3i offset;
    private final Schem schematic;

    public MassBlockChange(Schem schematic, Vector3i offset) {
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
        public void serialize(MassBlockChange change, Node node) {
            Node schem = Schem.SERIALIZER.serialize(change.schematic);
            node.put("x", "y", "z", change.offset);
            node.put("schem", schem);
        }

        @Override
        public MassBlockChange deserialize(Node node) {
            Schem schematic = Schem.SERIALIZER.deserialize(node.getChild("schem"));
            Vector3i offset = node.getVec3i("x", "y", "z");
            return new MassBlockChange(schematic, offset);
        }
    };
}
