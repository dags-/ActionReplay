package me.dags.actionreplay.event.masschange.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import me.dags.actionreplay.replay.Recorder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dags <dags@dags.me>
 */
class WEExtent extends AbstractDelegateExtent {

    private final Recorder recorder;
    private final Vector center;
    private final List<WETransaction> transactions = new ArrayList<>();

    WEExtent(Recorder recorder, Extent extent) {
        super(extent);
        this.recorder = recorder;
        this.center = new Vector(recorder.getCenter().getX(), recorder.center().getY(), recorder.getCenter().getZ());
    }

    public boolean setBlock(Vector location, BaseBlock block) throws WorldEditException {
        if (recorder.contains(location.getBlockX(), location.getBlockY(), location.getBlockZ())) {
            BaseBlock from = super.getBlock(location);
            if (super.setBlock(location, block)) {
                WETransaction transaction = new WETransaction(location.subtract(center), from, block);
                transactions.add(transaction);
                return true;
            }
            return false;
        }
        return super.setBlock(location, block);
    }

    @Override
    protected Operation commitBefore() {
        WEMassChange change = new WEMassChange(transactions.toArray(new WETransaction[transactions.size()]));
        recorder.addNextFrame(change);
        return null;
    }
}
