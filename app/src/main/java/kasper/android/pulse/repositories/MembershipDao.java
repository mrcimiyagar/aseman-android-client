package kasper.android.pulse.repositories;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface MembershipDao {
    @Insert
    void insert(Entities.Membership... memberships);
    @Update
    void update(Entities.Membership... memberships);
    @Delete
    void delete(Entities.Membership... memberships);
    @Query("delete from membership where complexId = :complexId ")
    void deleteComplexMemberships(long complexId);
    @Query("select * from membership where complexId = :complexId")
    List<Entities.Membership> getComplexMemberships(long complexId);
    @Query("delete from membership")
    void deleteAll();
}
