package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
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
import kasper.android.pulse.callbacks.ui.ComplexListener;
import kasper.android.pulse.callbacks.ui.RoomListener;
import kasper.android.pulse.components.OneClickFAB;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateComplexActivity extends AppCompatActivity {

    private CircleImageView avatarIV;
    private EditText nameET;
    private FrameLayout loadingView;
    private CircularProgressBar progressBar;
    private OneClickFAB saveFAB;

    File selectedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_complex);

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

    Entities.Complex complex = null;

    public void onOkBtnClicked(View view) {
        final String name = nameET.getText().toString();
        if (name.length() > 0) {
            loadingView.setVisibility(View.VISIBLE);
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
                    DatabaseHelper.notifyComplexCreated(complex);
                    DatabaseHelper.notifyComplexSecretCreated(complexSecret);
                    DatabaseHelper.notifyRoomCreated(complex.getRooms().get(0));
                    if (selectedImageFile != null && selectedImageFile.exists()) {
                        Pair<Entities.File, Entities.FileLocal> pair = DatabaseHelper.notifyPhotoUploading(true, selectedImageFile.getPath()
                                , 56, 56);
                        Entities.File file = pair.first;
                        NetworkHelper.uploadFile(file, -1, -1, selectedImageFile.getPath(),
                                progress -> GraphicHelper.runOnUiThread(() ->
                                        progressBar.setProgress(progress))
                                , (OnFileUploadListener) (fileId, fileUsageId) -> {
                                    File sourceFile = new File(selectedImageFile.getPath());
                                    File destFile = new File(new File(Environment
                                            .getExternalStorageDirectory(), DatabaseHelper
                                            .StorageDir), fileId + "");
                                    try {
                                        FileUtils.copyFile(sourceFile, destFile);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                    GraphicHelper.runOnUiThread(() -> {
                                        loadingView.setVisibility(View.GONE);
                                        complex.setAvatar(fileId);
                                        Packet packet2 = new Packet();
                                        packet2.setComplex(complex);
                                        ComplexHandler profileHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
                                        Call<Packet> call2 = profileHandler.updateComplexProfile(packet2);
                                        NetworkHelper.requestServer(call2, new ServerCallback() {
                                            @Override
                                            public void onRequestSuccess(Packet packet) {
                                                DatabaseHelper.updateComplex(complex);
                                                GraphicHelper.getComplexListener().notifyComplexCreated(complex);
                                                Entities.Room room = complex.getRooms().get(0);
                                                room.setComplex(complex);
                                                GraphicHelper.getRoomListener().roomCreated(complex.getComplexId(), room);
                                                finish();
                                            }

                                            @Override
                                            public void onServerFailure() {
                                                Toast.makeText(CreateComplexActivity.this, "Complex profile update failure", Toast.LENGTH_SHORT).show();
                                            }

                                            @Override
                                            public void onConnectionFailure() {
                                                Toast.makeText(CreateComplexActivity.this, "Complex profile update failure", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                                });
                    } else {
                        GraphicHelper.runOnUiThread(() -> {
                            for (ComplexListener complexListener : GraphicHelper.getComplexListeners()) {
                                complexListener.notifyComplexCreated(complex);
                            }
                            Entities.Room room = complex.getRooms().get(0);
                            for (RoomListener roomListener : GraphicHelper.getRoomListeners()) {
                                roomListener.roomCreated(complex.getComplexId(), room);
                            }
                            finish();
                        });
                    }
                }

                @Override
                public void onServerFailure() {
                    Toast.makeText(CreateComplexActivity.this, "Complex creation failure", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionFailure() {
                    Toast.makeText(CreateComplexActivity.this, "Complex creation failure", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
