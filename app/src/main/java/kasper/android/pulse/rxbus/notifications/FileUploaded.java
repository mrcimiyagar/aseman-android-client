package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.extras.DocTypes;

public class FileUploaded {

    private DocTypes docType;
    private long localFileId;
    private long onlineFileId;

    public FileUploaded(DocTypes docType, long localFileId, long onlineFileId) {
        this.docType = docType;
        this.localFileId = localFileId;
        this.onlineFileId = onlineFileId;
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
}
