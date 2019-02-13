package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface RoomDao {
    @Insert
    void insert(Entities.Room... rooms);
    @Update
    void update(Entities.Room... rooms);
    @Delete
    void delete(Entities.Room... rooms);
    @Query("select * from room where roomId = :roomId")
    Entities.Room getRoomById(long roomId);
    @Query("select * from room where complexId = :complexId")
    List<Entities.Room> getComplexRooms(long complexId);
    @Query("select * from room")
    List<Entities.Room> getAllRooms();
    @Query("delete from room where complexId = :complexId")
    void deleteComplexRooms(long complexId);
    @Query("delete from room where roomId = :roomId")
    void deleteRoomById(long roomId);
    @Query("select * from room where roomId in (:roomIds)")
    List<Entities.Room> getRoomsByIds(List<Long> roomIds);
    @Query("select * from room where complexId in (:complexesIds)")
    List<Entities.Room> getRoomsByComplexesIds(List<Long> complexesIds);
    @Query("delete from room")
    void deleteAll();
}
