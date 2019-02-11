package kasper.android.pulse.callbacks.network;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

/**
 * Created by keyhan1376 on 3/2/2018.
 */

public interface OnWorkershipsGotListener {
    void workershipsGot(List<Entities.Workership> workerships);
}
