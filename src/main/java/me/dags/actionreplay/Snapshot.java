package me.dags.actionreplay;

import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
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
    private final Avatar active;
    private final ItemStackSnapshot itemStack;
    private Snapshot previous = null;
    private Snapshot next = null;

    public Snapshot(Avatar avatar, List<Transaction<BlockSnapshot>> transactions) {
        this.active = avatar;
        this.itemStack = avatar.getItemInHand();
        this.transactions = transactions;
        this.avatars.put(avatar, avatar.getTransform());
    }

    public void setNext(Snapshot next) {
        // link the new snapshot to this one
        next.previous = this;
        this.next = next;

        // transfer the previously captured avatars so their movement is still tracked
        // even if they have not triggered this snapshot
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
        // prevent the avatars from moving between snapshots
        avatars.keySet().forEach(Avatar::pause);
    }

    public void restore() {
        // update user positions
        for (Map.Entry<Avatar, Transform<World>> entry : avatars.entrySet()) {
            entry.getKey().updatePosition(entry.getValue());
        }

        // update the active avatar's held item
        active.updateItemInHand(itemStack);

        // place/break block(s)
        restoreBlocks();
    }

    public void restoreBlocks() {
        // re-enact the block changes from -> to captured by this snapshot
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getFinal().getExtendedState()));
        }
    }

    public void removeAvatars() {
        // calls entity.remove on avatars
        avatars.keySet().forEach(Avatar::remove);
    }

    public void resetBlocks() {
        // reverse block changes to what they were before this snapshot
        for (Transaction<BlockSnapshot> transaction : transactions) {
            transaction.getOriginal().getLocation()
                    .ifPresent(loc -> loc.setBlock(transaction.getOriginal().getExtendedState()));
        }
    }
}
