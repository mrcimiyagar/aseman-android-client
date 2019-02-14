package kasper.android.pulse.activities;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.anadeainc.rxbus.Subscribe;
import com.esotericsoftware.reflectasm.shaded.org.objectweb.asm.Handle;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kasper.android.pulse.R;
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
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    public void showSnack(String message) {
        if (statusSnackbar == null) {
            View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
            statusSnackbar = Snackbar.make(rootView, message,
                    Snackbar.LENGTH_INDEFINITE);
        } else {
            statusSnackbar.setText(message);
        }
        if (!statusSnackbar.isShown()) {
            statusSnackbar.show();
        }
    }

    public void setupSnackAction(String action, View.OnClickListener clickListener) {
        statusSnackbar.setAction("Retry Login", clickListener);
        statusSnackbar.setActionTextColor(getResources().getColor(R.color.colorBlue));
    }

    public void hideSnack() {
        statusSnackbar.dismiss();
    }

    @Subscribe
    public void onConnectionStateChanged(ConnectionStateChanged connectionStateChanged) {
        switch (connectionStateChanged.getState()) {
            case Connected:
                hideSnack();
                break;
            case Reconnecting:
                showSnack("Reconnecting to server");
                break;
        }
    }
}
