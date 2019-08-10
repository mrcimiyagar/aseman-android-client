package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.text.InputType;
import android.view.View;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.components.OneClickFAB;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.AuthHandler;
import retrofit2.Call;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;
import studio.carbonylgroup.textfieldboxes.TextFieldBoxes;

public class RegisterActivity extends BaseActivity {

    private TextFieldBoxes entryBox;
    private ExtendedEditText entryET;
    private OneClickFAB doneFAB;

    private boolean secondLevel;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        entryBox = findViewById(R.id.text_field_boxes);
        entryET = findViewById(R.id.activity_register_edit_text);
        doneFAB = findViewById(R.id.doneFAB);

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
                        DatabaseHelper.notifyUserCreated(packet.getUser());
                        DatabaseHelper.notifyUserSecretCreated(userSecret);
                        DatabaseHelper.notifyComplexCreated(userSecret.getHome());
                        DatabaseHelper.notifyComplexSecretCreated(complexSecret);
                        DatabaseHelper.notifyMembershipCreated(userSecret.getHome().getMembers().get(0));
                        DatabaseHelper.notifyMemberAccessCreated(userSecret.getHome().getMembers().get(0).getMemberAccess());
                        gotoStartupPage();
                    }
                    @Override
                    public void onServerFailure() {
                        entryBox.setError("Wrong verification code", true);
                        doneFAB.enable();
                    }
                    @Override
                    public void onConnectionFailure() {
                        entryBox.setError("Server connection failure", true);
                        doneFAB.enable();
                    }
                });
            }
            else {
                entryBox.setError("Please enter verification code", true);
                doneFAB.enable();
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
                        doneFAB.enable();
                    }

                    @Override
                    public void onServerFailure() {
                        entryBox.setError("Invalid email", true);
                        doneFAB.enable();
                    }

                    @Override
                    public void onConnectionFailure() {
                        entryBox.setError("Server connection failure", true);
                        doneFAB.enable();
                    }
                });
            } else {
                entryBox.setError("Please enter email address", true);
                doneFAB.enable();
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
