package me.dags.actionreplay.event.masschange.worldedit;

import com.google.common.primitives.Ints;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.blocks.BaseBlock;
import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.Ids;
import me.dags.actionreplay.event.masschange.MassChange;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
class WEMassChange implements MassChange<WETransaction> {

    private final WETransaction[] transactions;
    private com.sk89q.worldedit.world.World world;

    WEMassChange(WETransaction[] transactions) {
        this.transactions = transactions;
    }

    @Override
    public WETransaction[] getBlocks() {
        return transactions;
    }

    @Override
    public byte getMID() {
        return Ids.WORLD_EDIT;
    }

    @Override
    public void restoreOne(WETransaction block, Location<World> location) throws Exception {
        setBlock(block.to, block.position, location);
    }

    @Override
    public void undoOne(WETransaction block, Location<World> location) throws Exception {
        setBlock(block.from, block.position, location);
    }

    @Override
    public int getContentVersion() {
        return 0;
    }

    private void setBlock(BaseBlock block, Vector position, Location<World> location) throws WorldEditException {
        Vector targetPos = position.add(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (targetPos.getY() > -1 && targetPos.getY() < 256) {
            com.sk89q.worldedit.world.World world = getWorld(location.getExtent().getName());
            if (world != null) {
                world.setBlock(targetPos, block);
            }
        }
    }

    private com.sk89q.worldedit.world.World getWorld(String name) {
        if (world == null) {
            world = WorldEdit.getInstance().getServer().getWorlds()
                    .stream()
                    .filter(world -> world.getName().equals(name))
                    .findFirst()
                    .orElse(null);
        }
        return world;
    }

    public static class Builder implements MassChange.Builder {

        private static final int LENGTH = (3 * 4) + 4 + 4 + 1 + 1;

        @Override
        public Optional<Change> from(DataView view) throws InvalidDataException {
            Optional<Object> data = view.get(TRANSACTIONS);
            if (data.isPresent()) {
                try {
                    byte[] bytes = (byte[]) data.get();
                    WETransaction[] transactions = new WETransaction[bytes.length / LENGTH];
                    for (int i = 0, j = 0; i < bytes.length; ) {

                        int x = Ints.fromBytes(bytes[i++], bytes[i++], bytes[i++], bytes[i++]);
                        int y = Ints.fromBytes(bytes[i++], bytes[i++], bytes[i++], bytes[i++]);
                        int z = Ints.fromBytes(bytes[i++], bytes[i++], bytes[i++], bytes[i++]);
                        int fromType = Ints.fromBytes(bytes[i++], bytes[i++], bytes[i++], bytes[i++]);
                        int toType = Ints.fromBytes(bytes[i++], bytes[i++], bytes[i++], bytes[i++]);
                        byte fromData = bytes[i++];
                        byte toData = bytes[i++];

                        Vector position = new Vector(x, y, z);
                        BaseBlock from = new BaseBlock(fromType, fromData);
                        BaseBlock to = new BaseBlock(toType, toData);
                        transactions[j++] = new WETransaction(position, from, to);
                    }
                    if (transactions.length > 0) {
                        return Optional.of(new WEMassChange(transactions));
                    }
                } catch (Exception e) {
                    throw new InvalidDataException(e);
                }
            }
            throw new InvalidDataException("Missing DataQuery: " + TRANSACTIONS.toString());
        }
    }
}
