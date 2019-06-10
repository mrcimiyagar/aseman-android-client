package kasper.android.pulse.callbacks.middleware;

import kasper.android.pulse.models.entities.Entities;

public interface OnRoomSyncListener {
    void roomSynced(Entities.BaseRoom room);
    void syncFailed();
}
