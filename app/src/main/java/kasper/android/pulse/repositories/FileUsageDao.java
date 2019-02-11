package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface FileUsageDao {
    @Insert
    void insert(Entities.FileUsage... fileUsages);
    @Update
    void update(Entities.FileUsage... fileUsages);
    @Delete
    void delete(Entities.FileUsage... fileUsages);
    @Query("select * from fileusage where fileUsageId = :fuId")
    Entities.FileUsage getFileUsageById(long fuId);
}
