package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ContactHandler {
    @POST("contact/get_contacts")
    Call<Packet> getContacts();
    @POST("contact/create_contact")
    Call<Packet> createContact(@Body Packet packet);
}
