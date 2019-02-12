package kasper.android.pulse.rxbus.notifications;

public class UiThreadRequested {

    private Runnable runnable;

    public UiThreadRequested(Runnable runnable) {
        this.runnable = runnable;
    }

    public Runnable getRunnable() {
        return runnable;
    }
}
