package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public abstract class RoomDao {
    @Insert
    public abstract void insert(Entities.Room... rooms);
    @Update
    public abstract void update(Entities.Room... rooms);
    @Delete
    public abstract void delete(Entities.Room... rooms);

    @Insert
    public abstract void insert(Entities.SingleRoom... rooms);
    @Update
    public abstract void update(Entities.SingleRoom... rooms);
    @Delete
    public abstract void delete(Entities.SingleRoom... rooms);

    @Query("select * from room where roomId = :roomId")
    abstract Entities.Room getNonSingleRoomById(long roomId);
    @Query("select * from singleroom where roomId = :roomId")
    abstract Entities.SingleRoom getSingleRoomById(long roomId);

    @Query("select * from room where complexId = :complexId")
    public abstract List<Entities.Room> getComplexNonSingleRooms(long complexId);
    @Query("select * from singleroom where complexId = :complexId")
    public abstract List<Entities.SingleRoom> getComplexSingleRooms(long complexId);

    @Query("select * from room")
    abstract List<Entities.Room> getAllNonSingleRooms();
    @Query("select * from singleroom")
    abstract List<Entities.SingleRoom> getAllSingleRooms();

    @Query("delete from room where complexId = :complexId")
    abstract void deleteComplexNonSingleRooms(long complexId);
    @Query("delete from singleroom where complexId = :complexId")
    abstract void deleteComplexSingleRooms(long complexId);

    @Query("delete from room where roomId = :roomId")
    abstract void deleteNonSingleRoomById(long roomId);
    @Query("delete from singleroom where roomId = :roomId")
    abstract void deleteSingleRoomById(long roomId);

    @Query("select * from room where roomId in (:roomIds)")
    abstract List<Entities.Room> getNonSingleRoomsByIds(List<Long> roomIds);
    @Query("select * from singleroom where roomId in (:roomIds)")
    abstract List<Entities.SingleRoom> getSingleRoomsByIds(List<Long> roomIds);

    @Query("select * from room where complexId in (:complexesIds)")
    abstract List<Entities.Room> getNonSingleRoomsByComplexesIds(List<Long> complexesIds);
    @Query("select * from singleroom where complexId in (:complexesIds)")
    abstract List<Entities.SingleRoom> getSingleRoomsByComplexesIds(List<Long> complexesIds);

    @Query("select * from singleroom where ((user1Id = :user1Id and user2Id = :user2Id) or (user1Id = :user2Id and user2Id = :user1Id))")
    public abstract Entities.SingleRoom getSingleRoomByParticipantsIds(long user1Id, long user2Id);

    @Query("select * from room where (select mode from complex where complex.complexId = room.complexId) = 2")
    public abstract List<Entities.Room> getAllContactRooms();

    @Transaction
    public Entities.BaseRoom getRoomById(long roomId) {
        Entities.SingleRoom singleRoom = getSingleRoomById(roomId);
        if (singleRoom != null) return singleRoom;
        return getNonSingleRoomById(roomId);
    }

    @Transaction
    public List<Entities.BaseRoom> getComplexRooms(long complexId) {
        List<Entities.BaseRoom> baseRooms = new ArrayList<>();
        baseRooms.addAll(getComplexSingleRooms(complexId));
        baseRooms.addAll(getComplexNonSingleRooms(complexId));
        return baseRooms;
    }

    @Transaction
    public List<Entities.BaseRoom> getAllRooms() {
        List<Entities.BaseRoom> baseRooms = new ArrayList<>();
        baseRooms.addAll(getAllSingleRooms());
        baseRooms.addAll(getAllNonSingleRooms());
        return baseRooms;
    }

    @Transaction
    public void deleteComplexRooms(long complexId) {
        deleteComplexSingleRooms(complexId);
        deleteComplexNonSingleRooms(complexId);
    }

    @Transaction
    public void deleteRoomById(long roomId) {
        deleteSingleRoomById(roomId);
        deleteNonSingleRoomById(roomId);
    }

    @Transaction
    public List<Entities.BaseRoom> getRoomsByIds(List<Long> roomIds) {
        List<Entities.BaseRoom> baseRooms = new ArrayList<>();
        baseRooms.addAll(getSingleRoomsByIds(roomIds));
        baseRooms.addAll(getNonSingleRoomsByIds(roomIds));
        return baseRooms;
    }

    @Transaction
    public List<Entities.BaseRoom> getRoomsByComplexesIds(List<Long> complexIds) {
        List<Entities.BaseRoom> baseRooms = new ArrayList<>();
        baseRooms.addAll(getSingleRoomsByComplexesIds(complexIds));
        baseRooms.addAll(getNonSingleRoomsByComplexesIds(complexIds));
        return baseRooms;
    }

    @Query("delete from room")
    abstract void deleteAllNonSingleRooms();
    @Query("delete from singleroom")
    abstract void deleteAllSingleRooms();
    @Transaction
    public void deleteAll() {
        deleteAllNonSingleRooms();
        deleteAllSingleRooms();
    }
}
