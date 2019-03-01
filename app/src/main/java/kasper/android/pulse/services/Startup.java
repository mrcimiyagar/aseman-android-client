package kasper.android.pulse.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import kasper.android.pulse.activities.StartupActivity;

public class Startup extends BroadcastReceiver {

    public Startup() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        new Thread(() -> {
            Intent i = new Intent(context, AsemanService.class);
            context.startService(i);
        }).start();
        new Thread(() -> {
            Intent i = new Intent(context, MusicsService.class);
            context.startService(i);
        }).start();
    }
}