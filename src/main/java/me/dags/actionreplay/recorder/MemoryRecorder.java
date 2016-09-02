package me.dags.actionreplay.recorder;

import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.Change;

import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class MemoryRecorder extends Recorder {

    private Frame first = null;
    private Frame last = null;

    public MemoryRecorder(UUID worldId, Vector3i center, int radius, int height) {
        super(worldId, center, radius, height);
    }

    @Override
    public void addNextFrame(AvatarSnapshot snapshot, Change change) {
        Frame next = new Frame(snapshot, change);
        if (first == null) {
            first = next;
            last = first;
        } else {
            last.setNextAndUpdate(next, getCenter().toDouble());
            last = next;
        }
    }
}
