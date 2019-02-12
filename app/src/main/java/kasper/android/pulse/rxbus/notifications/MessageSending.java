package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class MessageSending {

    private Entities.Message message;
    private Entities.MessageLocal messageLocal;

    public MessageSending(Entities.Message message, Entities.MessageLocal messageLocal) {
        this.message = message;
        this.messageLocal = messageLocal;
    }

    public Entities.Message getMessage() {
        return message;
    }

    public Entities.MessageLocal getMessageLocal() {
        return messageLocal;
    }
}
