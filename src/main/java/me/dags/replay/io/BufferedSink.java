package me.dags.replay.io;

import com.sk89q.jnbt.CompoundTag;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.serialize.DataView;
import me.dags.replay.util.CancellableTask;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author dags <dags@dags.me>
 */
public class BufferedSink extends CancellableTask implements FrameSink {

    private final FrameSink sink;
    private final ConcurrentLinkedQueue<DataView> buffer = new ConcurrentLinkedQueue<>();

    public BufferedSink(FrameSink sink) {
        this.sink = sink;
    }

    public void start(Object plugin) {
        Task.builder().execute(this).delayTicks(1).intervalTicks(10).async().submit(plugin);
    }

    public void stop() {
        cancel();
    }

    @Override
    public void skipHeader() throws IOException {
        if (isCancelled()) {
            return;
        }
        sink.skipHeader();
    }

    @Override
    public void write(DataView frame) {
        if (isCancelled()) {
            return;
        }
        // serialize frame to nbt and then queue for writing
        buffer.add(new BufferedFrame(frame.getData()));
    }

    @Override
    public void writeHeader(DataView header) throws IOException {
        if (isCancelled()) {
            return;
        }
        sink.writeHeader(header);
    }

    @Override
    public void run() {
        if (!buffer.isEmpty()) {
            drain();
        }
    }

    @Override
    public void close() {
        // flush buffer
        while (!buffer.isEmpty()) {
            drain();
        }

        try {
            sink.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drain() {
        DataView frame = buffer.poll();
        try {
            sink.write(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class BufferedFrame implements DataView {

        private final CompoundTag data;

        private BufferedFrame(CompoundTag data) {
            this.data = data;
        }

        @Override
        public CompoundTag getData() {
            return data;
        }
    }
}
