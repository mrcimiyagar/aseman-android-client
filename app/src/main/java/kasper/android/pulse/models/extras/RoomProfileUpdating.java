package kasper.android.pulse.models.extras;

import kasper.android.pulse.models.entities.Entities;

public class RoomProfileUpdating extends ProfileUpdating {

    private Entities.Room room;

    public RoomProfileUpdating(String path, Entities.Room room) {
        super(path);
        this.room = room;
    }

    public Entities.Room getRoom() {
        return room;
    }
}
