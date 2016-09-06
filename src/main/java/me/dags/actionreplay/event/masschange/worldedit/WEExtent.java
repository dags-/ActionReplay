package me.dags.actionreplay.event.masschange.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import me.dags.actionreplay.replay.Recorder;
import me.dags.actionreplay.replay.avatar.AvatarSnapshot;
import org.spongepowered.api.Sponge;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
class WEExtent extends AbstractDelegateExtent {

    private final UUID playerId;
    private final Vector center;
    private final Recorder recorder;
    private final List<WETransaction> transactions = new ArrayList<>();

    WEExtent(UUID playerId, Recorder recorder, Extent extent) {
        super(extent);
        this.playerId = playerId;
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
        if (transactions.size() > 0) {
            Optional<AvatarSnapshot> avatar = Sponge.getServer().getPlayer(playerId)
                    .map(p -> new AvatarSnapshot(p, recorder.getCenter().toDouble()));

            WEMassChange change = new WEMassChange(transactions.toArray(new WETransaction[transactions.size()]));

            if (avatar.isPresent()) {
                recorder.addNextFrame(avatar.get(), change);
            } else {
                recorder.addNextFrame(change);
            }
        }
        return null;
    }
}
