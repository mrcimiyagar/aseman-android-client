package kasper.android.pulse.retrofit;

import kasper.android.pulse.models.network.Packet;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ComplexHandler {
    @POST("complex/create_complex")
    Call<Packet> createComplex(@Body Packet packet);
    @POST("complex/get_complex_by_id")
    Call<Packet> getComplexById(@Body Packet packet);
    @POST("complex/get_complexes")
    Call<Packet> getComplexes();
    @POST("complex/delete_complex")
    Call<Packet> deleteComplex(@Body Packet packet);
    @POST("complex/update_complex_profile")
    Call<Packet> updateComplexProfile(@Body Packet packet);
    @POST("complex/search_complexes")
    Call<Packet> searchComplexes(@Body Packet packet);
    @POST("complex/update_member_access")
    Call<Packet> updateMemberAccess(@Body Packet packet);
    @POST("complex/get_complex_accesses")
    Call<Packet> getComplexAccesses(@Body Packet packet);
}
