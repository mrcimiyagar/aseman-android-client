package kasper.android.pulse.models.extras;

import kasper.android.pulse.models.entities.Entities;

public class UserProfileUpdating extends ProfileUpdating {

    private Entities.User user;

    public UserProfileUpdating(String path, Entities.User user) {
        super(path);
        this.user = user;
    }

    public Entities.User getUser() {
        return user;
    }
}
