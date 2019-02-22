package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;

public class FileUploaded {

    private DocTypes docType;
    private long localFileId;
    private long onlineFileId;
    private long fileUsageId;
    private long complexId;
    private long roomId;
    private Entities.File file;
    private Entities.Message message;

    public FileUploaded(DocTypes docType, long localFileId, long onlineFileId, long fileUsageId
            , long complexId, long roomId, Entities.File file, Entities.Message message) {
        this.docType = docType;
        this.localFileId = localFileId;
        this.onlineFileId = onlineFileId;
        this.fileUsageId = fileUsageId;
        this.complexId = complexId;
        this.roomId = roomId;
        this.file = file;
        this.message = message;
    }

    public DocTypes getDocType() {
        return docType;
    }

    public long getLocalFileId() {
        return localFileId;
    }

    public long getOnlineFileId() {
        return onlineFileId;
    }

    public long getFileUsageId() {
        return fileUsageId;
    }

    public long getComplexId() {
        return complexId;
    }

    public long getRoomId() {
        return roomId;
    }

    public Entities.File getFile() {
        return file;
    }

    public Entities.Message getMessage() {
        return message;
    }
}
