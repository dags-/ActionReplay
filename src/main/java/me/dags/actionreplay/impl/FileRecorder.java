package me.dags.actionreplay.impl;

import com.flowpowered.math.vector.Vector3d;
import com.flowpowered.math.vector.Vector3i;
import me.dags.actionreplay.ActionReplay;
import me.dags.actionreplay.NodeUtils;
import me.dags.actionreplay.animation.Meta;
import me.dags.actionreplay.animation.Recorder;
import me.dags.actionreplay.animation.avatar.AvatarSnapshot;
import me.dags.actionreplay.animation.frame.Frame;
import me.dags.actionreplay.animation.frame.FrameWriter;
import me.dags.actionreplay.event.BlockChange;
import me.dags.actionreplay.event.Change;
import me.dags.commandbus.utils.Format;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.chat.ChatTypes;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author dags <dags@dags.me>
 */
public class FileRecorder extends Recorder {

    private final Vector3d centerD;
    private final String name;

    private Frame last = null;
    private FrameWriter writer;
    private String world = "unknown";

    public FileRecorder(Meta meta) {
        super(meta.worldId, meta.center, meta.radius, meta.height);
        this.centerD = meta.center.toDouble();
        this.name = meta.name;
    }

    public FileRecorder(String name, UUID worldId, Vector3i center, int radius, int height) {
        super(worldId, center, radius, height);
        this.centerD = center.toDouble();
        this.name = name;
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        format().info("Recording: ").stress(name).info(" in world: ").stress(world).tell(event.getTargetEntity());
    }

    @Override
    public void setRecording(boolean recording) {
        super.setRecording(recording);
        Meta meta = toMeta();
        meta.recording = isRecording();
        NodeUtils.saveMeta(meta);
    }

    @Override
    public void start(Object plugin) {
        super.start(plugin);
        this.world = Sponge.getServer().getWorld(worldId).map(World::getName).orElse("unknown");
        try {
            this.writer = new FrameWriter(ActionReplay.getRecordingFile(name));

            Task.builder()
                    .interval(500, TimeUnit.MILLISECONDS)
                    .delay(500, TimeUnit.MILLISECONDS)
                    .execute(writer)
                    .async()
                    .submit(plugin);

            Task.builder()
                    .interval(ActionReplay.getInstance().getConfig().announceInterval, TimeUnit.SECONDS)
                    .execute(task -> {
                        if (isPresent() && isRecording() && !Sponge.getServer().getOnlinePlayers().isEmpty()) {
                            Text message = format().info("Recording: ").stress(name).build();
                            Sponge.getServer().getBroadcastChannel().send(message, ChatTypes.ACTION_BAR);
                        } else {
                            task.cancel();
                        }
                    })
                    .submit(plugin);
        } catch (IOException e) {
            e.printStackTrace();
            stop();
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (writer != null) {
            writer.interrupt();
        }
    }

    @Override
    public void addNextFrame(AvatarSnapshot snapshot, Change change) {
        if (change instanceof BlockChange && writer != null) {
            Frame next;
            if (last == null) {
                next = new Frame(snapshot, change);
            } else {
                next = new Frame(snapshot, change, last.getRelativeAvatars(centerD));
            }
            last = next;
            writer.queue(next);
        }
    }

    private Meta toMeta() {
        Meta meta = new Meta();
        meta.name = name;
        meta.worldId = worldId;
        meta.center = center;
        meta.radius = radius;
        meta.height = height;
        return meta;
    }

    private static Format format() {
        return ActionReplay.getInstance().getFormat();
    }
}
