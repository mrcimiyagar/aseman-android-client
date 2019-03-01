package kasper.android.pulse.repositories;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import kasper.android.pulse.models.extras.Uploading;

@Dao
public interface UploadingDao {
    @Insert
    long insert(Uploading uploading);
    @Update
    void update(Uploading... uploadings);
    @Delete
    void delete(Uploading... uploadings);
    @Query("select * from uploading where uploadingId = :uploadingId")
    Uploading getUploadingById(long uploadingId);
    @Query("select * from uploading")
    List<Uploading> getUploadings();
    @Query("delete from uploading where uploadingId = :uploadingId")
    void deleteUploadingById(long uploadingId);
    @Query("delete from uploading")
    void deleteAll();
}
