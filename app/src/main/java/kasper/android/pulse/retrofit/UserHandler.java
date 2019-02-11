package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserHandler {
    @POST("user/get_user_by_id")
    Call<Packet> getUserById(@Body Packet packet);
    @POST("user/update_user_profile")
    Call<Packet> updateUserProfile(@Body Packet packet);
    @POST("user/get_me")
    Call<Packet> getMe();
    @POST("user/search_users")
    Call<Packet> searchUsers(@Body Packet packet);
}
