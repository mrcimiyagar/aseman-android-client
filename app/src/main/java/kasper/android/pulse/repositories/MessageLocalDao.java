package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface MessageLocalDao {
    @Insert
    void insert(Entities.MessageLocal... messageLocals);
    @Update
    void update(Entities.MessageLocal... messageLocals);
    @Delete
    void delete(Entities.MessageLocal... messageLocals);
    @Query("select * from messagelocal where messageId = :messageId")
    Entities.MessageLocal getMessageLocalById(long messageId);
    @Query("select * from messagelocal where messageId in (:messageIds)")
    List<Entities.MessageLocal> getMessageLocalsByIds(List<Long> messageIds);
    @Query("delete from messagelocal where messageId = :messageId")
    void deleteMessageById(long messageId);
    @Query("delete from messagelocal")
    void deleteAll();
}
