package me.dags.replay.io;

import com.sk89q.jnbt.CompoundTag;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.frame.FrameView;
import me.dags.replay.replay.ReplayContext;
import me.dags.replay.util.CancellableTask;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author dags <dags@dags.me>
 */
public class BufferedFrameSink extends CancellableTask implements FrameSink {

    private final FrameSink sink;
    private final ConcurrentLinkedQueue<FrameView> buffer = new ConcurrentLinkedQueue<>();

    private volatile boolean stopped = false;

    public BufferedFrameSink(FrameSink sink) {
        this.sink = sink;
    }

    public void start(Object plugin) {
        startAsync(plugin);
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void accept(FrameView frame) {
        if (stopped) {
            return;
        }
        // serialize frame to nbt and then queue for writing
        buffer.add(new BufferedFrame(frame.toData()));
    }

    @Override
    public void run() {
        if (!buffer.isEmpty()) {
            FrameView frame = buffer.poll();
            try {
                sink.accept(frame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispose() {
        // flush remaining frames
        run();
    }

    private static class BufferedFrame implements FrameView {

        private final CompoundTag data;

        private BufferedFrame(CompoundTag data) {
            this.data = data;
        }

        @Override
        public CompoundTag toData() {
            return data;
        }

        @Override
        public void apply(Location<World> origin, ReplayContext context) {

        }

        @Override
        public boolean isPresent() {
            return true;
        }
    }
}
