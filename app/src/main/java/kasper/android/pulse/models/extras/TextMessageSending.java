package kasper.android.pulse.models.extras;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity
public class TextMessageSending {

    @PrimaryKey(autoGenerate = true)
    private long sendingId;
    private long complexId;
    private long roomId;
    private long messageId;
    private String text;

    @Ignore
    public TextMessageSending(long complexId, long roomId, String text) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.text = text;
    }

    public TextMessageSending() {

    }

    public long getSendingId() {
        return sendingId;
    }

    public void setSendingId(long sendingId) {
        this.sendingId = sendingId;
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

    public long getMessageId() {
        return messageId;
    }

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
