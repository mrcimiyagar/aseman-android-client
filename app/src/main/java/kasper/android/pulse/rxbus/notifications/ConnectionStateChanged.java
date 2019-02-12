package kasper.android.pulse.rxbus.notifications;

public class ConnectionStateChanged {

    public enum State {
        Reconnecting, Connected
    }

    private State state;

    public ConnectionStateChanged(State state) {
        this.state = state;
    }

    public State getState() {
        return state;
    }
}
