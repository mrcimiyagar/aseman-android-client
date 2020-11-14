package kasper.android.pulse.rxbus.notifications;

public class OpenBotPicker {

    private long complexId;
    private long roomId;

    public OpenBotPicker(long complexId, long roomId) {
        this.complexId = complexId;
        this.roomId = roomId;
    }

    public long getComplexId() {
        return complexId;
    }

    public long getRoomId() {
        return roomId;
    }
}
