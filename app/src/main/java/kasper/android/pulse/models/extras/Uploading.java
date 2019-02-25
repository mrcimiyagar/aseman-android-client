package kasper.android.pulse.models.extras;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Uploading {

    @PrimaryKey(autoGenerate = true)
    private long uploadingId;
    private String docType;
    private String path;
    private long complexId;
    private long roomId;
    private long messageId;
    private long fileId;
    private boolean compress;
    private boolean attachToMessage;

    public Uploading(DocTypes docType, String path, long complexId, long roomId, long messageId
            , long fileId, boolean compress, boolean attachToMessage) {
        this.docType = docType.toString();
        this.path = path;
        this.complexId = complexId;
        this.roomId = roomId;
        this.messageId = messageId;
        this.fileId = fileId;
        this.compress = compress;
        this.attachToMessage = attachToMessage;
    }

    public Uploading(DocTypes docType, String path, long complexId, long roomId, boolean compress, boolean attachToMessage) {
        this.docType = docType.toString();
        this.path = path;
        this.complexId = complexId;
        this.roomId = roomId;
        this.compress = compress;
        this.attachToMessage = attachToMessage;
    }

    public Uploading() {

    }

    public long getUploadingId() {
        return uploadingId;
    }

    public void setUploadingId(long uploadingId) {
        this.uploadingId = uploadingId;
    }

    public String getDocType() {
        return docType;
    }

    public DocTypes getDocTypeEnum() {
        return DocTypes.valueOf(docType);
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
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

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public boolean isCompress() {
        return compress;
    }

    public void setCompress(boolean compress) {
        this.compress = compress;
    }

    public boolean isAttachToMessage() {
        return attachToMessage;
    }

    public void setAttachToMessage(boolean attachToMessage) {
        this.attachToMessage = attachToMessage;
    }
}
