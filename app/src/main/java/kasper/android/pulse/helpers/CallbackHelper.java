package kasper.android.pulse.helpers;

import androidx.recyclerview.widget.RecyclerView;

import java.util.Hashtable;

import kasper.android.pulse.callbacks.ui.OnWorkershipModifyListener;
import kasper.android.pulse.callbacks.ui.OnDocSelectListener;
import kasper.android.pulse.callbacks.ui.OnFileSelectListener;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;

/**
 * Created by keyhan1376 on 6/3/2018.
 */

public class CallbackHelper {

    private static Hashtable<Long, Object> callbacks;
    private static long idCounter;
    private static final Object lock = new Object();

    public static void setup() {
        callbacks = new Hashtable<>();
        idCounter = 0;
    }

    public static long register(Object object) {
        synchronized (lock) {
            long id = idCounter++;
            callbacks.put(id, object);
            return id;
        }
    }

    public static void invoke(long id, long methodIndex, Object... params) {
        synchronized (lock) {
            Object object = callbacks.get(id);
            if (object != null) {
                if (object instanceof OnFileSelectListener) {
                    ((OnFileSelectListener) object).fileSelected((String) params[0]
                            , (DocTypes) params[1]);
                } else if (object instanceof OnDocSelectListener) {
                    ((OnDocSelectListener) object).docLongClicked((Entities.File) params[0]);
                } else if (object instanceof RecyclerView.OnScrollListener) {
                    if (methodIndex == 1) {
                        ((RecyclerView.OnScrollListener) object).onScrolled((RecyclerView) params[0]
                                , (Integer) params[1], (Integer) params[2]);
                    }
                } else if (object instanceof OnWorkershipModifyListener) {
                    if (methodIndex == 0) {
                        ((OnWorkershipModifyListener) object).botAdded((Entities.Bot) params[0]);
                    } else if (methodIndex == 1) {
                        ((OnWorkershipModifyListener) object).botRemoved((Entities.Bot) params[0]);
                    }
                } else if (object instanceof Runnable) {
                    ((Runnable) object).run();
                }
            }
        }
    }
}
