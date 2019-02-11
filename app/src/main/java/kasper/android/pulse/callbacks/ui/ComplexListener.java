package kasper.android.pulse.callbacks.ui;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

public interface ComplexListener {
    void notifyComplexCreated(Entities.Complex complex);
    void notifyComplexCreated(Entities.Contact contact, Entities.Complex complex);
    void notifyComplexRemoved(long complexId);
    void notifyComplexesCreated(List<Entities.Complex> complexes);
}
