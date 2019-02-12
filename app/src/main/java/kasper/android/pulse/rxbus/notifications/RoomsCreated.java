package kasper.android.pulse.rxbus.notifications;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

public class RoomsCreated {

    private long complexId;
    private List<Entities.Room> rooms;

    public RoomsCreated(long complexId, List<Entities.Room> rooms) {
        this.complexId = complexId;
        this.rooms = rooms;
    }

    public long getComplexId() {
        return complexId;
    }

    public List<Entities.Room> getRooms() {
        return rooms;
    }
}
