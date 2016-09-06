package me.dags.actionreplay.replay;

import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.replay.frame.FrameProvider;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayTask implements Consumer<Task> {

    private final ReplayPlayer framePlayer;
    private final Location<World> center;
    private final Runnable finishCallback;
    private final int intervalTicks;

    private boolean interrupted = false;
    private int ticker = 0;

    public ReplayTask(FrameProvider provider, Runnable callback, Location<World> center, int interval, int operations) {
        this.framePlayer = new ReplayPlayer(provider, operations);
        this.center = center;
        this.finishCallback = callback;
        this.intervalTicks = interval;
        this.ticker = interval;
    }

    public void stop() {
        interrupt();
        Sponge.getEventManager().unregisterListeners(this);
        try {
            framePlayer.stop();
            framePlayer.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void interrupt() {
        this.interrupted = true;
    }


    public boolean isInterrupted() {
        return interrupted;
    }

    @Listener (order = Order.PRE)
    public void damageListener(DamageEntityEvent event) {
        if (framePlayer.protect(event.getTargetEntity())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void accept(Task task) {
        try {
            if (isInterrupted()) {
                task.cancel();
                finishCallback.run();
                Task.builder().delayTicks(5).execute(this::stop).submit(ActionReplay.getInstance());
                return;
            }

            if (framePlayer.finished()) {
                interrupt();
                return;
            }

            if (ticker-- > 0) {
                framePlayer.pause();
                framePlayer.loadNext();
            } else {
                ticker = intervalTicks;
                framePlayer.playNext(center);
            }
        } catch (Exception e) {
            e.printStackTrace();
            task.cancel();
            stop();
        }
    }
}
