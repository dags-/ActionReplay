package me.dags.replay.manager;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import me.dags.replay.replay.ReplayFile;
import org.spongepowered.api.registry.CatalogRegistryModule;

/**
 * @author dags <dags@dags.me>
 */
public class Registry implements CatalogRegistryModule<ReplayFile> {

    private static final String EXTENSION = ".replay";

    private final Map<String, ReplayFile> registry = new HashMap<>();
    private final File replayDir;

    public Registry(File replayDir) {
        this.replayDir = replayDir;
    }

    @Override
    public Optional<ReplayFile> getById(String id) {
        return Optional.ofNullable(registry.get(id));
    }

    @Override
    public Collection<ReplayFile> getAll() {
        return registry.values();
    }

    public ReplayFile newReplayFile(String name) {
        File file = new File(replayDir, name + EXTENSION);
        ReplayFile replay = new ReplayFile(name, file);
        registry.put(replay.getId(), replay);
        return replay;
    }

    public void update() {
        registry.clear();

        File[] files = replayDir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.getName().endsWith(EXTENSION)) {
                int length = file.getName().length() - EXTENSION.length();
                String name = file.getName().substring(0, length);
                ReplayFile replay = new ReplayFile(name, file);
                registry.put(replay.getId(), replay);
            }
        }
    }
}
