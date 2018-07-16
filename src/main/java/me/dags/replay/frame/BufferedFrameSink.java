package me.dags.replay.frame;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.dags.replay.io.Sink;
import me.dags.replay.replay.ReplayMeta;
import me.dags.replay.util.CancellableTask;
import org.jnbt.CompoundTag;
import org.spongepowered.api.scheduler.Task;

/**
 * @author dags <dags@dags.me>
 */
public class BufferedFrameSink extends CancellableTask implements Sink {

    private final Sink<CompoundTag> sink;
    private final ConcurrentLinkedQueue<CompoundTag> buffer = new ConcurrentLinkedQueue<>();

    public BufferedFrameSink(Sink<CompoundTag> sink) {
        this.sink = sink;
    }

    public void start(Object plugin) {
        Task.builder().execute(this).delayTicks(1).intervalTicks(10).async().submit(plugin);
    }

    public void stop() {
        cancel();
    }

    @Override
    public void goToEnd() {
        if (isCancelled()) {
            return;
        }
        try {
            sink.goToEnd();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Object frame) throws IOException {

    }

    @Override
    public void writeHeader(Object header) throws IOException {

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

    public void writeHeader(ReplayMeta meta) {
        if (isCancelled()) {
            return;
        }
        try {
            CompoundTag node = ReplayMeta.SERIALIZER.serialize(meta);
            sink.writeHeader(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(Frame view) {
        if (isCancelled()) {
            return;
        }

        CompoundTag node = Frame.SERIALIZER.serialize(view);
        if (node.isPresent()) {
            buffer.add(node);
        }
    }

    private void drain() {
        try {
            CompoundTag frame = buffer.poll();
            sink.write(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
