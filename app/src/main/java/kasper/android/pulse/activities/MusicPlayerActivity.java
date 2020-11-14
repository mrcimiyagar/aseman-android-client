package kasper.android.pulse.activities;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import kasper.android.pulse.R;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.services.MusicsService;

public class MusicPlayerActivity extends BaseActivity {

    TextView captionTV;
    SeekBar progressSB;
    ImageButton shuffleBTN;
    ImageButton replayBTN;
    ImageButton playBTN;

    RecyclerView audiosRV;

    long roomId;
    Entities.Audio audio;

    final Object lockObject = new Object();
    Handler handler = new Handler();
    Runnable runnable;

    private boolean shuffle = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_player);

        if (getIntent().getExtras() != null) {
            roomId = getIntent().getExtras().getLong("room-id");
            audio = (Entities.Audio) getIntent().getExtras().getSerializable("audio");
        }

        initViews();
        initDecorations();
        initList();
        initMediaPlayer();
    }

    private void initViews() {
        captionTV = findViewById(R.id.activity_music_player_caption_text_view);
        progressSB = findViewById(R.id.activity_music_player_progress_seek_bar);
        shuffleBTN = findViewById(R.id.activity_music_player_shuffle_image_button);
        replayBTN = findViewById(R.id.activity_music_player_replay_image_button);
        playBTN = findViewById(R.id.activity_music_player_play_image_button);
        audiosRV = findViewById(R.id.activity_music_player_recycler_view);
    }

    private void initDecorations() {
        audiosRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initList() {

    }

    private void initMediaPlayer() {
        captionTV.setText(audio.getTitle());
        try {
            playBTN.setImageResource(R.drawable.ic_pause);
            startService(new Intent(this, MusicsService.class)
                            .putExtra("command", "play")
                            .putExtra("path", DatabaseHelper.getFilePath(audio.getFileId())));
            updateProgressBar();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateProgressBar() {
        synchronized (lockObject) {
            runnable = () -> {
                synchronized (lockObject) {
                    updateProgressBar();
                }
            };
            handler.postDelayed(runnable, 500);
        }
    }

    public void onShuffleBtnClicked(View view) {
        if (shuffle) {
            shuffle = false;
            shuffleBTN.setColorFilter(Color.WHITE);
        } else {
            shuffle = true;
            shuffleBTN.setColorFilter(Color.YELLOW);
        }
    }

    public void onBackwardBtnClicked(View view) {

    }

    public void onPlayBtnClicked(View view) {

    }

    public void onForwardBtnClicked(View view) {

    }

    public void onReplayBtnClicked(View view) {


    }
}
