package kasper.android.pulse.callbacks.ui;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.models.extras.Doc;

/**
 * Created by keyhan1376 on 12/15/2017.
 */

public interface OnDocsLoadedListener {
    void docsLoaded(List<Doc> docs);
}
