package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface SearchHandler {
    @POST("user/search_users")
    Call<Packet> searchUsers(@Body Packet packet);
    @POST("robot/search_bots")
    Call<Packet> searchBots(@Body Packet packet);
    @POST("complex/search_complexes")
    Call<Packet> searchComplexes(@Body Packet packet);
}
