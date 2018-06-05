package me.dags.replay.frame.schematic;

import com.sk89q.jnbt.CompoundTag;
import me.dags.replay.serialize.Serializer;
import me.dags.replay.serialize.TagBuilder;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.schematic.Schematic;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author dags <dags@dags.me>
 */
public class SpongeSchematic implements Schem {

    private final Schematic schematic;

    public SpongeSchematic(Schematic schematic) {
        this.schematic = schematic;
    }

    @Override
    public boolean isPresent() {
        return schematic != null;
    }

    @Override
    public String getType() {
        return "sponge";
    }

    @Override
    public void apply(Location<World> location) {
        if (isAbsent()) {
            return;
        }
        schematic.apply(location, BlockChangeFlags.NONE);
    }

    public byte[] getBytes() {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
            try (OutputStream compressed = new GZIPOutputStream(out)) {
                DataContainer container = DataTranslators.SCHEMATIC.translate(schematic);
                DataFormats.NBT.writeTo(compressed, container);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    public static final Serializer<SpongeSchematic> SERIALIZER = new Serializer<SpongeSchematic>() {
        @Override
        public void serialize(SpongeSchematic schem, TagBuilder builder) {
            builder.put("data", schem.getBytes());
        }

        @Override
        public SpongeSchematic deserialize(CompoundTag tag) {
            try (InputStream in = new GZIPInputStream(new ByteArrayInputStream(tag.getByteArray("data")))) {
                DataContainer container = DataFormats.NBT.readFrom(in);
                Schematic schematic = DataTranslators.SCHEMATIC.translate(container);
                return new SpongeSchematic(schematic);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    };
}
