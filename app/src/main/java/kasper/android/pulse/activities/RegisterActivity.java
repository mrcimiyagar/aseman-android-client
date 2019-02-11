package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.text.InputType;
import android.view.View;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.AuthHandler;
import retrofit2.Call;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class RegisterActivity extends AppCompatActivity {

    private TextFieldBoxes entryBox;
    private ExtendedEditText entryET;

    private boolean secondLevel;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        entryBox = findViewById(R.id.text_field_boxes);
        entryET = findViewById(R.id.activity_register_edit_text);

        secondLevel = false;
        email = "";
    }

    public void onOkBtnClicked(View view) {

        if (secondLevel) {
            String vCode = entryET.getText().toString();
            if (vCode.length() > 0) {
                Packet packet = new Packet();
                packet.setEmail(email);
                packet.setVerifyCode(vCode);
                AuthHandler authHandler = NetworkHelper.getRetrofit().create(AuthHandler.class);
                Call<Packet> call = authHandler.verify(packet);
                NetworkHelper.requestServer(call, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        Entities.Session session = packet.getSession();
                        Entities.UserSecret userSecret = packet.getUserSecret();
                        Entities.ComplexSecret complexSecret = packet.getComplexSecret();
                        DatabaseHelper.createSession(session, true);
                        DatabaseHelper.notifyUserCreated((Entities.User) session.getBaseUser());
                        DatabaseHelper.notifyUserSecretCreated(userSecret);
                        DatabaseHelper.notifyComplexCreated(userSecret.getHome());
                        DatabaseHelper.notifyComplexSecretCreated(complexSecret);
                        gotoStartupPage();
                    }

                    @Override
                    public void onServerFailure() {
                        entryBox.setError("Wrong verification code", true);
                    }

                    @Override
                    public void onConnectionFailure() {
                        entryBox.setError("Server connection failure", true);
                    }
                });
            }
            else {
                entryBox.setError("Please enter verification code", true);
            }
        }
        else {
            email = entryET.getText().toString();
            if (email.length() > 0) {
                Packet packet = new Packet();
                packet.setEmail(email);
                AuthHandler authHandler = NetworkHelper.getRetrofit().create(AuthHandler.class);
                Call<Packet> call = authHandler.register(packet);
                NetworkHelper.requestServer(call, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        secondLevel = true;
                        entryET.setText("");
                        entryET.setInputType(InputType.TYPE_CLASS_TEXT);
                        entryBox.setIconSignifier(R.drawable.ic_verify);
                        entryBox.setLabelText("Verification Code");
                        entryBox.setMaxCharacters(8);
                    }

                    @Override
                    public void onServerFailure() {
                        entryBox.setError("Invalid email", true);
                    }

                    @Override
                    public void onConnectionFailure() {
                        entryBox.setError("Server connection failure", true);
                    }
                });
            } else {
                entryBox.setError("Please enter email address", true);
            }
        }
    }

    public void gotoStartupPage() {
        startActivity(new Intent(RegisterActivity.this, StartupActivity.class)
                .putExtra("loading-data", true)
                .putExtra("newUser", true));
        finish();
    }
}
