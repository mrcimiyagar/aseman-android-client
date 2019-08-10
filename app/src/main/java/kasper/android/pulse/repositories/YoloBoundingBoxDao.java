package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.YoloBoundingBox;

@Dao
public interface YoloBoundingBoxDao {
    @Insert
    void insert(YoloBoundingBox... box);
    @Update
    void update(YoloBoundingBox... box);
    @Delete
    void delete(YoloBoundingBox... box);
    @Query("select * from yoloboundingbox where imageId = :imageId")
    List<YoloBoundingBox> getImageYoloBoundingBoxes(long imageId);
    @Query("delete from yoloboundingbox")
    void deleteAll();
}
