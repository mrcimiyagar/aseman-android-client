package kasper.android.pulse.callbacks.ui;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

/**
 * Created by keyhan1376 on 5/31/2018.
 */

public interface RoomListener {
    void roomCreated(long complexId, Entities.Room room);
    void roomsCreated(long complexId, List<Entities.Room> rooms);
    void roomRemoved(Entities.Room room);
    void updateRoomLastMessage(long roomId, Entities.Message message);
}
