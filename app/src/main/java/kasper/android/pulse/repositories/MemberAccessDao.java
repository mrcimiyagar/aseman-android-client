package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import kasper.android.pulse.models.entities.Entities;

@Dao
public interface MemberAccessDao {
    @Insert
    void insert(Entities.MemberAccess... memberAccesses);
    @Update
    void update(Entities.MemberAccess... memberAccesses);
    @Delete
    void delete(Entities.MemberAccess... memberAccesses);
    @Query("select * from memberaccess where memberAccessId = :memAccessId")
    Entities.MemberAccess getMemberAccessById(long memAccessId);
    @Query("select * from memberaccess where membershipId = :membershipId")
    Entities.MemberAccess getMemberAccessByMembershipId(long membershipId);
    @Query("delete from memberaccess")
    void deleteAll();
}
