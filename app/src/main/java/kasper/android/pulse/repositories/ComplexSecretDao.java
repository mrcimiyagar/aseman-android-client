package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface ComplexSecretDao {
    @Insert
    void insert(Entities.ComplexSecret... complexSecrets);
    @Update
    void update(Entities.ComplexSecret... complexSecrets);
    @Delete
    void delete(Entities.ComplexSecret... complexSecrets);
    @Query("select * from complexsecret where complexId = :complexId")
    Entities.ComplexSecret getComplexSecretByComplexId(long complexId);
    @Query("select * from complexsecret where complexSecretId = :complexSecretId")
    Entities.ComplexSecret getComplexSecretById(long complexSecretId);
}
