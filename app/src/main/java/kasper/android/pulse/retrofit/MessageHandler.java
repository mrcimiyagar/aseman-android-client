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
    @POST("message/notify_message_seen")
    Call<Packet> notifyMessageSeen(@Body Packet packet);
    @POST("message/get_messages")
    Call<Packet> getMessages(@Body Packet packet);
    @POST("message/get_last_actions")
    Call<Packet> getLastActions(@Body Packet packet);
}
