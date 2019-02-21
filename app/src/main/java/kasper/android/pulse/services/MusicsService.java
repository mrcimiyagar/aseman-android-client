package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.annotation.Nullable;
import kasper.android.pulse.helpers.LogHelper;

import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;

public class MusicsService extends IntentService {

    MediaPlayer mediaPlayer = new MediaPlayer();

    public MusicsService() {
        super("Music Service");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        LogHelper.log("KasperLogger", "Music service started");
        if (intent != null && intent.getExtras() != null) {
            String command = intent.getExtras().getString("command");
            if (command != null) {
                switch (command) {
                    case "play":
                        LogHelper.log("KasperLogger", "play");
                        String path = intent.getExtras().getString("path");
                        try {
                            mediaPlayer.reset();
                            final File file = new File(path);
                            FileInputStream is = new FileInputStream(file);
                            FileDescriptor fd = is.getFD();
                            mediaPlayer.setDataSource(fd);
                            is.close();
                            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                                @Override
                                public void onPrepared(MediaPlayer mp) {
                                    mediaPlayer.start();
                                }
                            });
                            mediaPlayer.prepareAsync();
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                        break;
                    case "resume":
                        LogHelper.log("KasperLogger", "resume");
                        try {
                            mediaPlayer.start();
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                        break;
                    case "pause":
                        LogHelper.log("KasperLogger", "pause");
                        try {
                            mediaPlayer.pause();
                        } catch (Exception ignored) {
                        }
                        break;
                    case "stop":
                        LogHelper.log("KasperLogger", "stop");
                        try {
                            mediaPlayer.stop();
                        } catch (Exception ignored) {
                        }
                        break;
                    case "seek-to":
                        LogHelper.log("KasperLogger", "seek-to");
                        int position = intent.getExtras().getInt("position");
                        try {
                            mediaPlayer.seekTo(position);
                        } catch (Exception ignored) {
                        }
                        break;
                }
            }
        }
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("KasperLogger", "Music service task removed");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        LogHelper.log("KasperLogger", "Music service destroyed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}
