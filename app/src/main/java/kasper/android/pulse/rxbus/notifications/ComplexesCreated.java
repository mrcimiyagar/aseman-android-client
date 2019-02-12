package kasper.android.pulse.rxbus.notifications;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

public class ComplexesCreated {

    private List<Entities.Complex> complexes;

    public ComplexesCreated(List<Entities.Complex> complexes) {
        this.complexes = complexes;
    }

    public List<Entities.Complex> getComplexes() {
        return complexes;
    }
}
