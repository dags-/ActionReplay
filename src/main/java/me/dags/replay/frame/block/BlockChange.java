package me.dags.replay.frame.block;

import me.dags.replay.frame.Change;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public interface BlockChange extends Change {

    @Override
    default boolean isPresent() {
        return this != NONE;
    }

    BlockChange NONE = new BlockChange() {
        @Override
        public boolean isPresent() {
            return false;
        }

        @Override
        public void apply(Location<World> origin) {

        }

        @Override
        public String getType() {
            return "none";
        }
    };
}
