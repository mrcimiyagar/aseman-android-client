package kasper.android.pulse.rxbus.notifications;

public class AppBarStateChanged {

    public enum AppBarState {
        EXPANDED,
        COLLAPSED
    }

    private AppBarState state;

    public AppBarStateChanged(AppBarState state) {
        this.state = state;
    }

    public AppBarState getState() {
        return state;
    }
}
