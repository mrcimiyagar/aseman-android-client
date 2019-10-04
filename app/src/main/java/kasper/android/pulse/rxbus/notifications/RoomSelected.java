package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class RoomSelected {
    private Entities.BaseRoom room;

    public RoomSelected(Entities.BaseRoom room) {
        this.room = room;
    }

    public Entities.BaseRoom getRoom() {
        return room;
    }
}
