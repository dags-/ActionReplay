package me.dags.replay.frame.entity;

import com.flowpowered.math.vector.Vector3d;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import me.dags.replay.data.Serializer;
import me.dags.replay.data.Serializers;
import me.dags.replay.util.Buffers;
import org.jnbt.CompoundTag;
import org.spongepowered.api.data.DataContainer;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityArchetype;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class EntityCreate implements EntityChange {

    private final UUID id;
    private final Vector3d offset;
    private final EntityArchetype archetype;

    public EntityCreate(Entity entity, Vector3d offset) {
        this.id = entity.getUniqueId();
        this.offset = offset;
        this.archetype = entity.createArchetype();
    }

    private EntityCreate(EntityArchetype archetype, UUID id, Vector3d offset) {
        this.id = id;
        this.archetype = archetype;
        this.offset = offset;
    }

    @Override
    public boolean isPresent() {
        return this != NONE;
    }

    @Override
    public void apply(Location<World> origin) {
        archetype.apply(origin.add(offset)).ifPresent(entity -> EntityTracker.store(id, entity.getUniqueId()));
    }

    @Override
    public String getType() {
        return "entity.create";
    }

    private static final EntityCreate NONE = new EntityCreate(null, UUID.randomUUID(), Vector3d.ZERO);

    public static Serializer<EntityCreate> SERIALIZER = new Serializer<EntityCreate>() {
        @Override
        public EntityCreate deserialize(CompoundTag root) {
            UUID id = Serializers.uuid(root, "id0", "id1");
            Vector3d offset = Serializers.vec3d(root, "x", "y", "z");
            ByteArrayInputStream data = new ByteArrayInputStream(root.getBytes("data"));
            try (InputStream in = new GZIPInputStream(data)) {
                DataContainer container = DataFormats.NBT.readFrom(in);
                Optional<EntityArchetype> archetype = EntityArchetype.builder().build(container);
                if (archetype.isPresent()) {
                    return new EntityCreate(archetype.get(), id, offset);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return NONE;
        }

        @Override
        public void serialize(EntityCreate create, CompoundTag root) {
            Serializers.uuid(root, "id0", "id1", create.id);
            Serializers.vec3d(root, "x", "y", "z", create.offset);
            ByteArrayOutputStream data = Buffers.getCachedBuffer();
            try (OutputStream out = new GZIPOutputStream(data)) {
                DataContainer container = create.archetype.toContainer();
                DataFormats.NBT.writeTo(out, container);
            } catch (IOException e) {
                e.printStackTrace();
            }
            root.put("data", data.toByteArray());
        }
    };
}
