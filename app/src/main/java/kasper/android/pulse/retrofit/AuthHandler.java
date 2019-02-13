package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthHandler {
    @POST("auth/register")
    Call<Packet> register(@Body Packet packet);
    @POST("auth/verify")
    Call<Packet> verify(@Body Packet packet);
    @POST("auth/login")
    Call<Packet> login();
    @POST("auth/logout")
    Call<Packet> logout();
    @POST("auth/delete_account")
    Call<Packet> deleteAccount();
}
