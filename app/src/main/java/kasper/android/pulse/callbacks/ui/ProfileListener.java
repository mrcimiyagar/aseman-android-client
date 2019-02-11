package kasper.android.pulse.callbacks.ui;

import kasper.android.pulse.models.entities.Entities;

public interface ProfileListener {
    void profileUpdated(Entities.User user);
    void profileUpdated(Entities.Complex complex);
    void profileUpdated(Entities.Room room);
    void profileUpdated(Entities.Bot bot);
}
