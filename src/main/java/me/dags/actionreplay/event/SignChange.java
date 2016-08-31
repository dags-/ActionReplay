package me.dags.actionreplay.event;

import com.flowpowered.math.vector.Vector3i;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.manipulator.mutable.tileentity.SignData;
import org.spongepowered.api.text.Text;

import java.util.Collections;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
public class SignChange implements Change {

    private final BlockSnapshot blockSnapshot;
    private final List<Text> lines;

    public SignChange(BlockSnapshot blockSnapshot, List<Text> lines) {
        this.blockSnapshot = blockSnapshot;
        this.lines = lines;
    }

    public BlockSnapshot getBlockSnapshot() {
        return blockSnapshot;
    }

    public List<Text> getLines() {
        return lines;
    }

    @Override
    public void restore(Vector3i relative) {
        blockSnapshot.getLocation().ifPresent(loc -> loc.get(SignData.class).ifPresent(signData -> signData.setElements(lines)));
    }

    @Override
    public void undo(Vector3i relative) {
        blockSnapshot.getLocation().ifPresent(loc -> loc.get(SignData.class).ifPresent(signData -> signData.setElements(Collections.emptyList())));
    }
}
