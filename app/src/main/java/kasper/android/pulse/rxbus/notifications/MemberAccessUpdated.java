package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class MemberAccessUpdated {

    private Entities.MemberAccess memberAccess;

    public MemberAccessUpdated(Entities.MemberAccess memberAccess) {
        this.memberAccess = memberAccess;
    }

    public Entities.MemberAccess getMemberAccess() {
        return memberAccess;
    }
}
