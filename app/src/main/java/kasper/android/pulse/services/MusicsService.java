package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import androidx.annotation.Nullable;
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
        Log.d("KasperLogger", "Music service started");
        if (intent != null && intent.getExtras() != null) {
            String command = intent.getExtras().getString("command");
            if (command != null) {
                switch (command) {
                    case "play":
                        Log.d("KasperLogger", "play");
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
                        Log.d("KasperLogger", "resume");
                        try {
                            mediaPlayer.start();
                        } catch (Exception ignored) {
                            ignored.printStackTrace();
                        }
                        break;
                    case "pause":
                        Log.d("KasperLogger", "pause");
                        try {
                            mediaPlayer.pause();
                        } catch (Exception ignored) {
                        }
                        break;
                    case "stop":
                        Log.d("KasperLogger", "stop");
                        try {
                            mediaPlayer.stop();
                        } catch (Exception ignored) {
                        }
                        break;
                    case "seek-to":
                        Log.d("KasperLogger", "seek-to");
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
        Log.d("KasperLogger", "Music service destroyed");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }
}
