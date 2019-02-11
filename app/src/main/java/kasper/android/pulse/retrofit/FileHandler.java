package kasper.android.pulse.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileHandler {
    @Streaming
    @GET("file/download_file")
    Call<ResponseBody> downloadFile(@Query("fileId") long fileId);
}
