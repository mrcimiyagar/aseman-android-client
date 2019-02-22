package kasper.android.pulse.models.extras;

public class FileMessageSending {

    private long complexId;
    private long roomId;
    private DocTypes docType;
    private String path;

    public FileMessageSending(long complexId, long roomId, DocTypes docType, String path) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.docType = docType;
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
}
