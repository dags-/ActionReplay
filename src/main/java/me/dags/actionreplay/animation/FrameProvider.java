package me.dags.actionreplay.animation;

/**
 * @author dags <dags@dags.me>
 */
public interface FrameProvider {

    Frame nextFrame() throws Exception;

    boolean hasNext() throws Exception;

    void close() throws Exception;

    void forward() throws Exception;

    void backward() throws Exception;
}
