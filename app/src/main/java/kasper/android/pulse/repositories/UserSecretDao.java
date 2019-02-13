package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface UserSecretDao {
    @Insert
    void insert(Entities.UserSecret... userSecrets);
    @Update
    void update(Entities.UserSecret... userSecrets);
    @Delete
    void delete(Entities.UserSecret... userSecrets);
    @Query("select * from usersecret where userId = :userId")
    Entities.UserSecret getUserSecretByUserId(long userId);
    @Query("delete from usersecret")
    void deleteAll();
}
