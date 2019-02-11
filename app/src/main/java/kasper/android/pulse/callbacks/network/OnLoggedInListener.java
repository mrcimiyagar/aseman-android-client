package kasper.android.pulse.callbacks.network;

/**
 * Created by keyhan1376 on 3/5/2018.
 */

public interface OnLoggedInListener {
    void loggedIn(long humanId);
    void loginFailed();
}
