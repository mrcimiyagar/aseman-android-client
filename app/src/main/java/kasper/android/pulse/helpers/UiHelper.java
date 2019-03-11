package kasper.android.pulse.helpers;

import android.os.Handler;
import android.os.Looper;

import com.anadeainc.rxbus.Subscribe;

import kasper.android.pulse.core.Core;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;

public class UiHelper {

    private Handler handler;

    public static void setup() {
        new UiHelper();
    }

    private UiHelper() {
        handler = new Handler(Looper.getMainLooper());
        Core.getInstance().bus().register(this);
    }

    @Subscribe
    public void onUiThreadRequested(UiThreadRequested requested) {
        handler.post(requested.getRunnable());
    }
}
