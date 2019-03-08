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
    @Query("select * from membership where membershipId = :membershipId")
    Entities.Membership getMembershipById(long membershipId);
    @Query("select * from membership where userId = :userId and complexId = :complexId")
    Entities.Membership getMembershipByUserAndComplexId(long userId, long complexId);
    @Query("select * from membership where userId = :userId")
    List<Entities.Membership> getUserMemberships(long userId);
    @Query("delete from membership")
    void deleteAll();
}
