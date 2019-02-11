package kasper.android.pulse.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import kasper.android.pulse.callbacks.network.Callback;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LogoutService extends Service {

    OkHttpClient client;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Entities.Session session = DatabaseHelper.getSingleSession();
        final long sessionId = session.getSessionId();
        final String sessionToken = session.getToken();
        Log.d("KasperLogger", "Logout service started");
        try {
            JSONObject jo = new JSONObject();
            jo.put("SessionId", sessionId);
            jo.put("SessionToken", sessionToken);
            callServer("/Auth/Logout", jo, new Callback() {
                @Override
                public void onResponse(String result) {
                    stopSelf();
                }
            });
        } catch (Exception ignored) {
            ignored.printStackTrace();
        }

        return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
    }

    @Override
    public void onDestroy() {
        Log.d("KasperLogger", "Logout service destroyed");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("KasperLogger", "Logout Service task removed");
        stopSelf();
    }

    private void callServer(final String path, final JSONObject data, final Callback callback) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {

                    client = new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .build();


                    final MediaType JSON = MediaType.parse("application/json");

                    String url = NetworkHelper.API_PATH + path;
                    String json = data.toString();

                    RequestBody body = RequestBody.create(JSON, json);
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String result = response.body().string();

                    Log.d("KasperLogger", result);

                    callback.onResponse(result);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}