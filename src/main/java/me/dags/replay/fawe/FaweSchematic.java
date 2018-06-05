package me.dags.replay.fawe;

import com.boydti.fawe.FaweAPI;
import com.boydti.fawe.object.schematic.Schematic;
import com.flowpowered.math.vector.Vector3i;
import com.sk89q.jnbt.CompoundTag;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import me.dags.replay.frame.schematic.Schem;
import me.dags.replay.serialize.Serializer;
import me.dags.replay.serialize.TagBuilder;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public class FaweSchematic implements Schem {

    private final Schematic schematic;

    FaweSchematic(Schematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public boolean isPresent() {
        return schematic != null;
    }

    @Override
    public String getType() {
        return "fawe";
    }

    @Override
    public void apply(Location<World> location) {
        if (isAbsent()) {
            return;
        }
        Vector3i pos = location.getBlockPosition();
        Vector vector = new Vector(pos.getX(), pos.getY(), pos.getZ());
        com.sk89q.worldedit.world.World world = FaweAPI.getWorld(location.getExtent().getName());
        schematic.paste(world, vector, false, true, null);
    }

    @Override
    public byte[] getBytes() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            schematic.save(out, ClipboardFormat.SCHEMATIC);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    public static final Serializer<FaweSchematic> SERIALIZER = new Serializer<FaweSchematic>() {
        @Override
        public void serialize(FaweSchematic schem, TagBuilder builder) {
            builder.put("data", schem.getBytes());
        }

        @Override
        public FaweSchematic deserialize(CompoundTag tag) {
            try {
                ByteArrayInputStream in = new ByteArrayInputStream(tag.getByteArray("data"));
                Schematic schematic = ClipboardFormat.SCHEMATIC.load(in);
                return new FaweSchematic(schematic);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }
    };
}
