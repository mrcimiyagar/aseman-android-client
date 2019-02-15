package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class MessageSeen {

    private Entities.Message message;

    public MessageSeen(Entities.Message message) {
        this.message = message;
    }

    public Entities.Message getMessage() {
        return message;
    }
}
