package kasper.android.pulse.callbacks.network;

/**
 * Created by keyhan1376 on 3/18/2018.
 */

public interface OnContactCreatedListener {
    void contactCreated(long contactId, long roomId, long messageId, String text, long time);
    void contactExists();
}
