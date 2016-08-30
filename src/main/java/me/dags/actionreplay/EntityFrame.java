package me.dags.actionreplay;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.entity.SpawnEntityEvent;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author dags <dags@dags.me>
 */
public class EntityFrame extends KeyFrame.TargetAvatar {

    private final List<WeakReference<Entity>> entities;

    public EntityFrame(Avatar avatar, SpawnEntityEvent event) {
        super(avatar);
        List<Entity> entityList = event.getEntities();
        this.entities = new ArrayList<>(entityList.size());
        this.entities.addAll(entityList.stream().map(WeakReference::new).collect(Collectors.toList()));
    }

    @Override
    public void restore() {
        Iterator<WeakReference<Entity>> iterator = entities.iterator();
        while (iterator.hasNext()) {
            hide(iterator, false);
        }
    }

    @Override
    public void reset() {
        Iterator<WeakReference<Entity>> iterator = entities.iterator();
        while (iterator.hasNext()) {
            hide(iterator, true);
        }
    }

    private void hide(Iterator<WeakReference<Entity>> iterator, boolean value) {
        Entity entity = iterator.next().get();
        if (entity != null && !entity.isRemoved()) {
            entity.offer(Keys.INVISIBLE, value);
        } else {
            iterator.remove();
        }
    }
}
