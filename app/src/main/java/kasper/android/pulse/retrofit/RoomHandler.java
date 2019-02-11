package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RoomHandler {
    @POST("room/create_room")
    Call<Packet> createRoom(@Body Packet packet);
    @POST("room/get_room_by_id")
    Call<Packet> getRoomById(@Body Packet packet);
    @POST("room/get_rooms")
    Call<Packet> getRooms(@Body Packet packet);
    @POST("room/update_room_profile")
    Call<Packet> updateRoomProfile(@Body Packet packet);
    @POST("room/delete_room")
    Call<Packet> deleteRoom(@Body Packet packet);
}
