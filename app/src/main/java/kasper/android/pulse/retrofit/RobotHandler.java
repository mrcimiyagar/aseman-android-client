package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface RobotHandler {
    @POST("robot/get_workerships")
    Call<Packet> getWorkerships(@Body Packet packet);
    @POST("robot/get_robot")
    Call<Packet> getRobot(@Body Packet packet);
    @POST("robot/call_robot_server")
    Call<Packet> callBotServer(@Body Packet packet);
    @POST("robot/get_subscribed_bots")
    Call<Packet> getSubscribedBots();
    @POST("robot/subscribe_bot")
    Call<Packet> subscribeBot(@Body Packet packet);
    @POST("robot/get_bot_store_content")
    Call<Packet> getBotStoreContent();
    @POST("robot/add_bot_to_room")
    Call<Packet> addBotToRoom(@Body Packet packet);
    @POST("robot/remove_bot_from_room")
    Call<Packet> removeBotFromRoom(@Body Packet packet);
    @POST("robot/create_bot")
    Call<Packet> createBot(@Body Packet packet);
    @POST("robot/update_workership")
    Call<Packet> updateWorkership(@Body Packet packet);
    @POST("robot/get_created_bots")
    Call<Packet> getCreatedBots();
    @POST("robot/get_bots")
    Call<Packet> getBots();
    @POST("robot/update_bot_profile")
    Call<Packet> updateBotProfile(@Body Packet packet);
    @POST("robot/search_bots")
    Call<Packet> searchBots(@Body Packet packet);
}
