package kasper.android.pulse.retrofit;

import java.util.Map;

import kasper.android.pulse.models.network.Packet;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface FileHandler {
    @Streaming
    @Multipart
    @POST("file/write_to_file")
    Call<Packet> writeToFile(@PartMap() Map<String, RequestBody> partMap, @Part MultipartBody.Part file);
    @POST("file/get_file_size")
    Call<Packet> getFileSize(@Body Packet packet);
    @Multipart
    @POST("file/upload_photo")
    Call<Packet> uploadPhoto(@PartMap() Map<String, RequestBody> partMap);
    @Multipart
    @POST("file/upload_audio")
    Call<Packet> uploadAudio(@PartMap() Map<String, RequestBody> partMap);
    @Multipart
    @POST("file/upload_video")
    Call<Packet> uploadVideo(@PartMap() Map<String, RequestBody> partMap);
    @Streaming
    @GET("file/download_file")
    Call<ResponseBody> downloadFile(@Query("fileId") long fileId, @Query("offset") long offset);
}
