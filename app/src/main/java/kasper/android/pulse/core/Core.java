package kasper.android.pulse.core;

import android.app.Application;

import com.anadeainc.rxbus.Bus;
import com.anadeainc.rxbus.BusProvider;
import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import kasper.android.pulse.helpers.CallbackHelper;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;

/**
 * Created by keyhan1376 on 1/24/2018
 */

public class Core extends Application {

    private static Core instance;
    public static Core getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        final Fabric fabric = new Fabric.Builder(this)
                .kits(new Crashlytics())
                .debuggable(true)
                .build();
        Fabric.with(fabric);
        super.onCreate();
        instance = this;
        CallbackHelper.setup();
        GraphicHelper.setup(this);
        DatabaseHelper.setup();
        NetworkHelper.setup();
    }

    public Bus bus() {
        return BusProvider.getInstance();
    }
}
