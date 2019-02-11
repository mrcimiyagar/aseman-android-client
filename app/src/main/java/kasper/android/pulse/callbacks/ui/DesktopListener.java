package kasper.android.pulse.callbacks.ui;

import kasper.android.pulse.models.entities.Entities;

public interface DesktopListener {
    void workerAdded(Entities.Workership workership);
    void workerUpdated(Entities.Workership workership);
    void workerRemoved(Entities.Workership workership);
}
