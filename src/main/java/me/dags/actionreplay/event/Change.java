package me.dags.actionreplay.event;

/**
 * @author dags <dags@dags.me>
 */
public interface Change {

    void restore();

    void undo();
}
