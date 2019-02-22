package kasper.android.pulse.models.extras;

public class Uploading {

    private DocTypes docType;
    private String path;
    private long complexId;
    private long roomId;
    private boolean compress;
    private boolean attachToMessage;

    public Uploading(DocTypes docType, String path, long complexId, long roomId, boolean compress, boolean attachToMessage) {
        this.docType = docType;
        this.path = path;
        this.complexId = complexId;
        this.roomId = roomId;
        this.compress = compress;
        this.attachToMessage = attachToMessage;
    }

    public DocTypes getDocType() {
        return docType;
    }

    public void setDocType(DocTypes docType) {
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
