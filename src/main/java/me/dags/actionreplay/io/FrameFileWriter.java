package me.dags.actionreplay.io;

import me.dags.actionreplay.replay.frame.Frame;
import org.spongepowered.api.scheduler.Task;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class FrameFileWriter implements AutoCloseable, Consumer<Task> {

    private static final int ACCELERATED_THRESHOLD = 20;
    private static final int NORMAL_THRESHOLD = 10;

    private final Queue<Frame> buffer = new ConcurrentLinkedDeque<>();
    private final AtomicBoolean interrupt = new AtomicBoolean(false);
    private final AtomicInteger counter = new AtomicInteger(0);
    private final RandomAccessFile file;

    public FrameFileWriter(File file) throws IOException {
        this.file = new RandomAccessFile(file, "rw");
        this.file.seek(file.length());
    }

    public void interrupt() {
        interrupt.set(true);
    }

    public void queue(Frame frame) {
        if (!interrupt.get()) {
            buffer.add(frame);
            counter.getAndAdd(1);
        }
    }

    private void write(int count) throws IOException {
        try (ByteArrayDataOutputStream outputStream = new ByteArrayDataOutputStream(8192 * count)) {
            while (count-- > 0 && !buffer.isEmpty()) {
                Frame frame = buffer.poll();
                counter.getAndAdd(-1);
                if (frame != null) {
                    write(outputStream, frame);
                }
            }
            outputStream.writeTo(file);
        }
    }

    private void write(ByteArrayDataOutputStream buffer, Frame frame) throws IOException {
        FrameFileFormat.FORMAT.write(buffer, frame);
        file.write(buffer.toByteArray());
    }

    @Override
    public void close() throws IOException {
        interrupt.set(true);
        flush();
        file.close();
    }

    @Override
    public void accept(Task task) {
        try {
            if (!task.isAsynchronous()) {
                throw new UnsupportedOperationException("FrameFileWriter should not be running on the main thread!");
            }
            if (interrupt.get()) {
                close();
                task.cancel();
                return;
            }
            int count = counter.get();

            if (count > ACCELERATED_THRESHOLD) {
                count = ACCELERATED_THRESHOLD;
            } else if (count > NORMAL_THRESHOLD) {
                count = NORMAL_THRESHOLD;
            }

            if (count >= NORMAL_THRESHOLD) {
                write(count);
            }

        } catch (IOException e) {
            e.printStackTrace();

            try {
                close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            task.cancel();
        }
    }

    private void flush() throws IOException {
        try (ByteArrayDataOutputStream outputStream = new ByteArrayDataOutputStream(8192 * counter.get())) {
            while (!buffer.isEmpty()) {
                Frame frame = buffer.poll();
                if (frame != null) {
                    write(outputStream, frame);
                }
            }
            file.write(outputStream.toByteArray());
            counter.set(0);
        }
    }
}
