package me.dags.replay.frame.selector;

import com.flowpowered.math.vector.Vector3i;
import java.lang.reflect.Constructor;
import java.util.Map;
import me.dags.replay.frame.schematic.Schem;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface Selector {

    AABB NULL_BOX = new AABB(Vector3i.ZERO, Vector3i.ONE);
    AABB INVALID_BOX = new AABB(Vector3i.ZERO, Vector3i.ONE);

    void register(Object plugin);

    void pos1(Player player, Vector3i pos);

    void pos2(Player player, Vector3i pos);

    AABB getSelection(Player player);

    Schem createSchematic(Location<World> origin, AABB bounds);

    static Selector init(Map<String, String> options) {
        for (Map.Entry<String, String> e : options.entrySet()) {
            try {
                Class.forName(e.getKey());
                Class<?> api = Class.forName(e.getValue());
                if (Selector.class.isAssignableFrom(api)) {
                    Class<? extends Selector> type = api.asSubclass(Selector.class);
                    Constructor<? extends Selector> constructor = type.getConstructor();
                    return constructor.newInstance();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
        return new SpongeSelector();
    }

    static AABB getBounds(Vector3i pos1, Vector3i pos2) {
        Vector3i min = pos1.min(pos2);
        Vector3i max = pos1.max(pos2);
        if (min.getX() >= max.getX() || min.getY() >= max.getY() || min.getZ() >= max.getZ()) {
            return Selector.INVALID_BOX;
        }
        return new AABB(min, max);
    }
}
