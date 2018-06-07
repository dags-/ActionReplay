package me.dags.replay.manager;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import me.dags.commandbus.fmt.Fmt;
import me.dags.config.Config;
import me.dags.replay.frame.FrameRecorder;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.replay.Replay;
import me.dags.replay.replay.ReplayFile;
import me.dags.replay.replay.ReplayMeta;
import me.dags.replay.util.OptionalActivity;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.text.channel.MessageReceiver;

/**
 * @author dags <dags@dags.me>
 */
public class Manager {

    private final Object plugin;
    private final Config config;
    private final Registry registry;
    private Instance instance = Instance.NONE;

    public Manager(Object plugin, File configDir) {
        File replayDir = new File(configDir, "replays");
        this.plugin = plugin;
        this.registry = new Registry(replayDir);
        this.config = Config.must(configDir.toPath(), "config.conf");
        if (!replayDir.exists() && !replayDir.mkdirs()) {
            new IOException("Unable to create replay directory").printStackTrace();
        }
    }

    public Registry getRegistry() {
        return registry;
    }

    public Instance getActive() {
        return instance;
    }

    public void attachReplayFile(MessageReceiver receiver, ReplayFile replay) {
        instance.dispose();
        instance = new Instance(replay);
        try (FrameSource source = replay.getSource()) {
            instance.setMeta(source.header());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Fmt.info("Set current replay file: ").stress(replay.getName()).tell(receiver);
    }

    public void attachRecorder(MessageReceiver receiver) {
        if (canAttachActivity(receiver, "recorder")) {
            try {
                ReplayMeta meta = instance.getMeta();
                FrameSink sink = instance.getReplayFile().getSink();
                sink.goToEnd();

                FrameRecorder recorder = new FrameRecorder(meta, sink);
                instance.setRecorder(recorder);
                Fmt.info("Attached new recorder at ").stress(meta.getOrigin().getBlockPosition()).tell(receiver);
            } catch (IOException e) {
                e.printStackTrace();
                Fmt.warn("An error occurred whilst creating the recorder, see console").tell(receiver);
            }
        }
    }

    public void attachReplay(MessageReceiver receiver) {
        if (canAttachActivity(receiver, "replay")) {
            try {
                ReplayFile file = instance.getReplayFile();
                ReplayMeta meta = instance.getMeta();
                FrameSource source = instance.getReplayFile().getSource();
                source.header();

                Replay replay = new Replay(file, meta, source);
                instance.setReplay(replay);
                Fmt.info("Attached replay ").stress(file.getName()).tell(receiver);
            } catch (IOException e) {
                e.printStackTrace();
                Fmt.warn("An error occurred whilst loading the replay, see console").tell(receiver);
            }
        }
    }

    public void startRecorder(MessageReceiver receiver) {
        if (canStartActivity(receiver, instance.getRecorder(), instance.getRecorder())) {
            Fmt.info("Starting recorder...").tell(receiver);
            instance.getRecorder().start(plugin);
            updateConfig();
        }
    }

    public void startReplay(MessageReceiver receiver, int ticks) {
        if (canStartActivity(receiver, instance.getReplay(), instance.getRecorder())) {
            Fmt.info("Starting replay...").tell(receiver);
            instance.getReplay().start(plugin, ticks);
        }
    }

    public void stop(MessageReceiver receiver) {
        if (instance.isAbsent()) {
            Fmt.info("Nothing to stop").tell(receiver);
            return;
        }
        if (instance.getRecorder().isActive()) {
            stopRecorder(receiver);
            updateConfig();
        }
        if (instance.getReplay().isActive()) {
            stopReplay(receiver);
        }
    }

    public void delete(MessageReceiver receiver, ReplayFile replay) {
        if (!replay.exists()) {
            Fmt.error("File does not exist").tell(receiver);
            return;
        }
        if (instance.getReplayFile() == replay) {
            stop(receiver);
        }
        if (!replay.delete()) {
            Fmt.error("Unable to delete file").tell(receiver);
        }
        Fmt.info("Deleted file").tell(receiver);
        getRegistry().update();
    }

    public void loadFromConfig() {
        String name = config.node("last").get("name", "");
        boolean recording = config.node("last").get("recording", false);
        Optional<ReplayFile> replay = getRegistry().getById(name);
        if (replay.isPresent()) {
            attachReplayFile(Sponge.getServer().getConsole(), replay.get());
            if (recording) {
                attachRecorder(Sponge.getServer().getConsole());
                startRecorder(Sponge.getServer().getConsole());
            }
        }
    }

    private void stopRecorder(MessageReceiver receiver) {
        if (canStopActivity(receiver, instance.getRecorder())) {
            Fmt.info("Stopping recorder...").tell(receiver);
            instance.getRecorder().stop();
        }
    }

    private void stopReplay(MessageReceiver receiver) {
        if (canStopActivity(receiver, instance.getReplay())) {
            Fmt.info("Stopping replay...").tell(receiver);
            instance.getReplay().stop();
        }
    }

    private boolean canAttachActivity(MessageReceiver receiver, String activity) {
        if (instance.isAbsent()) {
            Fmt.error("No replay currently set up").tell(receiver);
            return false;
        }
        if (instance.getRecorder().isPresent() && instance.getRecorder().isActive()) {
            Fmt.error("Cannot attach %s whilst recorder is active", activity).tell(receiver);
            return false;
        }
        if (instance.getReplay().isPresent() && instance.getReplay().isActive()) {
            Fmt.error("Cannot attach %s whilst replay is active", activity).tell(receiver);
            return false;
        }
        return true;
    }

    private boolean canStartActivity(MessageReceiver receiver, OptionalActivity activity, OptionalActivity not) {
        if (instance.isAbsent()) {
            Fmt.error("No replay currently set up").tell(receiver);
            return false;
        }
        if (activity.isAbsent()) {
            Fmt.error("No %s currently attached", activity.getName()).tell(receiver);
            return false;
        }
        if (activity.isActive()) {
            Fmt.error("A %s is already active", activity.getName()).tell(receiver);
            return false;
        }
        if (not.isPresent() && not.isActive()) {
            Fmt.error("Cannot start %s whilst a %s is active", activity.getName(), not.getName()).tell(receiver);
            return false;
        }
        return true;
    }

    private boolean canStopActivity(MessageReceiver receiver, OptionalActivity activity) {
        if (instance.isAbsent()) {
            Fmt.error("No replay currently set up").tell(receiver);
            return false;
        }
        if (activity.isAbsent()) {
            Fmt.error("No %s currently attached", activity.getName()).tell(receiver);
            return false;
        }
        if (!activity.isActive()) {
            Fmt.error("A %s is not currently active").tell(receiver);
            return false;
        }
        return true;
    }

    private void updateConfig() {
        if (instance.isAbsent()) {
            return;
        }

        if (instance.getRecorder().isPresent() && instance.getRecorder().isActive()) {
            config.node("last").set("name", instance.getReplayFile().getId());
            config.node("last").set("recording", true);
            config.save();
        } else {
            config.clear();
            config.save();
        }
    }
}
