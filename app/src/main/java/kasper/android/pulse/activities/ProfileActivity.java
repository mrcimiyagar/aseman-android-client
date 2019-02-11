package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ContactHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private long humanId;

    private ImageView avatarIV;
    private TextView titleTV;
    private FloatingActionButton connectFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        this.avatarIV = findViewById(R.id.activity_profile_avatar_image_view);
        this.titleTV = findViewById(R.id.activity_profile_title_text_view);
        this.connectFAB = findViewById(R.id.connectFAB);

        if (getIntent().getExtras() != null)
            this.humanId = getIntent().getExtras().getLong("user-id");

        final Entities.User user = DatabaseHelper.getHumanById(humanId);

        if (user != null) {
            fillContent(user);
            syncData();
        } else {
            syncData();
        }

        Entities.User me = DatabaseHelper.getMe();
        if (me != null) {
            if (humanId == me.getBaseUserId()) {
                connectFAB.setImageResource(R.drawable.ic_home);
            } else {
                if (DatabaseHelper.isContactInDatabase(humanId)) {
                    connectFAB.setImageResource(R.drawable.ic_message);
                } else {
                    connectFAB.setImageResource(R.drawable.ic_connection);
                }
            }
        }
    }

    private void syncData() {
        DataSyncer.syncBaseUserWithServer(humanId, new OnBaseUserSyncListener() {
            @Override
            public void userSynced(Entities.BaseUser user) {
                fillContent(user);
            }
            @Override
            public void syncFailed() { }
        });
    }

    private void fillContent(Entities.BaseUser user) {
        titleTV.setText(user.getTitle());
        NetworkHelper.loadUserAvatar(user.getAvatar(), avatarIV);
    }

    public void onConnectBtnClicked(View view) {
        Entities.User me = DatabaseHelper.getMe();
        if (me != null) {
            if (humanId == me.getBaseUserId()) {
                Entities.Complex homeComplex = DatabaseHelper.getMe().getUserSecret().getHome();
                Entities.Room mainRoom = homeComplex.getRooms().get(0);
                startActivity(new Intent(ProfileActivity
                        .this, RoomActivity.class)
                        .putExtra("complex_id", homeComplex.getComplexId())
                        .putExtra("room_id", mainRoom.getRoomId()));
            } else {
                if (DatabaseHelper.isContactInDatabase(humanId)) {
                    long complexId = 0;
                    for (Entities.Contact contact : DatabaseHelper.getContacts()) {
                        if (contact.getPeerId() == humanId) {
                            complexId = contact.getComplexId();
                        }
                    }
                    Entities.Room room = DatabaseHelper.getRooms(complexId).get(0);
                    final long foundRoomId = room.getRoomId();
                    startActivity(new Intent(ProfileActivity
                            .this, RoomActivity.class)
                            .putExtra("complex_id", complexId)
                            .putExtra("room_id", foundRoomId));
                } else {
                    Packet packet = new Packet();
                    Entities.User user = new Entities.User();
                    user.setBaseUserId(humanId);
                    packet.setUser(user);
                    ContactHandler contactHandler = NetworkHelper.getRetrofit().create(ContactHandler.class);
                    Call<Packet> call = contactHandler.createContact(packet);
                    NetworkHelper.requestServer(call, new ServerCallback() {
                        @Override
                        public void onRequestSuccess(Packet packet) {
                            DatabaseHelper.notifyComplexCreated(packet.getContact().getComplex());
                            DatabaseHelper.notifyRoomCreated(packet.getContact().getComplex()
                                    .getRooms().get(0));
                            DatabaseHelper.notifyContactCreated(packet.getContact());
                            DatabaseHelper.notifyServiceMessageReceived(packet.getServiceMessage());
                            GraphicHelper.getContactListener().contactCreated(packet.getContact());
                            connectFAB.setImageResource(R.drawable.ic_message);
                            ProfileActivity.this.startActivity(new Intent(ProfileActivity
                                    .this, RoomActivity.class)
                                    .putExtra("complex_id", packet.getContact()
                                            .getComplex().getComplexId())
                                    .putExtra("room_id", packet.getContact()
                                            .getComplex().getRooms().get(0).getRoomId()));
                        }

                        @Override
                        public void onServerFailure() {
                            Toast.makeText(ProfileActivity.this, "Contact creation failure"
                                    , Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onConnectionFailure() {
                            Toast.makeText(ProfileActivity.this, "Contact creation failure"
                                    , Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
