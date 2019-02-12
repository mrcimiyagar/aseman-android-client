package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class ContactCreated {

    private Entities.Contact contact;

    public ContactCreated(Entities.Contact contact) {
        this.contact = contact;
    }

    public Entities.Contact getContact() {
        return contact;
    }
}
