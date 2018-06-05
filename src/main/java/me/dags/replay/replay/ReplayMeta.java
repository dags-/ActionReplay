package me.dags.replay.replay;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.CompoundTag;
import me.dags.replay.frame.selector.Selector;
import me.dags.replay.serialize.DataView;
import me.dags.replay.serialize.Serializers;
import me.dags.replay.serialize.TagBuilder;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayMeta implements OptionalValue, DataView {

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

    @Override
    public CompoundTag getData() {
        TagBuilder root = new TagBuilder();
        root.put("world", origin.getExtent().getName());
        root.put("x", "y", "z", origin.getBlockPosition());
        root.put("x0", "y0", "z0", bounds.getMin().toInt());
        root.put("x1", "y1", "z1", bounds.getMax().toInt());
        return root.build();
    }

    public static ReplayMeta fromData(CompoundTag root) {
        String world = root.getString("world");
        Vector3i origin = Serializers.vector3i(root, "x", "y", "z");
        Vector3i min = Serializers.vector3i(root, "x0", "y0", "z0");
        Vector3i max = Serializers.vector3i(root, "x1", "y1", "z1");
        Optional<World> extent = Sponge.getServer().getWorld(world);
        return extent.map(w -> new ReplayMeta(new Location<>(w, origin), new AABB(min, max))).orElse(NONE);
    }
}
