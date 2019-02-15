package kasper.android.pulse.rxbus.notifications;

public class RoomUnreadChanged {

    private long roomId;

    public RoomUnreadChanged(long roomId) {
        this.roomId = roomId;
    }

    public long getRoomId() {
        return roomId;
    }
}
