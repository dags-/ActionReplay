package me.dags.actionreplay.serialize;

import com.flowpowered.math.vector.Vector3d;
import com.google.common.reflect.TypeToken;
import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.BlockChange;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;

import java.util.List;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class Tokens {

    public static final TypeToken<AvatarSnapshot> AVATAR = TypeToken.of(AvatarSnapshot.class);
    public static final TypeToken<Frame> FRAME = TypeToken.of(Frame.class);
    public static final TypeToken<BlockChange> BLOCK_CHANGE = TypeToken.of(BlockChange.class);

    public static final TypeToken<UUID> UUID = TypeToken.of(UUID.class);
    public static final TypeToken<Vector3d> VECTOR_3D = TypeToken.of(Vector3d.class);
    public static final TypeToken<List<Text>> TEXT_LIST = new TypeToken<List<Text>>(){};
    public static final TypeToken<ItemStackSnapshot> ITEM_STACK_SNAPSHOT = TypeToken.of(ItemStackSnapshot.class);
    public static final TypeToken<BlockSnapshot> BLOCK_SNAPSHOT = new TypeToken<BlockSnapshot>(){};
}
