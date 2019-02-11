package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface BotCreationDao {
    @Insert
    void insert(Entities.BotCreation... botCreations);
    @Update
    void update(Entities.BotCreation... botCreations);
    @Delete
    void delete(Entities.BotCreation... botCreations);
}
