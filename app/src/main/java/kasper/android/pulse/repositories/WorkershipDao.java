package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface WorkershipDao {
    @Insert
    void insert(Entities.Workership... workerships);
    @Update
    void update(Entities.Workership... workerships);
    @Delete
    void delete(Entities.Workership... workerships);
}
