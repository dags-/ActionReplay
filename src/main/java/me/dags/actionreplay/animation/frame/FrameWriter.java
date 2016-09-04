package me.dags.actionreplay.animation.frame;

import com.google.common.primitives.Ints;
import org.spongepowered.api.data.persistence.DataFormats;
import org.spongepowered.api.scheduler.Task;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public class FrameWriter implements Consumer<Task> {

    private static final int ACCELERATED_RATE = 10;
    private static final int NORMAL_RATE = 5;

    private final Queue<Frame> buffer = new ConcurrentLinkedDeque<>();
    private final RandomAccessFile file;
    private AtomicBoolean interrupted = new AtomicBoolean(false);
    private int counter = 0;

    public FrameWriter(File file) throws IOException {
        this.file = new RandomAccessFile(file, "rw");
        this.file.seek(file.length());
    }

    public void queue(Frame frame) {
        buffer.add(frame);
        counter++;
    }

    public void interrupt() {
        interrupted.set(true);
    }

    private void write(int count) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream(count * 1024)) {
            while (count-- > 0 && !buffer.isEmpty()) {
                counter--;
                Frame frame = buffer.poll();

                try (ByteArrayOutputStream frameBytes = new ByteArrayOutputStream(1024)) {
                    DataFormats.NBT.writeTo(frameBytes, frame.toContainer());
                    byte[] length = Ints.toByteArray(frameBytes.size());
                    byteStream.write(length);
                    frameBytes.writeTo(byteStream);
                    byteStream.write(length);
                }

            }
            file.write(byteStream.toByteArray());
        }
    }

    @Override
    public void accept(Task task) {
        if (interrupted.get()) {
            task.cancel();
            try {
                write(counter);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        int size = counter;
        if (size > ACCELERATED_RATE) {
            size = ACCELERATED_RATE;
        } else if (size > NORMAL_RATE) {
            size = NORMAL_RATE;
        }
        if (size > 0) {
            try {
                write(size);
            } catch (IOException e) {
                e.printStackTrace();
                task.cancel();
            }
        }
    }
}
