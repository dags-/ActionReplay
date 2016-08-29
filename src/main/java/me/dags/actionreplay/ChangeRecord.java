package me.dags.actionreplay;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dags <dags@dags.me>
 */
public class ChangeRecord {

    private ChangeRecord previous = null;
    private ChangeRecord next = null;

    private final Map<Avatar, PositionRecord> avatars = new HashMap<>();
    private final BlockRecord blockChangeRecord;

    public ChangeRecord(Avatar avatar, BlockRecord blockChangeRecord) {
        this.blockChangeRecord = blockChangeRecord;
        this.avatars.put(avatar, avatar.getPosition());
    }

    public ChangeRecord addRecord(Avatar avatar, BlockRecord blockChange) {
        ChangeRecord next = new ChangeRecord(avatar, blockChange);
        for (Avatar a : avatars.keySet()) {
            next.avatars.putIfAbsent(a, a.getPosition());
        }
        next.previous = this;
        this.next = next;
        return next;
    }

    public ChangeRecord next() {
        return next;
    }

    public ChangeRecord previous() {
        return previous;
    }

    public void suspendAvatars() {
        for (Avatar avatar : avatars.keySet()) {
            avatar.suspend();
        }
    }

    public void forward() {
        for (Map.Entry<Avatar, PositionRecord> entry : avatars.entrySet()) {
            entry.getKey().updateAvatar(entry.getValue());
        }
        restore();
    }

    public void remove() {
        for (Avatar avatar : avatars.keySet()) {
            avatar.reset();
        }
    }

    public void restore() {
        blockChangeRecord.restore();
    }

    public void reset() {
        blockChangeRecord.reset();
    }
}
