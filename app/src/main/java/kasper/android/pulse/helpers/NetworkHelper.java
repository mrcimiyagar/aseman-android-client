package kasper.android.pulse.helpers;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.StreamEncoder;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.OnFileDownloadListener;
import kasper.android.pulse.callbacks.network.OnFileUploadListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.ProgressListener;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.services.FilesService;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by keyhan1376 on 1/28/2018.
 */

public class NetworkHelper {

    //public static String SERVER_IP = "http://192.168.43.161:8080/";
    public static String SERVER_IP = "http://164.215.133.201:8080/";
    public static String API_PATH = SERVER_IP + "api/";
    private static Retrofit retrofit;
    public static Retrofit getRetrofit() {
        return retrofit;
    }
    private static ObjectMapper mapper;
    public static ObjectMapper getMapper() {
        return mapper;
    }
    private static OkHttpClient okHttpClient;

    public static void setup() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.addInterceptor(chain -> {
            Entities.Session session = DatabaseHelper.getSingleSession();
            Request request = chain.request();
            Request.Builder newRequest = request.newBuilder();
            if (session != null)
                newRequest.header("Authorization"
                        , session.getSessionId() + " " + session.getToken());
            return chain.proceed(newRequest.build());
        });
        HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
        loggerInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
        builder.addInterceptor(loggerInterceptor);
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
                .supportsTlsExtensions(true)
                .tlsVersions(TlsVersion.TLS_1_2, TlsVersion.TLS_1_1, TlsVersion.TLS_1_0)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_RC4_128_SHA,
                        CipherSuite.TLS_ECDHE_RSA_WITH_RC4_128_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_DHE_DSS_WITH_AES_128_CBC_SHA,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_256_CBC_SHA)
                .build();

        okHttpClient = builder.connectionSpecs(Arrays.asList(
                spec, ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT)).build();

        mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        retrofit = new Retrofit.Builder()
                .baseUrl(API_PATH)
                .client(okHttpClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();
    }

    public static void uploadFile(Entities.File file, long complexId, long roomId, boolean isAvatar, String path, OnFileUploadListener uploadListener) {
        Uploading uploading = new Uploading();
        uploading.setFile(file);
        uploading.setPath(path);
        uploading.setComplexId(complexId);
        uploading.setRoomId(roomId);
        uploading.setAvatarUsage(isAvatar);
        uploading.setUploadListener(uploadListener);
        FilesService.uploadFile(uploading);
    }

    public static void downloadFile(Entities.File file, long roomId, ProgressListener progressListener, OnFileDownloadListener downloadListener) {
        Downloading downloading = new Downloading();
        downloading.setFile(file);
        downloading.setRoomId(roomId);
        downloading.setProgressListener(progressListener);
        downloading.setDownloadListener(downloadListener);
        FilesService.downloadFile(downloading);
    }

    public static void cancelUploadFile(long fileId) {
        FilesService.cancelUpload(fileId);
    }

    public static void cancelDownloadFile(long fileId) {
        FilesService.cancelDownload(fileId);
    }

    public static String createFileLink(long fileId) {
        return API_PATH + "file/download_file" +
                "?fileId=" + fileId;
    }

    public static void loadUserAvatar(long avatarId, ImageView imageView) {
        loadAvatar(createUserAvatarLink(avatarId), R.drawable.user_empty_icon, imageView);
    }

    public static void loadComplexAvatar(long avatarId, ImageView imageView) {
        loadAvatar(createComplexAvatarLink(avatarId), R.drawable.complex_empty_icon, imageView);
    }

    public static void loadRoomAvatar(long avatarId, ImageView imageView) {
        loadAvatar(createRoomAvatarLink(avatarId), R.drawable.room_empty_icon, imageView);
    }

    public static void loadBotAvatar(long avatarId, ImageView imageView) {
        loadAvatar(createBotAvatarLink(avatarId), R.drawable.robot_empty_icon, imageView);
    }

    public static void requestServer(Call<Packet> call, ServerCallback callback) {
        try {
            Log.d("OkHttp", getMapper().writeValueAsString(call.request().body()));
        } catch (Exception ignored) { }
        call.enqueue(new Callback<Packet>() {
            @Override
            public void onResponse(@NonNull Call<Packet> call, @NonNull Response<Packet> response) {
                try {
                    if (response.body() != null && response.body().getStatus().equals("success")) {
                        Log.d("OkHttp", getMapper().writeValueAsString(response.body()));
                        callback.onRequestSuccess(response.body());
                    } else
                        callback.onServerFailure();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Packet> call, @NonNull Throwable t) {
                try {
                    callback.onConnectionFailure();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private static void loadAvatar(Object path, @DrawableRes int errorDrawable, ImageView imageView) {
        RequestOptions options = new RequestOptions()
                .centerCrop()
                .placeholder(errorDrawable)
                .error(errorDrawable)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .priority(Priority.NORMAL);
        Glide.with(Core.getInstance().getApplicationContext())
                .asBitmap()
                .load(path)
                .apply(options)
                .into(imageView);
    }

    private static Object createUserAvatarLink(long avatarId) {
        if (avatarId > 0) {
            return API_PATH + "file/download_file" +
                    "?fileId=" + avatarId;
        } else {
            return R.drawable.user_empty_icon;
        }
    }

    private static Object createComplexAvatarLink(long avatarId) {
        if (avatarId > 0) {
            return API_PATH + "file/download_file" +
                    "?fileId=" + avatarId;
        } else {
            return R.drawable.complex_empty_icon;
        }
    }

    private static Object createRoomAvatarLink(long avatarId) {
        if (avatarId > 0) {
            return API_PATH + "file/download_file" +
                    "?fileId=" + avatarId;
        } else {
            return R.drawable.room_empty_icon;
        }
    }

    private static Object createBotAvatarLink(long avatarId) {
        if (avatarId > 0) {
            return API_PATH + "file/download_file" +
                    "?fileId=" + avatarId;
        } else {
            return R.drawable.robot_empty_icon;
        }
    }
}