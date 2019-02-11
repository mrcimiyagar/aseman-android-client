package kasper.android.pulse.callbacks.network;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

/**
 * Created by keyhan1376 on 3/6/2018.
 */

public interface OnMessagesGotListener {
    void messagesGot(List<Entities.Message> messages);
}
