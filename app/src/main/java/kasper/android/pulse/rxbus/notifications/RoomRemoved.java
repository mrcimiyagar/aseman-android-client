package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class RoomRemoved {

    private Entities.Room room;

    public RoomRemoved(Entities.Room room) {
        this.room = room;
    }

    public Entities.Room getRoom() {
        return room;
    }
}
