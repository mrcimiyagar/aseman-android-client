package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface ContactDao {
    @Insert
    void insert(Entities.Contact... contacts);
    @Update
    void update(Entities.Contact... contacts);
    @Delete
    void delete(Entities.Contact... contacts);
    @Query("select * from contact")
    List<Entities.Contact> getContacts();
    @Query("select * from contact where peerId = :peerId")
    Entities.Contact getContactByPeerId(long peerId);
    @Query("select * from contact where complexId = :complexId")
    Entities.Contact getContactByComplexId(long complexId);
}
