package me.dags.replay.io;

import me.dags.replay.data.Node;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.util.CancellableTask;
import org.spongepowered.api.scheduler.Task;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author dags <dags@dags.me>
 */
public class BufferedSink extends CancellableTask implements FrameSink {

    private final FrameSink sink;
    private final ConcurrentLinkedQueue<Node> buffer = new ConcurrentLinkedQueue<>();

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
    public void goToEnd() throws IOException {
        if (isCancelled()) {
            return;
        }
        sink.goToEnd();
    }

    @Override
    public void write(Node node) {
        if (isCancelled()) {
            return;
        }
        // serialize frame to nbt and then queue for writing
        buffer.add(node);
    }

    @Override
    public void writeHeader(Node node) throws IOException {
        if (isCancelled()) {
            return;
        }
        sink.writeHeader(node);
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
        Node frame = buffer.poll();
        try {
            sink.write(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
