package kasper.android.pulseframework.locks;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import kasper.android.pulseframework.interfaces.IMainThreadRunner;

public class Locks {

    private static BlockingQueue<Runnable> accesses = new LinkedBlockingQueue<>();
    private static boolean alive = true;

    public static void setup(IMainThreadRunner mainThreadRunner) {
        new Thread(() -> {
            try {
                while (alive) {
                    Runnable runnable = accesses.take();
                    mainThreadRunner.runOnMainThread(runnable);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public static void runInQueue(Runnable runnable) {
        new Thread(() -> accesses.offer(runnable)).start();
    }
}
