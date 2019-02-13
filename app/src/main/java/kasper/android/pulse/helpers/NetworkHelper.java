package kasper.android.pulse.helpers;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
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
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

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
        loggerInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggerInterceptor);
        builder.connectTimeout(60, TimeUnit.SECONDS);
        builder.writeTimeout(60, TimeUnit.SECONDS);
        builder.readTimeout(60, TimeUnit.SECONDS);
        OkHttpClient client = builder.build();
        mapper = new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        retrofit = new Retrofit.Builder()
                .baseUrl(API_PATH)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();
    }

    public static void uploadFile(Entities.File file, long complexId, long roomId, String path, ProgressListener progressListener, OnFileUploadListener uploadListener) {
        Uploading uploading = new Uploading();
        uploading.setFile(file);
        uploading.setPath(path);
        uploading.setComplexId(complexId);
        uploading.setRoomId(roomId);
        uploading.setProgressListener(progressListener);
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
        call.enqueue(new Callback<Packet>() {
            @Override
            public void onResponse(@NonNull Call<Packet> call, @NonNull Response<Packet> response) {
                try {
                    if (response.body() != null && response.body().getStatus().equals("success"))
                        callback.onRequestSuccess(response.body());
                    else
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
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .priority(Priority.HIGH);
        Glide.with(Core.getInstance())
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