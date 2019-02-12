package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class UserProfileUpdated {

    private Entities.User user;

    public UserProfileUpdated(Entities.User user) {
        this.user = user;
    }

    public Entities.User getUser() {
        return user;
    }
}
