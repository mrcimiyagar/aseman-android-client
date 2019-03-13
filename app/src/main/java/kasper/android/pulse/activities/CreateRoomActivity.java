package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.components.OneClickFAB;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.Tuple;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.rxbus.notifications.FileRegistered;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.services.AsemanService;
import retrofit2.Call;

public class CreateRoomActivity extends AppCompatActivity {

    private long complexId = -1;

    private CircleImageView avatarIV;
    private EditText nameET;
    private FrameLayout loadingView;
    private CircularProgressBar progressBar;
    private OneClickFAB saveFAB;

    File selectedImageFile = null;

    private long fileId;
    private Entities.Complex complex = null;
    private Entities.Room room = null;
    private Entities.ServiceMessage message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        Core.getInstance().bus().register(this);

        if (getIntent().getExtras() != null) {
            complexId = getIntent().getExtras().getLong("complex_id");
            complex = DatabaseHelper.getComplexById(complexId);
        }

        avatarIV = findViewById(R.id.activity_create_room_avatar_image_view);
        nameET = findViewById(R.id.activity_create_room_name_edit_text);
        loadingView = findViewById(R.id.page_add_room_loading_view);
        progressBar = findViewById(R.id.page_add_room_progress_bar);
        saveFAB = findViewById(R.id.saveFAB);
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras() != null) {
                    String path = data.getExtras().getString("path");
                    if (path != null) {
                        selectedImageFile = new File(path);
                        GlideApp.with(this).load(path).into(avatarIV);
                    }
                }
            }
        }
    }

    public void onPickAvatarBtnClicked(View view) {
        startActivityForResult(new Intent(this, PickImageActivity.class), 123);
    }

    public void onOkBtnClicked(View view) {
        final String roomName = nameET.getText().toString();
        if (roomName.length() > 0) {
            if (selectedImageFile != null && selectedImageFile.exists()) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
            }
            final Packet packet = new Packet();
            Entities.Complex c = new Entities.Complex();
            c.setComplexId(complexId);
            packet.setComplex(c);
            Entities.Room r = new Entities.Room();
            r.setTitle(roomName);
            r.setAvatar(0);
            packet.setRoom(r);
            RoomHandler roomHandler = NetworkHelper.getRetrofit().create(RoomHandler.class);
            Call<Packet> call = roomHandler.createRoom(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    room = packet.getRoom();
                    message = packet.getServiceMessage();
                    room.setComplex(complex);
                    DatabaseHelper.notifyRoomCreated(room);
                    DatabaseHelper.notifyServiceMessageReceived(message);
                    if (selectedImageFile != null && selectedImageFile.exists()) {
                        Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData =
                                AsemanService.uploadFile(new Uploading(DocTypes.Photo, selectedImageFile.getPath()
                                        , -1, -1, true, false));
                        fileId = uploadData.first.getFileId();
                    } else {
                        taskDone(complex, room, message);
                    }
                }

                @Override
                public void onServerFailure() {
                    Toast.makeText(CreateRoomActivity.this, "Room creation failure", Toast.LENGTH_SHORT).show();
                    saveFAB.enable();
                }

                @Override
                public void onConnectionFailure() {
                    Toast.makeText(CreateRoomActivity.this, "Room creation failure", Toast.LENGTH_SHORT).show();
                    saveFAB.enable();
                }
            });
        } else {
            Toast.makeText(CreateRoomActivity.this, "Room name can not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe
    public void onFileTransferProgressed(FileTransferProgressed progressed) {
        if (progressed.getFileId() == fileId) {
            progressBar.setProgress(progressed.getProgress());
        }
    }

    @Subscribe
    public void onFileRegistered(FileRegistered fileRegistered) {
        if (fileRegistered.getLocalFileId() == fileId) {
            fileId = fileRegistered.getOnlineFileId();
        }
    }

    @Subscribe
    public void onFileUploaded(FileUploaded fileUploaded) {
        if (fileUploaded.getOnlineFileId() == fileId) {
            loadingView.setVisibility(View.GONE);
            Packet packet2 = new Packet();
            room.setAvatar(fileUploaded.getOnlineFileId());
            packet2.setRoom(room);
            packet2.setComplex(complex);
            RoomHandler profileHandler = NetworkHelper.getRetrofit().create(RoomHandler.class);
            Call<Packet> call2 = profileHandler.updateRoomProfile(packet2);
            NetworkHelper.requestServer(call2, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    DatabaseHelper.updateRoom(room);
                    taskDone(complex, room, message);
                }
                @Override
                public void onServerFailure() {
                    Core.getInstance().bus().post(new ShowToast("Room profile update failure"));
                    taskDone(complex, room, message);
                }
                @Override
                public void onConnectionFailure() {
                    Core.getInstance().bus().post(new ShowToast("Room profile update failure"));
                    taskDone(complex, room, message);
                }
            });
        }
    }

    private void taskDone(Entities.Complex complex, Entities.Room room, Entities.ServiceMessage message) {
        room.setComplex(complex);
        room.setLastAction(message);
        Core.getInstance().bus().post(new RoomCreated(complexId, room));
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(message.getMessageId());
        messageLocal.setSent(true);
        Core.getInstance().bus().post(new MessageReceived(true, message, messageLocal));
        finish();
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
