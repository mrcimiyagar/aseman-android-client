package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class MessageReceived {

    private boolean bottom;
    private Entities.Message message;
    private Entities.MessageLocal messageLocal;

    public MessageReceived(boolean bottom, Entities.Message message, Entities.MessageLocal messageLocal) {
        this.bottom = bottom;
        this.message = message;
        this.messageLocal = messageLocal;
    }

    public boolean isBottom() {
        return this.bottom;
    }

    public Entities.Message getMessage() {
        return message;
    }

    public Entities.MessageLocal getMessageLocal() {
        return messageLocal;
    }
}
