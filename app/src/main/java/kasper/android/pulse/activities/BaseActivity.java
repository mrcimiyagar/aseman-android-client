package kasper.android.pulse.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.anadeainc.rxbus.Subscribe;
import com.esotericsoftware.reflectasm.shaded.org.objectweb.asm.Handle;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.rxbus.notifications.ConnectionStateChanged;

public class BaseActivity extends AppCompatActivity {

    private Snackbar statusSnackbar;
    public Snackbar getStatusSnackbar() {
        return statusSnackbar;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Core.getInstance().bus().register(this);
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        new Handler().post(() -> {
            statusSnackbar = Snackbar.make(rootView, "Empty",
                    Snackbar.LENGTH_INDEFINITE);
            statusSnackbar.dismiss();
        });
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onConnectionStateChanged(ConnectionStateChanged connectionStateChanged) {
        switch (connectionStateChanged.getState()) {
            case Connected:
                statusSnackbar.dismiss();
                break;
            case Reconnecting:
                statusSnackbar.setText("Reconnecting to server");
                statusSnackbar.show();
                break;
        }
    }
}
