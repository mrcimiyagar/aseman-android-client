package kasper.android.pulse.activities;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

import kasper.android.pulse.R;
import kasper.android.pulse.components.ZoomableExoPlayerView;

public class VideoPlayerActivity extends AppCompatActivity implements ExoPlayer.EventListener {

    private String videoUri = "https://www.w3schools.com/html/mov_bbb.mp4";
    private SimpleExoPlayer player;
    private int CACHE_SIZE_BYTES = 10 * 1024 * 1024;
    private static ZoomableExoPlayerView exoPlayerView;
    public static boolean switched = true;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exoplayer);

        this.videoUri = "";

        try {
            this.videoUri = Uri.fromFile(new File(getIntent().getExtras().getString("video-path"))).toString();
        }
        catch (Exception exception) {

        }
        finally {
            if (this.videoUri == null || this.videoUri.length() == 0) {
                this.videoUri = getIntent().getExtras().getString("video-url");
            }
        }

        exoPlayerView = findViewById(R.id.activity_exoplayer_player_view);

        // 1. Create a default TrackSelector
        Handler mainHandler = new Handler();
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        // Produces DataSource instances through which media data is loaded.
        DataSource.Factory dataSourceFactory = buildDataSourceFactory();

        // 2. Create a default LoadControl
        LoadControl loadControl = new DefaultLoadControl();

        // Produces Extractor instances for parsing the media data.
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();

        // 3. Create the player
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector, loadControl);
        exoPlayerView.setControllerShowTimeoutMs(0);
        exoPlayerView.setPlayer(player);
        player.setPlayWhenReady(true);
        MediaSource videoSource = new ExtractorMediaSource( Uri.parse( videoUri ), dataSourceFactory, extractorsFactory, mainHandler, null );
        player.prepare( videoSource );

        exoPlayerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (switched){
                    exoPlayerView.hideController();
                }else {
                    exoPlayerView.showController();
                }
            }
        });

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

        switch (playbackState) {
            case ExoPlayer.STATE_BUFFERING:
                //You can use progress dialog to show user that video is preparing or buffering so please wait
                break;
            case ExoPlayer.STATE_IDLE:
                //idle state
                break;
            case ExoPlayer.STATE_READY:
                // dismiss your dialog here because our video is ready to play now
                break;
            case ExoPlayer.STATE_ENDED:
                // do your processing after ending of video

                Log.d("KasperLogger", "hello !");

                break;
        }
    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

        AlertDialog.Builder adb = new AlertDialog.Builder(VideoPlayerActivity.this);
        adb.setTitle("Was not able to stream video");
        adb.setMessage("It seems that something is going wrong.\nPlease try again.");
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish(); // take out user from this activity. you can skip this
            }
        });
        AlertDialog ad = adb.create();
        ad.show();
    }

    @Override
    public void onPositionDiscontinuity(int reason) {

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

    }

    @Override
    public void onSeekProcessed() {

    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

    }



    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.setPlayWhenReady(false); //to pause a video because now our video player is not in focus
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.release();
    }

    public DataSource.Factory buildDataSourceFactory() {
        return new DataSource.Factory() {
            @Override
            public DataSource createDataSource() {
                LeastRecentlyUsedCacheEvictor evictor = new LeastRecentlyUsedCacheEvictor( CACHE_SIZE_BYTES );
                //Your cache dir
                File cacheDir = new File(Environment.getExternalStorageDirectory()+ File.separator+"abc/abc.mp4");
//                File cacheDir = getDir("videos", MODE_PRIVATE);
                SimpleCache simpleCache = new SimpleCache( cacheDir, evictor );
                DataSource dataSource = buildMyDataSourceFactory().createDataSource();
                int cacheFlags = CacheDataSource.FLAG_BLOCK_ON_CACHE ;
                return new CacheDataSource( simpleCache, dataSource, cacheFlags, CACHE_SIZE_BYTES );
            }
        };
    }

    private DefaultDataSource.Factory buildMyDataSourceFactory() {
        return new DefaultDataSourceFactory(VideoPlayerActivity.this, Util.getUserAgent(this, "Exo2"), null);
    }

    public static void changeControllerVisibility(boolean visible){
        if (visible){
            exoPlayerView.hideController();
        }else {
            exoPlayerView.showController();
        }
    }

}
