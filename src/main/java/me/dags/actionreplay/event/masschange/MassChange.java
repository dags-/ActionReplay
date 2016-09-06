package me.dags.actionreplay.event.masschange;

import me.dags.actionreplay.event.Change;
import me.dags.actionreplay.event.Ids;
import me.dags.actionreplay.event.Transactional;
import me.dags.actionreplay.io.ByteArrayDataOutputStream;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.DataQuery;
import org.spongepowered.api.data.DataView;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.Optional;

/**
 * @author dags <dags@dags.me>
 */
public interface MassChange<T extends BinaryBlockTransaction> extends Change, Transactional {

    DataQuery MASS_ID = DataQuery.of("M_ID");

    T[] getBlocks();

    byte getMID();

    void restoreOne(T block, Location<World> location) throws Exception;

    void undoOne(T block, Location<World> location) throws Exception;

    default int size() {
        return getBlocks().length;
    }

    default int restoreRange(int offset, int operations, Location<World> location) throws Exception {
        if (offset < 0 || operations < 1) {
            return 0;
        }

        T[] blocks = getBlocks();
        int count = 0;
        int pos = Math.max(offset, 0);
        int end = Math.min(pos + operations, blocks.length - 1);

        while (pos < end) {
            count++;
            restoreOne(blocks[pos++], location);
        }

        return count;
    }

    default int undo(int offset, int operations, Location<World> location) throws Exception {
        if (offset < 1 || operations < 1) {
            return 0;
        }

        T[] blocks = getBlocks();
        int count = 0;
        int pos = Math.max(blocks.length - 1 - offset, 0);
        int end = Math.max(pos - operations, 0);

        while (pos > end && pos < blocks.length - 1) {
            count++;
            undoOne(blocks[pos--], location);
        }

        return count;
    }

    @Override
    default byte getId() {
        return Ids.MASS_CHANGE;
    }

    @Override
    default void restore(Location<World> location) {
        try {
            for (T transaction : getBlocks()) {
                restoreOne(transaction, location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    default void undo(Location<World> location) {
        try {
            for (T transaction : getBlocks()) {
                undoOne(transaction, location);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    default DataContainer toContainer() {
        ByteArrayDataOutputStream data = new ByteArrayDataOutputStream(8192);
        try {
            for (T t : getBlocks()) {
                t.writeTo(data);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return versionedContainer().set(MASS_ID, getMID()).set(TRANSACTIONS, data.toByteArray());
    }

    interface Builder {

        Optional<Change> from(DataView view) throws InvalidDataException;
    }
}
