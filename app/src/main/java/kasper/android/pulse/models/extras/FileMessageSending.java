package kasper.android.pulse.models.extras;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class FileMessageSending {

    @PrimaryKey(autoGenerate = true)
    private long sendingId;
    private long complexId;
    private long roomId;
    private long messageId;
    private String docType;
    private String path;

    public FileMessageSending(long complexId, long roomId, DocTypes docType, String path) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.docType = docType.toString();
        this.path = path;
    }

    public FileMessageSending() {

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

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public DocTypes getDocTypeEnum() {
        return DocTypes.valueOf(this.docType);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
