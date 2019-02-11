package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Objects;

import kasper.android.pulse.R;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;

public class BotProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_profile);

        CardView tokenContainer = findViewById(R.id.botProfileTokenContainer);
        TextView tokenTV = findViewById(R.id.botProfileTokenTV);
        FloatingActionButton subscribeFAB = findViewById(R.id.botProfileSubscribeFAB);
        TextView nameTV = findViewById(R.id.botProfileNameTV);
        ImageView avatarIV = findViewById(R.id.botProfileAvatarIV);

        long botId = Objects.requireNonNull(getIntent().getExtras()).getLong("bot_id");
        Entities.Bot bot = DatabaseHelper.getBotById(botId);
        NetworkHelper.loadBotAvatar(bot.getAvatar(), avatarIV);
        nameTV.setText(bot.getTitle());

        String token = getIntent().getExtras().getString("token");
        if (token != null && token.length() > 0) {
            tokenContainer.setVisibility(View.VISIBLE);
            tokenTV.setText(token);
            tokenTV.setOnLongClickListener(view -> {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Bot Token", tokenTV.getText().toString());
                Objects.requireNonNull(clipboard).setPrimaryClip(clip);
                Toast.makeText(this, "Token copied to clipboard", Toast.LENGTH_SHORT).show();
                return true;
            });
        } else {
            tokenContainer.setVisibility(View.GONE);
            tokenTV.setText("");
        }

        if (DatabaseHelper.isBotSubscribed(botId)) {
            subscribeFAB.setVisibility(View.GONE);
        } else {
            subscribeFAB.setVisibility(View.VISIBLE);
        }
    }

    public void onBackBtnClicked(View view) {
        onBackPressed();
    }

    public void onSubscribeBtnClicked(View view) {

    }
}
