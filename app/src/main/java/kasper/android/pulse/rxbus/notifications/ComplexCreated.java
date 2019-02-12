package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;

public class ComplexCreated {

    private Entities.Complex complex;

    public ComplexCreated(Entities.Complex complex) {
        this.complex = complex;
    }

    public Entities.Complex getComplex() {
        return complex;
    }
}
