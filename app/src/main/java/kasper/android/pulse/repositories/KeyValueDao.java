package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface KeyValueDao {
    @Insert
    void insert(Entities.IdKeeper idKeeper);
    @Update
    void update(Entities.IdKeeper idKeeper);
    @Query("select * from idkeeper limit 1")
    Entities.IdKeeper getIdKeeper();
}
