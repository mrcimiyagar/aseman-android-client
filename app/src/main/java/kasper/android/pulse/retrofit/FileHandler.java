package kasper.android.pulse.retrofit;

import java.util.Map;

import kasper.android.pulse.models.network.Packet;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

public interface FileHandler {
    @Streaming
    @Multipart
    @POST("file/upload_photo")
    Call<Packet> uploadPhoto(@PartMap() Map<String, RequestBody> partMap,
                             @Part MultipartBody.Part file);
    @Streaming
    @Multipart
    @POST("file/upload_audio")
    Call<Packet> uploadAudio(@PartMap() Map<String, RequestBody> partMap,
                             @Part MultipartBody.Part file);
    @Streaming
    @Multipart
    @POST("file/upload_video")
    Call<Packet> uploadVideo(@PartMap() Map<String, RequestBody> partMap,
                             @Part MultipartBody.Part file);
    @Streaming
    @GET("file/download_file")
    Call<ResponseBody> downloadFile(@Query("fileId") long fileId);
}
