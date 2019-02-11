package kasper.android.pulse.callbacks.ui;

import kasper.android.pulse.models.extras.DocTypes;

/**
 * Created by keyhan1376 on 5/11/2018.
 */

public interface OnFileSelectListener {
    void fileSelected(String path, DocTypes docType);
}
