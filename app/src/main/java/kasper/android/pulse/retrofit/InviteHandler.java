package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface InviteHandler {
    @POST("invite/create_invite")
    Call<Packet> createInvite(@Body Packet packet);
    @POST("invite/cancel_invite")
    Call<Packet> cancelInvite(@Body Packet packet);
    @POST("invite/accept_invite")
    Call<Packet> acceptInvite(@Body Packet packet);
    @POST("invite/ignore_invite")
    Call<Packet> ignoreInvite(@Body Packet packet);
}
