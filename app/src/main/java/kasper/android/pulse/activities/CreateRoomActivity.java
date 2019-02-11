package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.apache.commons.io.FileUtils;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.OnFileUploadListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.callbacks.ui.RoomListener;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RoomHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateRoomActivity extends AppCompatActivity {

    private long complexId = -1;

    private CircleImageView avatarIV;
    private EditText nameET;
    private FrameLayout loadingView;
    private CircularProgressBar progressBar;

    boolean donePressed;

    File selectedImageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        if (getIntent().getExtras() != null)
            complexId = getIntent().getExtras().getLong("complex_id");

        donePressed = false;

        avatarIV = findViewById(R.id.activity_create_room_avatar_image_view);
        nameET = findViewById(R.id.activity_create_room_name_edit_text);
        loadingView = findViewById(R.id.page_add_room_loading_view);
        progressBar = findViewById(R.id.page_add_room_progress_bar);
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
            if (!donePressed) {
                donePressed = true;
                loadingView.setVisibility(View.VISIBLE);
                final Packet packet = new Packet();
                final Entities.Complex complex = new Entities.Complex();
                complex.setComplexId(complexId);
                packet.setComplex(complex);
                Entities.Room room = new Entities.Room();
                room.setTitle(roomName);
                room.setAvatar(0);
                packet.setRoom(room);
                RoomHandler roomHandler = NetworkHelper.getRetrofit().create(RoomHandler.class);
                Call<Packet> call = roomHandler.createRoom(packet);
                NetworkHelper.requestServer(call, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        final Entities.Room room = packet.getRoom();
                        DatabaseHelper.notifyRoomCreated(room);
                        Pair<Entities.File, Entities.FileLocal> pair = DatabaseHelper.notifyPhotoUploading(true, selectedImageFile.getPath(), 56, 56);
                        Entities.Photo file = (Entities.Photo) pair.first;
                        if (selectedImageFile != null && selectedImageFile.exists()) {
                            NetworkHelper.uploadFile(file, complexId, -1, selectedImageFile.getPath(),
                                    progress -> GraphicHelper.runOnUiThread(() ->
                                            progressBar.setProgress(progress)),
                                    (OnFileUploadListener) (fileId, fileUsageId) -> {
                                        File sourceFile = new File(selectedImageFile.getPath());
                                        File destFile = new File(new File(Environment
                                                .getExternalStorageDirectory(), DatabaseHelper.StorageDir)
                                                , fileId + "");
                                        try {
                                            FileUtils.copyFile(sourceFile, destFile);
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }
                                        GraphicHelper.runOnUiThread(() -> {
                                            loadingView.setVisibility(View.GONE);
                                            Packet packet2 = new Packet();
                                            room.setAvatar(fileId);
                                            packet2.setRoom(room);
                                            packet2.setComplex(complex);
                                            RoomHandler profileHandler = NetworkHelper.getRetrofit().create(RoomHandler.class);
                                            Call<Packet> call2 = profileHandler.updateRoomProfile(packet2);
                                            NetworkHelper.requestServer(call2, new ServerCallback() {
                                                @Override
                                                public void onRequestSuccess(Packet packet) {
                                                    DatabaseHelper.updateRoom(room);
                                                    GraphicHelper.getRoomListener().roomCreated(complexId, room);
                                                    finish();
                                                }

                                                @Override
                                                public void onServerFailure() {
                                                    Toast.makeText(CreateRoomActivity.this, "Room profile update failure", Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onConnectionFailure() {
                                                    Toast.makeText(CreateRoomActivity.this, "Room profile update failure", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        });
                                    });
                        } else {
                            GraphicHelper.runOnUiThread(() -> {
                                for (RoomListener roomListener : GraphicHelper.getRoomListeners()) {
                                    roomListener.roomCreated(complexId, room);
                                }
                                finish();
                            });
                        }
                    }

                    @Override
                    public void onServerFailure() {
                        Toast.makeText(CreateRoomActivity.this, "Room creation failure", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionFailure() {
                        Toast.makeText(CreateRoomActivity.this, "Room creation failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            Toast.makeText(CreateRoomActivity.this, "Room name can not be empty", Toast.LENGTH_SHORT).show();
        }
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
