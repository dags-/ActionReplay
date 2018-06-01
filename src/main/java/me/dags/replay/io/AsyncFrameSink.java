package me.dags.replay.io;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.dags.replay.frame.Frame;
import me.dags.replay.frame.FrameSink;
import me.dags.replay.util.CancellableTask;

/**
 * @author dags <dags@dags.me>
 */
public class AsyncFrameSink extends CancellableTask implements FrameSink {

    private final FrameSink sink;
    private final ConcurrentLinkedQueue<Frame> queue = new ConcurrentLinkedQueue<>();

    private volatile boolean stopped = false;

    public AsyncFrameSink(FrameSink sink) {
        this.sink = sink;
    }

    public void start(Object plugin) {
        startAsync(plugin, 40);
    }

    public void stop() {
        stopped = true;
    }

    @Override
    public void accept(Frame frame) {
        if (stopped) {
            return;
        }
        queue.add(frame);
    }

    @Override
    public void run() {
        if (!queue.isEmpty()) {
            Frame frame = queue.poll();
            try {
                sink.accept(frame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void dispose() {
        while (!queue.isEmpty()) {
            Frame frame = queue.poll();
            try {
                sink.accept(frame);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
