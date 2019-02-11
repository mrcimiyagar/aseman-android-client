package kasper.android.pulseframework.locks;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import kasper.android.pulseframework.interfaces.IMainThreadRunner;

public class Locks {

    private static IMainThreadRunner mainThreadRunner;
    private static boolean running = false;
    private static Queue<Runnable> accesses = new LinkedBlockingQueue<>();

    public static void setup(IMainThreadRunner mainThreadRunner) {
        Locks.mainThreadRunner = mainThreadRunner;
    }

    public static void runSafeOnIdTable(Runnable runnable) {
        new Thread(() -> {
            Locks.accesses.offer(runnable);
            if (!running) runQueue();
        }).start();
    }

    private static void runQueue() {
        mainThreadRunner.runOnMainThread(() -> {
            running = true;
            while (!accesses.isEmpty()) {
                Runnable runnable = accesses.poll();
                runnable.run();
            }
            running = false;
        });
    }
}
