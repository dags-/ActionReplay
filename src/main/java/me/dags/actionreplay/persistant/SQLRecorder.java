package me.dags.actionreplay.persistant;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.Config;
import me.dags.actionreplay.Queries;
import me.dags.actionreplay.animation.Frame;
import me.dags.actionreplay.animation.Recorder;
import me.dags.actionreplay.avatar.AvatarSnapshot;
import me.dags.actionreplay.event.BlockChange;
import me.dags.actionreplay.event.Change;
import me.dags.commandbus.utils.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.World;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class SQLRecorder extends Recorder implements Consumer<Task> {

    private static final int ACCELERATED_RATE = 10;
    private static final int NORMAL_RATE = 5;

    private final Queue<Frame> buffer = new ArrayDeque<>();
    private final Vector3d centerD;
    private final String name;

    private boolean interrupted = false;
    private Frame last = null;
    private String world = "unknown";

    public SQLRecorder(String name, UUID worldId, Vector3i center, int radius, int height) {
        super(worldId, center, radius, height);
        this.centerD = center.toDouble();
        this.name = name;

        Config.RecorderSettings recorder = ActionReplay.getInstance().getConfig().recorderSettings;
        recorder.name = name;
        recorder.worldId = worldId;
        recorder.center = center;
        recorder.radius = radius;
        recorder.height = height;
        ActionReplay.getInstance().saveConfig();
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        format().info("Recording: ").stress(name).info(" in world: ").stress(world).tell(event.getTargetEntity());
    }

    @Override
    public void setRecording(boolean recording) {
        super.setRecording(recording);
        ActionReplay.getInstance().getConfig().recorderSettings.recording = super.isRecording();
        ActionReplay.getInstance().saveConfig();
    }

    @Override
    public void start(Object plugin) {
        super.start(plugin);
        this.interrupted = false;
        this.world = Sponge.getServer().getWorld(worldId).map(World::getName).orElse("unknown");

        ActionReplay.getDatabase().createTable(Queries.table(name));

        Task.builder()
                .intervalTicks(10)
                .execute(this)
                .submit(plugin);

        Task.builder()
                .interval(ActionReplay.getInstance().getConfig().announceInterval, TimeUnit.SECONDS)
                .execute(task -> {
                    if (isPresent() && isRecording() && !Sponge.getServer().getOnlinePlayers().isEmpty()) {
                        format().info("Recording: ").stress(name).tell(Sponge.getServer().getBroadcastChannel());
                    } else {
                        task.cancel();
                    }
                })
                .submit(plugin);
    }

    @Override
    public void stop() {
        super.stop();
        this.interrupted = true;
    }

    @Override
    public void addNextFrame(AvatarSnapshot snapshot, Change change) {
        if (change instanceof BlockChange) {
            if (last == null) {
                last = new Frame(snapshot, change);
                buffer.add(last);
            } else {
                Frame next = new Frame(snapshot, change);
                next.updateFromPrevious(last, centerD);
                last = next;
                buffer.add(next);
            }
        }
    }

    @Override
    public void accept(Task task) {
        if (interrupted) {
            task.cancel();
            return;
        }

        int size = buffer.size();
        if (size > ACCELERATED_RATE) {
            size = ACCELERATED_RATE;
        } else if (size > NORMAL_RATE) {
            size = NORMAL_RATE;
        }
        if (size > 0) {
            Frame[] frames = new Frame[size];
            for (int i = 0; i < frames.length; i++) {
                frames[i] = buffer.poll();
            }
            ActionReplay.getDatabase().writeFrame(name, frames);
        }
    }

    private static Format format() {
        return ActionReplay.getInstance().getFormat();
    }
}
