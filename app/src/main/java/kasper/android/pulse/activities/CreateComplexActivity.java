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
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.Tuple;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.FileRegistered;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.services.AsemanService;
import retrofit2.Call;

public class CreateComplexActivity extends BaseActivity {

    private CircleImageView avatarIV;
    private EditText nameET;
    private FrameLayout loadingView;
    private CircularProgressBar progressBar;
    private OneClickFAB saveFAB;

    private long fileId;

    File selectedImageFile;
    Entities.Complex complex = null;
    Entities.BaseRoom room = null;
    Entities.ServiceMessage message = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_complex);

        Core.getInstance().bus().register(this);

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
        final String name = nameET.getText().toString();
        if (name.length() > 0) {
            if (selectedImageFile != null && selectedImageFile.exists()) {
                loadingView.setVisibility(View.VISIBLE);
            } else {
                loadingView.setVisibility(View.GONE);
            }
            final Packet packet = new Packet();
            complex = new Entities.Complex();
            complex.setTitle(name);
            complex.setAvatar(0);
            packet.setComplex(complex);
            final ComplexHandler complexHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
            Call<Packet> call = complexHandler.createComplex(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    complex = packet.getComplex();
                    Entities.ComplexSecret complexSecret = packet.getComplexSecret();
                    room = complex.getAllRooms().get(0);
                    message = packet.getServiceMessage();
                    DatabaseHelper.notifyComplexCreated(complex);
                    for (Entities.Membership mem : complex.getMembers()) {
                        DatabaseHelper.notifyUserCreated(mem.getUser());
                        DatabaseHelper.notifyMembershipCreated(mem);
                        if (mem.getMemberAccess() != null)
                            DatabaseHelper.notifyMemberAccessCreated(mem.getMemberAccess());
                    }
                    DatabaseHelper.notifyComplexSecretCreated(complexSecret);
                    DatabaseHelper.notifyRoomCreated(room);
                    DatabaseHelper.notifyServiceMessageReceived(message);
                    if (selectedImageFile != null && selectedImageFile.exists()) {
                        Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData =
                                AsemanService.uploadFile(new Uploading(DocTypes.Photo, selectedImageFile.getPath()
                                        , -1, -1, true, false));
                        fileId = uploadData.fifth.getFileId();
                    } else {
                        taskDone(complex, room, message);
                    }
                }
                @Override
                public void onServerFailure() {
                    Toast.makeText(CreateComplexActivity.this, "Complex creation failure", Toast.LENGTH_SHORT).show();
                    saveFAB.enable();
                }
                @Override
                public void onConnectionFailure() {
                    Toast.makeText(CreateComplexActivity.this, "Complex creation failure", Toast.LENGTH_SHORT).show();
                    saveFAB.enable();
                }
            });
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
        if (fileRegistered.getLocalFileId() == fileId)
            fileId = fileRegistered.getOnlineFileId();
    }

    @Subscribe
    public void onFileUploaded(FileUploaded fileUploaded) {
        if (fileUploaded.getOnlineFileId() == fileId) {
            loadingView.setVisibility(View.GONE);
            complex.setAvatar(fileUploaded.getOnlineFileId());
            Packet packet2 = new Packet();
            packet2.setComplex(complex);
            ComplexHandler profileHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
            Call<Packet> call2 = profileHandler.updateComplexProfile(packet2);
            NetworkHelper.requestServer(call2, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    DatabaseHelper.updateComplex(complex);
                    taskDone(complex, room, message);
                }

                @Override
                public void onServerFailure() {
                    Core.getInstance().bus().post(new ShowToast("Complex profile update failure"));
                    taskDone(complex, room, message);
                }

                @Override
                public void onConnectionFailure() {
                    Core.getInstance().bus().post(new ShowToast("Complex profile update failure"));
                    taskDone(complex, room, message);
                }
            });
        }
    }

    private void taskDone(Entities.Complex complex, Entities.BaseRoom room, Entities.ServiceMessage message) {
        room.setComplex(complex);
        room.setLastAction(message);
        Core.getInstance().bus().post(new ComplexCreated(complex));
        Core.getInstance().bus().post(new RoomCreated(complex.getComplexId(), room));
        finish();
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
