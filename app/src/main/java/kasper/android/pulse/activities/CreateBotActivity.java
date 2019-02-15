package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Environment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.OnFileUploadListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import retrofit2.Call;

public class CreateBotActivity extends AppCompatActivity {

    private CircleImageView avatarIV;
    private EditText nameET;
    private EditText descET;
    private FrameLayout loadingView;
    private CircularProgressBar progressBar;

    boolean donePressed = false;

    File selectedImageFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_bot);
        avatarIV = findViewById(R.id.createBotAvatarIV);
        descET = findViewById(R.id.createBotDescET);
        nameET = findViewById(R.id.createBotNameET);
        loadingView = findViewById(R.id.createBotLoadingView);
        progressBar = findViewById(R.id.createBotProgressBar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                String path = Objects.requireNonNull(data.getExtras()).getString("path");
                selectedImageFile = new File(Objects.requireNonNull(path));
                GlideApp.with(this).load(path).into(avatarIV);
            }
        }
    }

    public void onPickAvatarBtnClicked(View view) {
        startActivityForResult(new Intent(this, PickImageActivity.class), 123);
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }

    public void onSaveBtnClicked(View view) {
        final String botName = nameET.getText().toString();
        final String botDesc = descET.getText().toString();
        if (botName.length() > 0 && botDesc.length() > 0) {
            if (!donePressed) {
                donePressed = true;
                if (selectedImageFile != null && selectedImageFile.exists()) {
                    loadingView.setVisibility(View.VISIBLE);
                } else {
                    loadingView.setVisibility(View.GONE);
                }
                if (selectedImageFile != null && selectedImageFile.exists()) {
                    Pair<Entities.File, Entities.FileLocal> pair = DatabaseHelper
                            .notifyPhotoUploading(true, selectedImageFile.getPath(), 56, 56);
                    Entities.Photo file = (Entities.Photo) pair.first;
                    NetworkHelper.uploadFile(file, -1, -1, selectedImageFile.getPath(),
                            progress -> Core.getInstance().bus().post(new UiThreadRequested(() ->
                                    progressBar.setProgress(progress))), (OnFileUploadListener) (fileId, fileUsageId) -> {
                                createBot(botName, fileId, botDesc);
                            });
                } else {
                    createBot(botName, 0, botDesc);
                }
            }
        }
    }

    private void createBot(String botName, long botAvatar, String description) {
        final Packet packet = new Packet();
        Entities.Bot bot = new Entities.Bot();
        bot.setTitle(botName);
        bot.setAvatar(botAvatar);
        bot.setDescription(description);
        packet.setBot(bot);
        RobotHandler botHandler = NetworkHelper.getRetrofit().create(RobotHandler.class);
        Call<Packet> call = botHandler.createBot(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                final Entities.Bot bot = packet.getBot();
                final Entities.Session botSess = bot.getSessions().get(0);
                final Entities.BotCreation creation = packet.getBotCreation();
                final Entities.BotSubscription subscription = packet.getBotSubscription();
                DatabaseHelper.notifyBotCreated(bot, creation);
                DatabaseHelper.notifyBotSubscribed(bot, subscription);
                DatabaseHelper.createSession(botSess, false);
                finish();
            }

            @Override
            public void onServerFailure() {
                Toast.makeText(CreateBotActivity.this, "Bot creation failure", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionFailure() {
                Toast.makeText(CreateBotActivity.this, "Bot creation failure", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
