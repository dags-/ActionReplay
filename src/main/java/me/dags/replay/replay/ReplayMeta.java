package me.dags.replay.replay;

import com.flowpowered.math.vector.Vector3i;
import java.util.Optional;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;
import me.dags.replay.frame.selector.Selector;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayMeta implements OptionalValue {

    public static final ReplayMeta NONE = new ReplayMeta(null, Selector.NULL_BOX) {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public String toString() {
            return "Meta{NONE}";
        }
    };

    private final Location<World> origin;
    private final AABB bounds;

    public ReplayMeta(Location<World> origin, AABB bounds) {
        this.origin = origin;
        this.bounds = bounds;
    }

    public AABB getRelativeBounds() {
        return bounds;
    }

    public AABB getActualBounds() {
        Vector3i min = origin.getBlockPosition().add(bounds.getMin().toInt());
        Vector3i max = origin.getBlockPosition().add(bounds.getMax().toInt());
        return new AABB(min, max);
    }

    public Location<World> getOrigin() {
        return origin;
    }

    @Override
    public boolean isPresent() {
        return origin != null && bounds != null;
    }

    @Override
    public String toString() {
        return "Meta{"
                + "origin=" + origin.getBlockPosition()
                + ",bounds=" + bounds
                + "}";
    }

    public static Serializer<ReplayMeta> SERIALIZER = new Serializer<ReplayMeta>() {
        @Override
        public void serialize(ReplayMeta meta, Node node) {
            node.put("world", meta.origin.getExtent().getName());
            node.put("x", "y", "z", meta.origin.getBlockPosition());
            node.put("x0", "y0", "z0", meta.bounds.getMin().toInt());
            node.put("x1", "y1", "z1", meta.bounds.getMax().toInt());
        }

        @Override
        public ReplayMeta deserialize(Node node) {
            String world = node.getString("world");
            Vector3i origin = node.getVec3i("x", "y", "z");
            Vector3i min = node.getVec3i("x0", "y0", "z0");
            Vector3i max = node.getVec3i("x1", "y1", "z1");
            AABB bounds = Selector.getBounds(min, max);
            Optional<World> extent = Sponge.getServer().getWorld(world);
            return extent.map(w -> new ReplayMeta(new Location<>(w, origin), bounds)).orElse(ReplayMeta.NONE);
        }
    };
}
