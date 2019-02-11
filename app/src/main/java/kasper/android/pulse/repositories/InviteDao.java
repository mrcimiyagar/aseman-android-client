package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface InviteDao {
    @Insert
    void insert(Entities.Invite... invites);
    @Update
    void update(Entities.Invite... invites);
    @Delete
    void delete(Entities.Invite... invites);
    @Query("delete from invite where complexId = :complexId")
    void deleteComplexInvites(long complexId);
    @Query("select * from invite")
    List<Entities.Invite> getInvites();
    @Query("select * from invite where complexId = :complexId")
    List<Entities.Invite> getComplexInvites(long complexId);
}
