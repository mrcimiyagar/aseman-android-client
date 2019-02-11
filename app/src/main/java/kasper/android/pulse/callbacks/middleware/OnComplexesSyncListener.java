package kasper.android.pulse.callbacks.middleware;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

public interface OnComplexesSyncListener {
    void complexesSynced(List<Entities.Complex> complexes);
    void syncFailed();
}
