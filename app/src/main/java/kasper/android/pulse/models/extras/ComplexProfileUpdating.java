package kasper.android.pulse.models.extras;

import kasper.android.pulse.models.entities.Entities;

public class ComplexProfileUpdating extends ProfileUpdating {

    private Entities.Complex complex;

    public ComplexProfileUpdating(String path, Entities.Complex complex) {
        super(path);
        this.complex = complex;
    }

    public Entities.Complex getComplex() {
        return complex;
    }
}
