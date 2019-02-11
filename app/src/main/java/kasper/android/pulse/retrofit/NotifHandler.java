package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface NotifHandler {
    @POST("notif/notify_notif_received")
    Call<Packet> notifyNotifReceived(@Body Packet packet);
}