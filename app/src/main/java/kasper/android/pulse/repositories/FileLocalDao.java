package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface FileLocalDao {
    @Insert
    void insert(Entities.FileLocal... fileLocals);
    @Update
    void update(Entities.FileLocal... fileLocals);
    @Delete
    void delete(Entities.FileLocal... fileLocals);
    @Query("select * from filelocal where fileId = :fileId")
    Entities.FileLocal getFileLocalById(long fileId);
    @Query("select * from filelocal where fileId in (:fileIds)")
    List<Entities.FileLocal> getFileLocalsByIds(List<Long> fileIds);
    @Query("delete from filelocal")
    void deleteAll();
}
