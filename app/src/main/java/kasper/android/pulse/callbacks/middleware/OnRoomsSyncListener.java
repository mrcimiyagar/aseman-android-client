package kasper.android.pulse.callbacks.middleware;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

public interface OnRoomsSyncListener {
    void roomsSynced(List<Entities.BaseRoom> rooms);
    void syncFailed();
}
