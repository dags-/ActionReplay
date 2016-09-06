package me.dags.actionreplay.event.masschange.worldedit;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.Support;
import me.dags.actionreplay.event.Ids;
import me.dags.actionreplay.event.masschange.MassChangeBuilder;
import me.dags.actionreplay.replay.Recorder;

/**
 * @author dags <dags@dags.me>
 */
public class WESupport implements Support.Hook {

    @Override
    public void init() {
        WorldEdit.getInstance().getEventBus().register(this);
        MassChangeBuilder.register(Ids.WORLD_EDIT, new WEMassChange.Builder());
    }

    @Subscribe
    public void onEditSession(EditSessionEvent event) {
        if (event.getStage() != EditSession.Stage.BEFORE_REORDER) {
            return;
        }

        World world = event.getWorld();
        if (world != null && getRecorder().isRecording() && world.getName().equals(getRecorder().getWorldName())) {
            WEExtent extent = new WEExtent(getRecorder(), event.getExtent());
            event.setExtent(extent);
        }
    }

    private static Recorder getRecorder() {
        return ActionReplay.getInstance().getRecorder();
    }
}
