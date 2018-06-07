package me.dags.replay.frame;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.dags.replay.data.Node;
import me.dags.replay.data.SerializationException;
import me.dags.replay.io.Sink;
import me.dags.replay.replay.ReplayMeta;
import me.dags.replay.util.CancellableTask;
import org.spongepowered.api.scheduler.Task;

/**
 * @author dags <dags@dags.me>
 */
public class BufferedFrameSink extends CancellableTask implements Sink<Frame, ReplayMeta> {

    private final Sink<Node, Node> sink;
    private final ConcurrentLinkedQueue<Node> buffer = new ConcurrentLinkedQueue<>();

    public BufferedFrameSink(Sink<Node, Node> sink) {
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
    public void writeHeader(ReplayMeta meta) {
        if (isCancelled()) {
            return;
        }
        try {
            Node node = ReplayMeta.SERIALIZER.serialize(meta);
            sink.writeHeader(node);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void write(Frame view) {
        if (isCancelled()) {
            return;
        }

        try {
            Node node = Frame.SERIALIZER.serializeChecked(view);
            buffer.add(node);
        } catch (SerializationException e) {
            e.printStackTrace();
        }
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
        try {
            Node frame = buffer.poll();
            sink.write(frame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}