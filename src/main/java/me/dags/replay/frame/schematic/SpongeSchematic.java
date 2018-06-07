package me.dags.replay.frame.schematic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;
import me.dags.replay.util.Buffers;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.data.persistence.InvalidDataException;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;
import org.spongepowered.api.world.schematic.Schematic;

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

    private static SpongeSchematic NONE = new SpongeSchematic(null);

    public static final Serializer<SpongeSchematic> SERIALIZER = new Serializer<SpongeSchematic>() {
        @Override
        public void serialize(SpongeSchematic schem, Node node) {
            ByteArrayOutputStream data = Buffers.getCachedBuffer();
            try (OutputStream out = new GZIPOutputStream(data)) {
                DataContainer container = DataTranslators.SCHEMATIC.translate(schem.schematic);
                DataFormats.NBT.writeTo(out, container);
            } catch (IOException e) {
                e.printStackTrace();
            }
            node.put("data", data.toByteArray());
        }

        @Override
        public SpongeSchematic deserialize(Node node) {
            ByteArrayInputStream data = new ByteArrayInputStream(node.getBytes("data"));
            try (InputStream in = new GZIPInputStream(data)) {
                DataContainer container = DataFormats.NBT.readFrom(in);
                Schematic schematic = DataTranslators.SCHEMATIC.translate(container);
                return new SpongeSchematic(schematic);
            } catch (IOException | InvalidDataException e) {
                e.printStackTrace();
                return SpongeSchematic.NONE;
            }
        }
    };
}
