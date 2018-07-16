package me.dags.replay.frame;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import me.dags.replay.io.Source;
import me.dags.replay.replay.ReplayMeta;
import me.dags.replay.util.CancellableTask;
import org.jnbt.CompoundTag;

/**
 * @author dags <dags@dags.me>
 */
public class BufferedFrameSource extends CancellableTask implements Source<Object> {

    private final int size;
    private final long timeout;
    private final Source<CompoundTag> source;
    private final ConcurrentLinkedQueue<CompoundTag> buffer = new ConcurrentLinkedQueue<>();

    public BufferedFrameSource(Source<CompoundTag> source, int size, long timeout, TimeUnit unit) {
        this.timeout = unit.toMillis(timeout);
        this.source = source;
        this.size = size;
    }

    public void start(Object plugin) {
        super.startAsyncTask(plugin, 1, 1);
    }

    public void stop() {
        super.cancel();
    }

    @Override
    public ReplayMeta header() {
        try {
            CompoundTag node = source.header();
            if (node.isAbsent()) {
                return ReplayMeta.NONE;
            }
            return ReplayMeta.SERIALIZER.deserialize(node);
        } catch (IOException e) {
            e.printStackTrace();
            return ReplayMeta.NONE;
        }
    }

    @Override
    public Frame next() {
        CompoundTag next = null;
        long start = System.currentTimeMillis();

        while (next == null) {
            // taking too long
            if (System.currentTimeMillis() - start > timeout) {
                break;
            }
            // wait for buffer to fill
            if (buffer.isEmpty()) {
                continue;
            }
            // poll from buffer
            next = buffer.poll();
        }

        if (next == null || next.isAbsent()) {
            return Frame.NONE;
        }

        return Frame.SERIALIZER.deserialize(next);

    }

    @Override
    public Frame first() {
        try {
            CompoundTag first = source.first();
            return Frame.SERIALIZER.deserialize(first);
        } catch (IOException e) {
            e.printStackTrace();
            return Frame.NONE;
        }
    }

    @Override
    public Frame last() {
        try {
            CompoundTag last = source.last();
            return Frame.SERIALIZER.deserialize(last);
        } catch (IOException e) {
            e.printStackTrace();
            return Frame.NONE;
        }
    }

    @Override
    public void run() {
        while (buffer.size() < size) {
            try {
                CompoundTag node = source.next();
                buffer.add(node);

                // absent node indicates end of file
                if (node.isAbsent()) {
                    stop();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close() {
        try {
            source.close();
            buffer.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
