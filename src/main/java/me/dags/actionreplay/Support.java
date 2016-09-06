package me.dags.actionreplay;

import org.spongepowered.api.scheduler.Task;

/**
 * @author dags <dags@dags.me>
 */
public class Support implements Runnable {

    private final String name;
    private final String lookupClass;
    private final String hookClass;

    private Support(String name, String lookup, String hook) {
        this.name = name;
        this.lookupClass = lookup;
        this.hookClass = hook;
    }

    @Override
    public void run() {
        ActionReplay.logger().info("Checking for {}...", name);
        try {
            Class.forName(lookupClass);
            Class<?> hook = Class.forName(hookClass);
            ActionReplay.logger().info("Detected support for {}", name);
            try {
                Object object = hook.newInstance();
                if (Hook.class.isInstance(object)) {
                    Hook.class.cast(object).init();
                    ActionReplay.logger().info("Initialized support for {}", name);
                } else {
                    ActionReplay.logger().info("Hook class {} is not of the required type {}", hook, Hook.class);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                ActionReplay.logger().info("Unable to instantiate hook class {}", hookClass);
            }
        } catch (ClassNotFoundException e) {
            ActionReplay.logger().info("{} not detected. {} support disabled", lookupClass, name);
        }
    }

    public void start(Object plugin) {
        Task.builder().delayTicks(5).execute(this).submit(plugin);
    }

    public static Support of(String name, String lookupClass, String hookClass) {
        return new Support(name, lookupClass, hookClass);
    }

    public interface Hook {

        void init();
    }
}
