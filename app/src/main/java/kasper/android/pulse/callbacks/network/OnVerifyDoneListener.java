package kasper.android.pulse.callbacks.network;

/**
 * Created by keyhan1376 on 1/28/2018.
 */

public interface OnVerifyDoneListener {
    void verifyDone(long sessionId, String token, long humanId, long homeChatId);
}