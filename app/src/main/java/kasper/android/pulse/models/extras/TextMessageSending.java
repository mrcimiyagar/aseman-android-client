package kasper.android.pulse.models.extras;

public class TextMessageSending {

    private long complexId;
    private long roomId;
    private String text;

    public TextMessageSending(long complexId, long roomId, String text) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.text = text;
    }

    public long getComplexId() {
        return complexId;
    }

    public void setComplexId(long complexId) {
        this.complexId = complexId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
