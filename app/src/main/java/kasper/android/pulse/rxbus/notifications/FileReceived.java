package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;

public class FileReceived {

    private DocTypes docType;
    private Entities.File file;
    private Entities.FileLocal fileLocal;

    public FileReceived(DocTypes docType, Entities.File file, Entities.FileLocal fileLocal) {
        this.docType = docType;
        this.file = file;
        this.fileLocal = fileLocal;
    }

    public DocTypes getDocType() {
        return docType;
    }

    public Entities.File getFile() {
        return file;
    }

    public Entities.FileLocal getFileLocal() {
        return fileLocal;
    }
}
