package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface UserDao {
    @Insert
    void insert(Entities.User... users);
    @Update
    void update(Entities.User... users);
    @Delete
    void delete(Entities.User... users);
    @Query("select * from user where baseUserId = :userId")
    Entities.User getUserById(long userId);
    @Query("delete from user where baseUserId = :userId")
    void deleteUserById(long userId);
    @Query("select * from user")
    List<Entities.User> getUsers();
    @Query("select * from user where baseUserId in (:userIds)")
    List<Entities.User> getUsersByIds(List<Long> userIds);
    @Query("delete from user")
    void deleteAll();
}
