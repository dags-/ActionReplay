package me.dags.actionreplay.animation.frame;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameProvider extends AutoCloseable {

    FrameProvider forward() throws Exception;

    FrameProvider backward() throws Exception;

    Frame nextFrame() throws Exception;

    boolean hasNext() throws Exception;
}
