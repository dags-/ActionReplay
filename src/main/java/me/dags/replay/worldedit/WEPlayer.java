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

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

/**
 * @author dags <dags@dags.me>
 */
public class WEPlayer extends AbstractPlayerActor {

    private final String name;
    private final UUID uuid;

    private WeakReference<Player> player = new WeakReference<>(null);

    public WEPlayer(String name, UUID uuid) {
        this.name = name;
        this.uuid = uuid;
    }

    private Player get() {
        Player delegate = player.get();
        if (delegate == null) {
            delegate = WorldEdit.getInstance().getServer().matchPlayer(this);
            player = new WeakReference<>(delegate);
            if (delegate == null) {
                throw new UnsupportedOperationException("could not find player " + name);
            }
        }
        return delegate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueId() {
        return uuid;
    }

    @Override
    public World getWorld() {
        return get().getWorld();
    }

    @Override
    public int getItemInHand() {
        return get().getItemInHand();
    }

    @Override
    public void giveItem(int i, int i1) {
        get().giveItem(i, i1);
    }

    @Override
    public BlockBag getInventoryBlockBag() {
        return get().getInventoryBlockBag();
    }

    @Override
    public WorldVector getPosition() {
        return get().getPosition();
    }

    @Override
    public double getPitch() {
        return get().getPitch();
    }

    @Override
    public double getYaw() {
        return get().getYaw();
    }

    @Override
    public void setPosition(Vector vector, float v, float v1) {
        get().setPosition(vector, v, v1);
    }

    @Nullable
    @Override
    public BaseEntity getState() {
        return get().getState();
    }

    @Override
    public Location getLocation() {
        return get().getLocation();
    }

    @Override
    public void printRaw(String s) {
        get().printRaw(s);
    }

    @Override
    public void printDebug(String s) {
        get().printDebug(s);
    }

    @Override
    public void print(String s) {
        get().print(s);
    }

    @Override
    public void printError(String s) {
        get().printError(s);
    }

    @Override
    public SessionKey getSessionKey() {
        return get().getSessionKey();
    }

    @Nullable
    @Override
    public <T> T getFacet(Class<? extends T> aClass) {
        return get().getFacet(aClass);
    }

    @Override
    public String[] getGroups() {
        return get().getGroups();
    }

    @Override
    public boolean hasPermission(String s) {
        return get().hasPermission(s);
    }
}
