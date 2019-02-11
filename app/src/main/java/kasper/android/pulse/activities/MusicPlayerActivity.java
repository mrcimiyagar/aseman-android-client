package kasper.android.pulse.activities;

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

public class MusicPlayerActivity extends AppCompatActivity {

    TextView captionTV;
    SeekBar progressSB;
    ImageButton shuffleBTN;
    ImageButton replayBTN;
    ImageButton playBTN;

    RecyclerView audiosRV;

    long roomId;
    Entities.Audio audio;

    MediaPlayer mediaPlayer;

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
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(DatabaseHelper.getFilePath(audio.getFileId()));
            mediaPlayer.setOnCompletionListener(mp -> playBTN.setImageResource(R.drawable.ic_play));
            progressSB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    synchronized (lockObject) {
                        mediaPlayer.seekTo(mediaPlayer.getDuration() * seekBar.getProgress() / 100);
                    }
                }
            });
            mediaPlayer.prepare();
            mediaPlayer.start();
            updateProgressBar();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateProgressBar() {
        synchronized (lockObject) {
            runnable = () -> {
                synchronized (lockObject) {
                    progressSB.setProgress(mediaPlayer.getCurrentPosition() * 100 / mediaPlayer.getDuration());
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
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playBTN.setImageResource(R.drawable.ic_play);
        } else {
            mediaPlayer.start();
            playBTN.setImageResource(R.drawable.ic_pause);
        }
    }

    public void onForwardBtnClicked(View view) {

    }

    public void onReplayBtnClicked(View view) {
        if (mediaPlayer.isLooping()) {
            mediaPlayer.setLooping(false);
            replayBTN.setColorFilter(Color.WHITE);
        } else {
            mediaPlayer.setLooping(true);
            replayBTN.setColorFilter(Color.YELLOW);
        }

    }
}
