package kasper.android.pulse.rxbus.notifications;

public class ComplexRemoved {

    private long complexId;

    public ComplexRemoved(long complexId) {
        this.complexId = complexId;
    }

    public long getComplexId() {
        return complexId;
    }
}
