package me.dags.replay.worldedit;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldVector;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Player;
import com.sk89q.worldedit.extension.platform.AbstractPlayerActor;
import com.sk89q.worldedit.extent.inventory.BlockBag;
import com.sk89q.worldedit.session.SessionKey;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.World;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.Nullable;

/**
 * @author dags <dags@dags.me>
 */
public final class WEPlayerMatcher extends AbstractPlayerActor {

    private static final ThreadLocal<WEPlayerMatcher> cache = ThreadLocal.withInitial(WEPlayerMatcher::new);

    private String name = "";
    private UUID id = UUID.randomUUID();

    private WEPlayerMatcher() {}

    public String getName() {
        return name;
    }

    public UUID getUniqueId() {
        return id;
    }

    public WEPlayerMatcher set(String name, UUID id) {
        this.name = name;
        this.id = id;
        return this;
    }

    @Override
    public World getWorld() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getItemInHand() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void giveItem(int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        throw new UnsupportedOperationException();
    }

    @Override
    public WorldVector getPosition() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getPitch() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getYaw() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPosition(Vector vector, float v, float v1) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public BaseEntity getState() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Location getLocation() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printRaw(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printDebug(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void print(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void printError(String s) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionKey getSessionKey() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> aClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String[] getGroups() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasPermission(String s) {
        throw new UnsupportedOperationException();
    }

    public static Optional<Player> match(String name, UUID uuid) {
        WEPlayerMatcher matcher = cache.get();
        Player player = WorldEdit.getInstance().getServer().matchPlayer(matcher);
        return Optional.ofNullable(player);
    }

    public static Optional<Player> match(org.spongepowered.api.entity.living.player.Player player) {
        return match(player.getName(), player.getUniqueId());
    }
}
