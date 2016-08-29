package me.dags.actionreplay;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class Snapshot {

    private final Map<Avatar, Transform<World>> avatars = new HashMap<>();
    private final List<Transaction<BlockSnapshot>> transactions;
    private Snapshot previous = null;
    private Snapshot next = null;

    public Snapshot(Avatar avatar, List<Transaction<BlockSnapshot>> transactions) {
        this.transactions = transactions;
        this.avatars.put(avatar, avatar.getTransform());
    }

    public void setNext(Snapshot next) {
        next.previous = this;
        this.next = next;

        for (Avatar avatar : avatars.keySet()) {
            next.avatars.putIfAbsent(avatar, avatar.getTransform());
        }
    }

    public Snapshot next() {
        return next;
    }

    public Snapshot previous() {
        return previous;
    }

    public void pauseAvatars() {
        for (Avatar avatar : avatars.keySet()) {
            avatar.pause();
        }
    }

    public void restore() {
        for (Map.Entry<Avatar, Transform<World>> entry : avatars.entrySet()) {
            entry.getKey().updateAvatar(entry.getValue());
        }
        restoreBlocks();
    }

    public void restoreBlocks() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getFinal().getExtendedState()));
        }
    }

    public void removeAvatars() {
        for (Avatar avatar : avatars.keySet()) {
            avatar.remove();
        }
    }

    public void resetBlocks() {
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getOriginal().getExtendedState()));
        }
    }
}
