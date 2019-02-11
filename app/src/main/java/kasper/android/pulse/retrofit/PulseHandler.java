package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface PulseHandler {
    @POST("pulse/request_bot_view")
    Call<Packet> requestBotView(@Body Packet packet);
}
