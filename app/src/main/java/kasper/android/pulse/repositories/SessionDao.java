package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface SessionDao {
    @Insert
    void insert(Entities.Session... sessions);
    @Update
    void update(Entities.Session... sessions);
    @Delete
    void delete(Entities.Session... sessions);
    @Query("select * from session where sessionId = :sessionId")
    Entities.Session getSessionById(long sessionId);
    @Query("select * from session where baseUserId = :baseUserId")
    Entities.Session getSessionByUserId(long baseUserId);
    @Query("delete from session")
    void deleteAll();
}
