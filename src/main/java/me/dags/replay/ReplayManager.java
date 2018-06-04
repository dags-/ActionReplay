package me.dags.replay;

import com.flowpowered.math.vector.Vector3i;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import me.dags.commandbus.fmt.Fmt;
import me.dags.config.Config;
import me.dags.config.Node;
import me.dags.replay.frame.FrameRecorder;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.frame.FrameSource;
import me.dags.replay.replay.Replay;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.AABB;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

/**
 * @author dags <dags@dags.me>
 */
public class ReplayManager implements CatalogRegistryModule<ReplayFile> {

    private static final String EXTENSION = ".replay";

    private final Object plugin;
    private final File replayDir;
    private final Config config;
    private final Map<String, ReplayFile> registry = new HashMap<>();

    private FrameRecorder recorder = FrameRecorder.NONE;
    private Replay replay = Replay.NONE;

    ReplayManager(Object plugin, File configDir) {
        this.plugin = plugin;
        this.replayDir = new File(configDir, "replays");
        this.config = Config.must(configDir.getAbsolutePath(), "config.conf");
        replayDir.mkdirs();
    }

    @Override
    public Optional<ReplayFile> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Override
    public Collection<ReplayFile> getAll() {
        return registry.values();
    }

    public boolean isRecording() {
        return !replay.isPresent() && recorder.isPresent() && recorder.isRecording();
    }

    public boolean isplaying() {
        return !recorder.isPresent() && replay.isPresent() && replay.isPlaying();
    }

    public Text createRecorder(String name, World world, AABB bounds) {
        if (replay.isPresent()) {
            return Fmt.error("Cannot set up recorder while a replay is active").build();
        }

        if (recorder.isPresent()) {
            return Fmt.error("Cannot set up a new recorder while a recorder is active").build();
        }

        File replay = new File(replayDir, name + EXTENSION);
        if (replay.exists()) {
            return Fmt.error("A replay by that name already exists").build();
        }

        try {
            ReplayFile file = new ReplayFile(name, replay);
            FrameSink sink = file.getSink();
            recorder = new FrameRecorder(world, bounds, sink, this);
            updateRegistry();
            return Fmt.info("Successfully created recorder").build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Fmt.error("Unable to create file sink").build();
        }
    }

    public Text startRecorder() {
        if (replay.isPresent()) {
            return Fmt.error("Cannot start recorder while a replay is active").build();
        }
        if (recorder.isAbsent()) {
            return Fmt.error("A recorder has not been set up yet").build();
        }
        if (recorder.isRecording()) {
            return Fmt.error("The recorder is already recording").build();

        }
        recorder.start(plugin);
        return Fmt.error("Recorder successfully started").build();
    }

    public Text stopRecorder() {
        if (!recorder.isPresent()) {
            return Fmt.error("A recorder has not been set up yet").build();
        }
        if (!recorder.isRecording()) {
            return Fmt.error("The recorder is not currently recording").build();
        }
        recorder.stop();
        return Fmt.info("Stopping recorder...").build();
    }

    public Text loadReplay(Location<World> origin, ReplayFile file) {
        if (recorder.isPresent()) {
            return Fmt.error("Cannot load replay while a recorder is active").build();
        }

        if (!file.exists()) {
            return Fmt.error("Cannot find replay by that name").build();
        }

        try {
            FrameSource source = file.getSource();
            replay = new Replay(source, origin, this);
            return Fmt.info("Loaded replay in ").stress(origin.getExtent().getName())
                    .info(" at ").stress(origin.getBlockPosition()).build();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return Fmt.error("Unable to load replay %s", file.getName()).build();
        }
    }

    public Text startReplay(int intervalTicks) {
        if (!replay.isPresent()) {
            return Fmt.error("A replay has not been loaded").build();
        }
        if (replay.isPlaying()) {
            return Fmt.error("The replay is already playing").build();
        }
        replay.start(plugin, intervalTicks);
        return Fmt.info("Starting replay...").build();
    }

    public Text stopReplay() {
        if (!replay.isPresent()) {
            return Fmt.error("A replay has not been loaded").build();
        }
        if (!replay.isPlaying()) {
            return Fmt.error("The replay is not currently playing").build();
        }
        replay.stop();
        return Fmt.info("Stopping replay...").build();
    }

    public Text delete(ReplayFile file) {
        if (replay.isPlaying() || recorder.isPresent()) {
            return Fmt.error("Cannot perform delete while recorder/replay is loaded").build();
        }
        if (!file.exists()) {
            return Fmt.error("Replay file does not exist").build();
        }
        if (!file.delete()) {
            return Fmt.error("Unable to delete replay file").build();
        }
        updateRegistry();
        return Fmt.info("Successfully deleted replay file").build();
    }

    public void onRecorderStarted() {
        recorder.writeTo(config);
        config.save();
        Fmt.info("Recorder started").tell(Sponge.getServer().getBroadcastChannel());
    }

    public void onRecorderStopped() {
        config.clear();
        config.save();
        recorder = FrameRecorder.NONE;
        Fmt.info("Recording stopped").tell(Sponge.getServer().getBroadcastChannel());
    }

    public void onReplayStarted() {
        Fmt.info("Replay started").tell(Sponge.getServer().getBroadcastChannel());
    }

    public void onReplayStopped() {
        replay = Replay.NONE;
        Fmt.info("Replay stopped").tell(Sponge.getServer().getBroadcastChannel());
    }

    public boolean loadFromConfig() {
        String name = config.get("name", "");
        Optional<ReplayFile> replay = getById(name);
        if (!replay.isPresent()) {
            return false;
        }

        String worldName = config.get("world", "");
        Optional<World> world = Sponge.getServer().getWorld(worldName);
        if (!world.isPresent()) {
            return false;
        }

        try {
            FrameSink sink = replay.get().getSink();
            Vector3i min = readVec(config.node("min"));
            Vector3i max = readVec(config.node("max"));
            AABB bounds = new AABB(min, max);
            recorder = new FrameRecorder(world.get(), bounds, sink, this);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    void updateRegistry() {
        File[] files = replayDir.listFiles();
        if (files == null || files.length == 0) {
            return;
        }

        registry.clear();
        for (File file : files) {
            if (file.getName().endsWith(EXTENSION)) {
                int length = file.getName().length() - EXTENSION.length();
                String name = file.getName().substring(0, length);
                ReplayFile replay = new ReplayFile(name, file);
                registry.put(name, replay);
            }
        }
    }

    private static Vector3i readVec(Node node) {
        return new Vector3i(node.get("x", 0), node.get("y", 0), node.get("z", 0));
    }
}
