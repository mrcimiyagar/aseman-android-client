package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MessageHandler {
    @POST("message/create_text_message")
    Call<Packet> createTextMessage(@Body Packet packet);
    @POST("message/create_file_message")
    Call<Packet> createFileMessage(@Body Packet packet);
}
