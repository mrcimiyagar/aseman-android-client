package kasper.android.pulse.callbacks.network;

import java.io.Serializable;

/**
 * Created by keyhan1376 on 3/28/2018.
 */

public interface OnFileUploadListener extends Serializable {
    void fileUploaded(long fileId, long fileUsageId);
}
