package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ContactHandler;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import retrofit2.Call;

public class ProfileActivity extends BaseActivity {

    private long humanId;

    private ImageView avatarIV;
    private TextView titleTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        this.avatarIV = findViewById(R.id.activity_profile_avatar_image_view);
        this.titleTV = findViewById(R.id.activity_profile_title_text_view);

        if (getIntent().getExtras() != null)
            this.humanId = getIntent().getExtras().getLong("user-id");

        final Entities.User user = DatabaseHelper.getHumanById(humanId);

        if (user != null) {
            fillContent(user);
            syncData();
        } else {
            syncData();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    if (data.getExtras() != null) {
                        if (data.getExtras().containsKey("complex")) {
                            Entities.Complex complex = (Entities.Complex) data.getExtras().getSerializable("complex");
                            if (complex != null) {

                            }
                        }
                    }
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
                Entities.BaseRoom mainRoom = homeComplex.getAllRooms().get(0);
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
                    Entities.BaseRoom room = DatabaseHelper.getRooms(complexId).get(0);
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
                            Entities.Contact contact = packet.getContact();
                            Entities.Complex complex = packet.getContact().getComplex();
                            Entities.ComplexSecret complexSecret = packet.getComplexSecret();
                            Entities.BaseRoom room = complex.getAllRooms().get(0);
                            room.setComplex(complex);
                            Entities.ServiceMessage message = packet.getServiceMessage();
                            DatabaseHelper.notifyComplexCreated(complex);
                            DatabaseHelper.notifyComplexSecretCreated(complexSecret);
                            for (Entities.Membership mem : complex.getMembers()) {
                                DatabaseHelper.notifyUserCreated(mem.getUser());
                                DatabaseHelper.notifyMembershipCreated(mem);
                                if (mem.getMemberAccess() != null)
                                    DatabaseHelper.notifyMemberAccessCreated(mem.getMemberAccess());
                            }
                            DatabaseHelper.notifyRoomCreated(room);
                            DatabaseHelper.notifyContactCreated(contact);
                            DatabaseHelper.notifyServiceMessageReceived(message);
                            Core.getInstance().bus().post(new ContactCreated(contact));
                            Core.getInstance().bus().post(new ComplexCreated(complex));
                            Core.getInstance().bus().post(new RoomCreated(complex.getComplexId(), room));
                            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
                            messageLocal.setMessageId(packet.getServiceMessage().getMessageId());
                            messageLocal.setSent(true);
                            Core.getInstance().bus().post(new MessageReceived(true, message, messageLocal));
                            ProfileActivity.this.startActivity(new Intent(ProfileActivity
                                    .this, RoomActivity.class)
                                    .putExtra("complex_id", packet.getContact()
                                            .getComplex().getComplexId())
                                    .putExtra("room_id", packet.getContact()
                                            .getComplex().getAllRooms().get(0).getRoomId()));
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

    public void onBlockBtnClicked(View view) {

    }

    public void onInviteBtnClicked(View view) {
        startActivity(new Intent(this, UserInvitesActivity.class).putExtra("user_id", humanId));
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
