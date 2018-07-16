package me.dags.replay.frame.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.dags.replay.event.ReplayEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;

/**
 * @author dags <dags@dags.me>
 */
public class EntityTracker {

    private static final Map<UUID, UUID> ids = new HashMap<>();

    private EntityTracker() {

    }

    @Listener
    public void onStart(ReplayEvent.Start event) {
        ids.clear();
    }

    @Listener
    public void onStop(ReplayEvent.Stop event) {
        ids.clear();
    }

    public static void store(UUID original, UUID id) {
        ids.put(original, id);
    }

    public static UUID lookup(UUID original) {
        return ids.getOrDefault(original, original);
    }

    public static void init(Object plugin) {
        Sponge.getEventManager().registerListeners(plugin, new EntityTracker());
    }
}
