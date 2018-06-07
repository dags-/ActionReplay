package me.dags.replay.worldedit;

import com.flowpowered.math.vector.Vector3i;
import com.sk89q.worldedit.MutableBlockVector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.util.Location;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.spongepowered.api.util.AABB;

/**
 * @author dags <dags@dags.me>
 */
public class WEChange extends AbstractDelegateExtent {

    static final Supplier<Boolean> FALSE = () -> false;

    private final AABB bounds;
    private final long created;
    private final Supplier<Boolean> completed;
    private final MutableBlockVector min = new MutableBlockVector(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);
    private final MutableBlockVector max = new MutableBlockVector(Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);

    private boolean valid = false;
    private boolean committed = false;

    WEChange(Extent extent, AABB bounds, Supplier<Boolean> completed) {
        super(extent);
        this.bounds = bounds;
        this.created = System.currentTimeMillis();
        this.completed = completed == FALSE ? () -> committed : completed;
    }

    /*
     * Records the min and max positions of blocks set inside the bounds
     * Once the operation is complete we can record the blocks inside those positions to a frame
     */
    @Override
    public boolean setBlock(int x, int y, int z, BaseBlock block) throws WorldEditException {
        if (bounds.contains(x, y, z)) {
            min(x, y, z);
            max(x, y, z);
            valid = true;
        }
        return super.setBlock(x, y, z, block);
    }

    /*
     * As setBlock but for entity creations
     */
    @Nullable
    @Override
    public Entity createEntity(Location location, BaseEntity entity) {
        if (bounds.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            min(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            max(location.getBlockX(), location.getBlockY(), location.getBlockZ());
            valid = true;
        }
        return super.createEntity(location, entity);
    }

    @Override
    protected Operation commitBefore() {
        committed = true;
        return super.commitBefore();
    }

    /*
     * If an operation takes longer than 5 seconds expire and do not record?
     */
    public boolean hasExpired() {
        long time = System.currentTimeMillis() - created;
        return TimeUnit.MILLISECONDS.toSeconds(time) > 5;
    }

    /*
     * The operation affects positions within the bounds
     */
    public boolean isValid() {
        return valid;
    }

    /*
     * The operation is complete
     */
    public boolean isComplete() {
        return completed.get();
    }

    public Vector3i getBlockMin() {
        return new Vector3i(min.getBlockX(), min.getBlockY(), min.getBlockZ());
    }

    public Vector3i getBlockMax() {
        return new Vector3i(max.getBlockX(), max.getBlockY(), max.getBlockZ());
    }

    private void min(int x, int y, int z) {
        x = Math.min(x, min.getBlockX());
        y = Math.min(y, min.getBlockY());
        z = Math.min(z, min.getBlockZ());
        min.setComponents(x, y, z);
    }

    private void max(int x, int y, int z) {
        x = Math.max(x, max.getBlockX());
        y = Math.max(y, max.getBlockY());
        z = Math.max(z, max.getBlockZ());
        max.setComponents(x, y, z);
    }
}
