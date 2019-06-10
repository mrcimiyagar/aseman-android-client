package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class RoomRemoved {

    private Entities.BaseRoom room;

    public RoomRemoved(Entities.BaseRoom room) {
        this.room = room;
    }

    public Entities.BaseRoom getRoom() {
        return room;
    }
}
