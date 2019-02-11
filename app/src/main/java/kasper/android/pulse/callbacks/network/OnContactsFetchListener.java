package kasper.android.pulse.callbacks.network;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

/**
 * Created by keyhan1376 on 5/24/2018.
 */

public interface OnContactsFetchListener {
    void contactsFetched(List<Entities.Contact> contacts);
}
