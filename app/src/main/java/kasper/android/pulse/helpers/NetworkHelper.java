package kasper.android.pulse.helpers;

import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.EOFException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.services.FilesService;
import okhttp3.CipherSuite;
import okhttp3.Connection;
import okhttp3.ConnectionSpec;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static java.net.HttpURLConnection.HTTP_OK;

/**
 * Created by keyhan1376 on 1/28/2018.
 */

public class NetworkHelper {

    //public static String SERVER_IP = "http://192.168.43.161:8080/";
    public static String SERVER_IP = "http://164.215.133.201:8080/";
    private static String API_PATH = SERVER_IP + "api/";
    private static Retrofit retrofit;
    public static Retrofit getRetrofit() {
        return retrofit;
    }
    private static ObjectMapper mapper;
    public static ObjectMapper getMapper() {
        return mapper;
    }

    private static final Charset UTF8 = Charset.forName("UTF-8");

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
        builder.addInterceptor(chain -> {

            Request request = chain.request();

            String log = "";

            RequestBody requestBody = request.body();
            boolean hasRequestBody = requestBody != null;

            Connection connection = chain.connection();
            String requestStartMessage = "--> "
                    + request.method()
                    + ' ' + request.url()
                    + (connection != null ? " " + connection.protocol() : "");
            log += "\n" + requestStartMessage;

            {
                if (hasRequestBody) {
                    if (requestBody.contentType() != null) {
                        log += "\n" + "Content-Type: " + requestBody.contentType();
                    }
                    if (requestBody.contentLength() != -1) {
                        log += "\n" + "Content-Length: " + requestBody.contentLength();
                    }
                }

                Headers headers = request.headers();
                for (int i = 0, count = headers.size(); i < count; i++) {
                    String name = headers.name(i);
                    if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                        log = logHeader(log, headers, i);
                    }
                }

                if (!hasRequestBody) {
                    log += "\n" + ("--> END " + request.method());
                } else if (bodyHasUnknownEncoding(request.headers())) {
                    log += "\n" + ("--> END " + request.method() + " (encoded body omitted)");
                } else {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);

                    Charset charset = UTF8;
                    MediaType contentType = requestBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }
                    if (isPlaintext(buffer)) {
                        if (charset != null) {
                            log += "\n" + (buffer.readString(charset));
                        }
                        log += "\n" + ("--> END " + request.method()
                                + " (" + requestBody.contentLength() + "-byte body)");
                    } else {
                        log += "\n" + ("--> END " + request.method() + " (binary "
                                + requestBody.contentLength() + "-byte body omitted)");
                    }
                }
            }

            LogHelper.log("OkHttp", log);
            log = "";

            long startNs = System.nanoTime();
            Response response;
            try {
                response = chain.proceed(request);
            } catch (Exception e) {
                log += "\n" + ("<-- HTTP FAILED: " + e);
                LogHelper.log("OkHttp", log);
                throw e;
            }
            long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

            ResponseBody responseBody = response.body();
            long contentLength = 0;
            if (responseBody != null) {
                contentLength = responseBody.contentLength();
            }
            String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
            log += "\n" + ("<-- "
                    + response.code()
                    + (response.message().isEmpty() ? "" : ' ' + response.message())
                    + ' ' + response.request().url()
                    + " (" + tookMs + "ms" + bodySize + ')');

            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                log = logHeader(log, headers, i);
            }

            if (!HttpHeaders.hasBody(response)) {
                log += "\n" + ("<-- END HTTP");
            } else if (bodyHasUnknownEncoding(response.headers())) {
                log += "\n" + ("<-- END HTTP (encoded body omitted)");
            } else {
                BufferedSource source;
                if (responseBody != null) {
                    source = responseBody.source();
                    source.request(Long.MAX_VALUE); // Buffer the entire body.
                    Buffer buffer = source.buffer();

                    Long gzippedLength = null;
                    if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                        gzippedLength = buffer.size();
                        try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                            buffer = new Buffer();
                            buffer.writeAll(gzippedResponseBody);
                        }
                    }

                    Charset charset = UTF8;
                    MediaType contentType = responseBody.contentType();
                    if (contentType != null) {
                        charset = contentType.charset(UTF8);
                    }

                    if (!isPlaintext(buffer)) {
                        log += "\n" + ("<-- END HTTP (binary " + buffer.size() + "-byte body omitted)");
                        LogHelper.log("OkHttp", log);
                        return response;
                    }

                    if (contentLength != 0) {
                        if (charset != null) {
                            log += "\n" + (buffer.clone().readString(charset));
                        }
                    }

                    if (gzippedLength != null) {
                        log += "\n" + ("<-- END HTTP (" + buffer.size() + "-byte, "
                                + gzippedLength + "-gzipped-byte body)");
                    } else {
                        log += "\n" + ("<-- END HTTP (" + buffer.size() + "-byte body)");
                    }
                }
            }

            LogHelper.log("OkHttp", log);

            return response;
        });
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

        OkHttpClient okHttpClient = builder.connectionSpecs(Arrays.asList(
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

    private static String logHeader(String log, Headers headers, int i) {
        String value = headers.value(i);
        log += "\n" + (headers.name(i) + ": " + value);
        return log;
    }

    private static boolean isPlaintext(Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            return false; // Truncated UTF-8 sequence.
        }
    }

    private static boolean bodyHasUnknownEncoding(Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null
                && !contentEncoding.equalsIgnoreCase("identity")
                && !contentEncoding.equalsIgnoreCase("gzip");
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
            public void onResponse(@NonNull Call<Packet> call, @NonNull retrofit2.Response<Packet> response) {
                try {
                    if (response.code() == HTTP_OK && response.body() != null && response.body().getStatus().equals("success")) {
                         callback.onRequestSuccess(response.body());
                    } else {
                        callback.onServerFailure();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Packet> call, @NonNull Throwable t) {
                try {
                    LogHelper.log("AsemanError", t.toString());
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