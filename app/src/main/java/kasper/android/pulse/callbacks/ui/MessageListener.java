package kasper.android.pulse.callbacks.ui;

import kasper.android.pulse.models.entities.Entities;

/**
 * Created by keyhan1376 on 5/31/2018.
 */

public interface MessageListener {
    void messageReceived(Entities.Message message, Entities.MessageLocal messageLocal);
    void messageDeleted(Entities.Message message);
    void messageSending(Entities.Message message, Entities.MessageLocal messageLocal);
    void messageSent(long localMessageId, long onlineMessageId);
}
