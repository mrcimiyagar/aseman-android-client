package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class UserJointComplex {

    private Entities.Membership membership;

    public UserJointComplex(Entities.Membership membership) {
        this.membership = membership;
    }

    public Entities.Membership getMembership() {
        return membership;
    }
}
