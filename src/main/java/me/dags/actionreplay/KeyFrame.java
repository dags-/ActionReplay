package me.dags.actionreplay;

import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public abstract class KeyFrame {

    private final Map<Avatar, Transform<World>> avatars = new HashMap<>();
    private KeyFrame previous = null;
    private KeyFrame next = null;

    public KeyFrame next() {
        return next;
    }

    public KeyFrame previous() {
        return previous;
    }

    public void setNext(KeyFrame next) {
        next.previous = this;
        this.next = next;

        for (Avatar avatar : avatars.keySet()) {
            next.avatars.putIfAbsent(avatar, avatar.getTransform());
        }
    }

    public void pauseAvatars() {
        avatars.keySet().forEach(Avatar::pause);
    }

    public void updateAvatars() {
        for (Map.Entry<Avatar, Transform<World>> entry : avatars.entrySet()) {
            entry.getKey().updatePosition(entry.getValue());
        }
    }

    public void removeAvatars() {
        avatars.keySet().forEach(Avatar::remove);
    }

    public abstract void play();

    public abstract void restore();

    public abstract void reset();

    public static abstract class TargetAvatar extends KeyFrame {

        private final Avatar active;
        private final ItemStackSnapshot itemStack;

        public TargetAvatar(Avatar avatar) {
            this.active = avatar;
            this.itemStack = avatar.getItemInHand();
        }

        @Override
        public void play() {
            super.updateAvatars();
            active.updateItemInHand(itemStack);
            this.restore();
        }
    }
}
