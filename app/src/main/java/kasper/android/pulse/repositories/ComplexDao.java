package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface ComplexDao {
    @Insert
    void insert(Entities.Complex... complexes);
    @Update
    void update(Entities.Complex... complexes);
    @Delete
    void delete(Entities.Complex... complexes);
    @Query("select * from complex where complexId = :complexId")
    Entities.Complex getComplexById(long complexId);
    @Query("select * from complex")
    List<Entities.Complex> getComplexes();
    @Query("delete from complex where complexId = :complexId")
    void deleteComplexById(long complexId);
    @Query("select * from complex where complexId in (:complexIds)")
    List<Entities.Complex> getComplexesByIds(List<Long> complexIds);
    @Query("select * from complex where complexId in (select complexId from complexsecret) and mode = 3")
    List<Entities.Complex> getAdminedComplexes();
    @Query("delete from complex")
    void deleteAll();
}
