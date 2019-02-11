package kasper.android.pulse.callbacks.middleware;

import kasper.android.pulse.models.entities.Entities;

public interface OnMeSyncListener {
    void meSynced(Entities.User me, long homeId);
    void syncFailed();
}
