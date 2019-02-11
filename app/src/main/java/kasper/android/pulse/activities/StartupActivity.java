package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import androidx.appcompat.app.AppCompatActivity;

import io.fabric.sdk.android.Fabric;
import kasper.android.pulse.R;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;

public class StartupActivity extends AppCompatActivity {

    boolean newUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        if (getIntent().getExtras() != null)
            if (getIntent().getExtras().containsKey("newUser"))
                newUser = getIntent().getExtras().getBoolean("newUser");

        Entities.Session session = DatabaseHelper.getSingleSession();

        if (session != null && session.getToken().length() > 0) {
            gotoHomePage();
        } else {
            gotoRegisterPage();
        }
    }

    private void gotoHomePage() {
        startActivity(new Intent(StartupActivity.this, HomeActivity.class)
                .putExtra("newUser", newUser));
        finish();
    }

    private void gotoRegisterPage() {
        startActivity(new Intent(StartupActivity.this, RegisterActivity.class));
        finish();
    }
}
