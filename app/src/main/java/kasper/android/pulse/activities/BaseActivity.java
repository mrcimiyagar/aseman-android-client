package kasper.android.pulse.activities;

import android.os.Bundle;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kasper.android.pulse.callbacks.ui.ConnectionListener;
import kasper.android.pulse.helpers.GraphicHelper;

public class BaseActivity extends AppCompatActivity {

    private ConnectionListener connectionListener;

    private Snackbar statusSnackbar;
    public Snackbar getStatusSnackbar() {
        return statusSnackbar;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        statusSnackbar = Snackbar.make(rootView, "Empty",
                Snackbar.LENGTH_INDEFINITE);
        statusSnackbar.dismiss();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerObservers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterObservers();
    }

    private void registerObservers() {
        connectionListener = new ConnectionListener() {
            @Override
            public void reconnecting() {
                statusSnackbar.setText("Reconnecting to server");
                statusSnackbar.show();
            }

            @Override
            public void connected() {
                statusSnackbar.dismiss();
            }
        };
        GraphicHelper.addConnectionListener(connectionListener);
    }

    private void unregisterObservers() {
        GraphicHelper.getConnectionListeners().remove(connectionListener);
    }
}
