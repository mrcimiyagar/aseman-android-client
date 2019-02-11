package kasper.android.pulse.callbacks.ui;

import kasper.android.pulse.models.entities.Entities;

public interface OnWorkershipModifyListener {
    void botAdded(Entities.Bot bot);
    void botRemoved(Entities.Bot bot);
}
