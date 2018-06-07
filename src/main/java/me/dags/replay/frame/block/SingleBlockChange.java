package me.dags.replay.frame.block;

import com.flowpowered.math.vector.Vector3i;
import me.dags.replay.data.Node;
import me.dags.replay.data.Serializer;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.effect.particle.ParticleEffect;
import org.spongepowered.api.effect.particle.ParticleTypes;
import org.spongepowered.api.effect.sound.SoundType;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class SingleBlockChange implements BlockChange {

    private final Vector3i offset;
    private final BlockState state;

    public SingleBlockChange(BlockState state, Vector3i offset) {
        this.offset = offset;
        this.state = state;
    }

    @Override
    public String getType() {
        return "single";
    }

    @Override
    public void apply(Location<World> origin) {
        Location<World> location = origin.add(offset);
        if (location.getExtent().containsBlock(location.getBlockPosition())) {
            BlockType original = location.getBlockType();
            if (!location.setBlock(state)) {
                return;
            }
            if (state.getType() == BlockTypes.AIR) {
                onBreakBlock(location, original);
            } else {
                onPlaceBlock(location, state.getType());
            }
        }
    }

    private void onPlaceBlock(Location<World> location, BlockType type) {
        SoundType sound = type.getSoundGroup().getPlaceSound();
        location.getExtent().playSound(sound, location.getPosition(), type.getSoundGroup().getVolume());
    }

    private void onBreakBlock(Location<World> location, BlockType type) {
        SoundType sound = type.getSoundGroup().getBreakSound();
        ParticleEffect effect = ParticleEffect.builder()
                .type(ParticleTypes.BREAK_BLOCK)
                .quantity(1)
                .build();

        location.getExtent().playSound(sound, location.getPosition(), type.getSoundGroup().getVolume());
        location.getExtent().spawnParticles(effect, location.getPosition());
    }

    public static final Serializer<SingleBlockChange> SERIALIZER = new Serializer<SingleBlockChange>() {

        private final BlockState NONE = BlockTypes.AIR.getDefaultState();

        @Override
        public void serialize(SingleBlockChange change, Node node) {
            node.put("x", "y", "z", change.offset);
            node.put("state", change.state.toString());
        }

        @Override
        public SingleBlockChange deserialize(Node node) {
            Vector3i offset = node.getVec3i("x", "y", "z");
            String stateId = node.getString("state");
            BlockState state = Sponge.getRegistry().getType(BlockState.class, stateId).orElse(NONE);
            return new SingleBlockChange(state, offset);
        }
    };
}
