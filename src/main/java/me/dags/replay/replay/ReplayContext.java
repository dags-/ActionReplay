package me.dags.replay.replay;

import me.dags.replay.avatar.Avatar;

import java.util.*;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayContext {

    private final Map<UUID, Avatar> avatars = new HashMap<>();
    private final Set<UUID> active = new HashSet<>();

    public Avatar getAvatar(UUID uuid) {
        active.add(uuid);
        return avatars.getOrDefault(uuid, Avatar.NONE);
    }

    public void setAvatar(UUID uuid, Avatar avatar) {
        avatars.put(uuid, avatar);
        active.add(uuid);
    }

    public void tick() {
        for (Avatar avatar : avatars.values()) {
            avatar.tick();
        }
    }

    public void push() {
        active.clear();
    }

    public void pop() {
        Iterator<Map.Entry<UUID, Avatar>> iterator = avatars.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Avatar> entry = iterator.next();
            if (active.contains(entry.getKey())) {
                continue;
            }
            entry.getValue().dispose();
            iterator.remove();
        }
    }

    public void dispose() {
        avatars.values().forEach(Avatar::dispose);
        avatars.clear();
    }
}
