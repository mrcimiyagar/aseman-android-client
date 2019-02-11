package kasper.android.pulse.callbacks.middleware;

import kasper.android.pulse.models.entities.Entities;

public interface OnComplexSyncListener {
    void complexSynced(Entities.Complex complex);
    void syncFailed();
}
