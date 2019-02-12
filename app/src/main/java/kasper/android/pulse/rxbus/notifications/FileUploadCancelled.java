package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.extras.DocTypes;

public class FileUploadCancelled {

    private DocTypes docType;
    private long localFileId;

    public FileUploadCancelled(DocTypes docType, long localFileId) {
        this.docType = docType;
        this.localFileId = localFileId;
    }

    public DocTypes getDocType() {
        return docType;
    }

    public long getLocalFileId() {
        return localFileId;
    }
}
