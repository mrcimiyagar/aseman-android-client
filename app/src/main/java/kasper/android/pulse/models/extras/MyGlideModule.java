package kasper.android.pulse.models.extras;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;

import java.io.IOException;
import java.io.InputStream;

import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void applyOptions(@NonNull Context context, @NonNull GlideBuilder builder) {
        builder.setLogLevel(Log.VERBOSE);
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {

        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.addInterceptor(chain -> {
            Entities.Session session = DatabaseHelper.getSingleSession();
            Request request = chain.request();
            Request.Builder newRequest = request.newBuilder();
            if (session != null)
                newRequest.header("Authorization", session.getSessionId() + " " + session.getToken());
            return chain.proceed(newRequest.build());
        });

        HttpLoggingInterceptor loggerInterceptor = new HttpLoggingInterceptor();
        loggerInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        builder.addInterceptor(loggerInterceptor);

        OkHttpClient client = builder.build();

        OkHttpUrlLoader.Factory factory = new OkHttpUrlLoader.Factory(client);

        glide.getRegistry().replace(GlideUrl.class, InputStream.class, factory);
    }
}
