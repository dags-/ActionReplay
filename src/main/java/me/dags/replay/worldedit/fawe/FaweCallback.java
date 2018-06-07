package me.dags.replay.worldedit.fawe;

import com.boydti.fawe.object.FaweQueue;
import com.boydti.fawe.object.RunnableVal2;
import java.util.function.Supplier;

/**
 * @author dags <dags@dags.me>
 */
public class FaweCallback extends RunnableVal2<FaweQueue.ProgressType, Integer> implements Supplier<Boolean> {

    private boolean complete = false;

    @Override
    public Boolean get() {
        return complete;
    }

    @Override
    public void run(FaweQueue.ProgressType type, Integer count) {
        if (type == FaweQueue.ProgressType.DONE) {
            complete = true;
        }
    }
}
