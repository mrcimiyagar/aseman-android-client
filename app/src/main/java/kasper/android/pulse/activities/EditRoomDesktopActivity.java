package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import studio.carbonylgroup.textfieldboxes.ExtendedEditText;

public class EditRoomDesktopActivity extends AppCompatActivity {

    ExtendedEditText backgroundUrlET;
    Entities.BaseRoom room;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_room_desktop);

        room = DatabaseHelper.getRoomById(getIntent().getExtras().getLong("room-id"));

        backgroundUrlET = findViewById(R.id.editRoomDesktopBackgroundUrl);
        if (room.getBackgroundUrl() != null)
            backgroundUrlET.setText(room.getBackgroundUrl());
    }

    public void onSaveBtnClicked(View view) {
        Packet packet = new Packet();
        Entities.BaseRoom r = new Entities.BaseRoom();
        r.setRoomId(room.getRoomId());
        r.setTitle(room.getTitle());
        r.setAvatar(room.getAvatar());
        r.setBackgroundUrl(backgroundUrlET.getText().toString());
        r.setComplexId(room.getComplexId());
        packet.setBaseRoom(r);
        NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(RoomHandler.class)
                .updateRoomProfile(packet), new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                room.setBackgroundUrl(backgroundUrlET.getText().toString());
                DatabaseHelper.notifyRoomCreated(room);
                finish();
            }

            @Override
            public void onServerFailure() {
                Core.getInstance().bus().post(new ShowToast("Server failure error."));
            }

            @Override
            public void onConnectionFailure() {
                Core.getInstance().bus().post(new ShowToast("Connection error."));
            }
        });
    }
}
