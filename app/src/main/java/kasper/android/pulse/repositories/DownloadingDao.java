package kasper.android.pulse.repositories;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.Uploading;

@Dao
public interface DownloadingDao {
    @Insert
    void insert(Downloading... downloadings);
    @Update
    void update(Downloading... downloadings);
    @Delete
    void delete(Downloading... downloadings);
    @Query("select * from downloading")
    List<Downloading> getDownloadings();
    @Query("delete from downloading where downloadingId = :downloadId")
    void deleteUploadingById(long downloadId);
    @Query("delete from downloading")
    void deleteAll();
}
