package kasper.android.pulse.callbacks.middleware;

import kasper.android.pulse.models.entities.Entities;

public interface OnBaseUserSyncListener {
    void userSynced(Entities.BaseUser baseUser);
    void syncFailed();
}
