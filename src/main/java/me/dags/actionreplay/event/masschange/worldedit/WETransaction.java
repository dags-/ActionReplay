package me.dags.actionreplay.event.masschange.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BaseBlock;
import me.dags.actionreplay.event.masschange.BinaryBlockTransaction;
import me.dags.actionreplay.io.ByteArrayDataOutputStream;

import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
class WETransaction implements BinaryBlockTransaction {

    final Vector position;
    final BaseBlock from;
    final BaseBlock to;

    WETransaction(Vector pos, BaseBlock from, BaseBlock to) {
        this.position = pos;
        this.from = from;
        this.to = to;
    }

    @Override
    public void writeTo(ByteArrayDataOutputStream outputStream) throws IOException {
        outputStream.writeInt(position.getBlockX());
        outputStream.writeInt(position.getBlockY());
        outputStream.writeInt(position.getBlockZ());
        outputStream.writeInt(from.getId());
        outputStream.writeInt(to.getId());
        outputStream.write(from.getData());
        outputStream.write(to.getData());
    }
}
