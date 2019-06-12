package kasper.android.pulse.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RobotHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Objects;

public class BotStoreBotActivity extends BaseActivity {

    private TextView titleTV;
    private CircleImageView avatarIV;
    private Button subscribeBtn;
    private CardView subscribeContainer;
    private TextView descTV;
    private ImageView screenshot1IV;
    private ImageView screenshot2IV;
    private ImageView screenshot3IV;
    private ImageView screenshot4IV;
    private ImageView screenshot5IV;

    private Entities.Bot bot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_store_bot);

        bot = (Entities.Bot) Objects.requireNonNull(getIntent().getExtras()).getSerializable("bot");

        titleTV = findViewById(R.id.botTitle);
        avatarIV = findViewById(R.id.botAvatar);
        subscribeBtn = findViewById(R.id.botSubscribe);
        subscribeContainer = findViewById(R.id.botSubscribeContainer);
        descTV = findViewById(R.id.botDesc);
        screenshot1IV = findViewById(R.id.botScreenShot1);
        screenshot2IV = findViewById(R.id.botScreenShot2);
        screenshot3IV = findViewById(R.id.botScreenShot3);
        screenshot4IV = findViewById(R.id.botScreenShot4);
        screenshot5IV = findViewById(R.id.botScreenShot5);

        updateBotInfo();
        updateSubscribeBtn();
        updateScreenShots();
    }

    private void updateBotInfo() {
        NetworkHelper.loadBotAvatar(bot.getAvatar(), avatarIV);
        titleTV.setText(bot.getTitle());
        descTV.setText(bot.getDescription());
    }

    @SuppressLint("SetTextI18n")
    private void updateSubscribeBtn() {
        if (DatabaseHelper.isBotSubscribed(bot.getBaseUserId())) {
            subscribeContainer.setCardBackgroundColor(Color.GREEN);
            subscribeBtn.setTextColor(Color.GREEN);
            subscribeBtn.setText("Subscribed");
        } else {
            subscribeContainer.setCardBackgroundColor(getResources().getColor(R.color.colorBlue));
            subscribeBtn.setTextColor(getResources().getColor(R.color.colorBlue));
            subscribeBtn.setText("Subscribe");
        }
    }

    private void updateScreenShots() {
        Glide.with(this).load("https://raw.githubusercontent.com/theprogrammermachine/KasperPulseFramework/master/images/image1.jpg").into(screenshot1IV);
        Glide.with(this).load("https://raw.githubusercontent.com/theprogrammermachine/KasperPulseFramework/master/images/image2.jpg").into(screenshot2IV);
        Glide.with(this).load("https://raw.githubusercontent.com/theprogrammermachine/KasperPulseFramework/master/images/image3.jpg").into(screenshot3IV);
        Glide.with(this).load("https://raw.githubusercontent.com/theprogrammermachine/KasperPulseFramework/master/images/image1.jpg").into(screenshot4IV);
        Glide.with(this).load("https://raw.githubusercontent.com/theprogrammermachine/KasperPulseFramework/master/images/image2.jpg").into(screenshot5IV);
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }

    public void onSubscribeBtnClicked(View view) {
        if (!DatabaseHelper.isBotSubscribed(bot.getBaseUserId())) {
            Packet packet = new Packet();
            packet.setBot(bot);
            RobotHandler robotHandler = NetworkHelper.getRetrofit().create(RobotHandler.class);
            Call<Packet> call = robotHandler.subscribeBot(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    Entities.BotSubscription subscription = packet.getBotSubscription();
                    DatabaseHelper.notifyBotSubscribed(bot, subscription);
                    runOnUiThread(BotStoreBotActivity.this::updateSubscribeBtn);
                }

                @Override
                public void onServerFailure() {
                    Toast.makeText(BotStoreBotActivity.this, "Bot subscription failure"
                            , Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionFailure() {
                    Toast.makeText(BotStoreBotActivity.this, "Bot subscription failure"
                            , Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
