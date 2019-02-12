package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class RoomCreated {

    private long complexId;
    private Entities.Room room;

    public RoomCreated(long complexId, Entities.Room room) {
        this.complexId = complexId;
        this.room = room;
    }

    public long getComplexId() {
        return complexId;
    }

    public Entities.Room getRoom() {
        return room;
    }
}
