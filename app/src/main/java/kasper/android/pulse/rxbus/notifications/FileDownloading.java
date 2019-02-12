package kasper.android.pulse.rxbus.notifications;

import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;

public class FileDownloading {

    private DocTypes docType;
    private Entities.File file;

    public FileDownloading(DocTypes docType, Entities.File file) {
        this.docType = docType;
        this.file = file;
    }

    public DocTypes getDocType() {
        return docType;
    }

    public Entities.File getFile() {
        return file;
    }
}
