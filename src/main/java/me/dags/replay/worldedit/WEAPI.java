package me.dags.replay.worldedit;

import com.flowpowered.math.vector.Vector3i;
import me.dags.replay.util.OptionalValue;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.World;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public interface WEAPI extends OptionalValue {

    AABB NULL_BOX = new AABB(Vector3i.ZERO, Vector3i.ONE);
    AABB INVALID_BOX = new AABB(Vector3i.ZERO, Vector3i.ONE);

    AABB getSelection(Player player, World world);

    Optional<World> getSelectionWorld(Player player);

    static WEAPI get() {
        return INSTANCE;
    }

    WEAPI INSTANCE = ((Supplier<WEAPI>) () -> {
        try {
            Class.forName("com.boydti.fawe.FaweAPI");
            return new FAWEHelper();
        } catch (ClassNotFoundException ignored) {

        }

        try {
            Class.forName("com.sk89q.worldedit.WorldEdit");
            return new WEHelper();
        } catch (ClassNotFoundException ignored) {

        }

        return new WEAPI() {
            @Override
            public boolean isPresent() {
                return false;
            }

            @Override
            public AABB getSelection(Player player, World world) {
                return NULL_BOX;
            }

            @Override
            public Optional<World> getSelectionWorld(Player player) {
                return Optional.empty();
            }
        };
    }).get();
}
