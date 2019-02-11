package kasper.android.pulse.callbacks.ui;

import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;

/**
 * Created by keyhan1376 on 5/31/2018.
 */

public interface FileListener {
    void fileUploaded(DocTypes docTypes, long localFileId, long onlineFileId);
    void fileUploading(DocTypes docTypes, Entities.File file, Entities.FileLocal fileLocal);
    void fileUploadCancelled(DocTypes docTypes, long fileId);
    void fileDownloaded(DocTypes docTypes, long fileId);
    void fileDownloading(DocTypes docTypes, Entities.File file);
    void fileDownloadCancelled(DocTypes docTypes, long fileId);
    void fileTransferProgressed(DocTypes docTypes, long fileId, int progress);
    void fileReceived(DocTypes docTypes, Entities.File file, Entities.FileLocal fileLocal);
}
