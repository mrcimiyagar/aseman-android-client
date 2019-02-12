package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.extras.DocTypes;

public class FileTransferProgressed {

    private DocTypes docType;
    private long fileId;
    private int progress;

    public FileTransferProgressed(DocTypes docType, long fileId, int progress) {
        this.docType = docType;
        this.fileId = fileId;
        this.progress = progress;
    }

    public DocTypes getDocType() {
        return docType;
    }

    public long getFileId() {
        return fileId;
    }

    public int getProgress() {
        return progress;
    }
}
