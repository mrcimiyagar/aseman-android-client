package kasper.android.pulse.callbacks.network;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

/**
 * Created by keyhan1376 on 3/9/2018.
 */

public interface OnSearchDoneListener {
    void searchDone(List<Entities.User> users);
}
