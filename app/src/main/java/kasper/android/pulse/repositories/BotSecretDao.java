package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface BotSecretDao {
    @Insert
    void insert(Entities.BotSecret... botSecrets);
    @Update
    void update(Entities.BotSecret... botSecrets);
    @Delete
    void delete(Entities.BotSecret... botSecrets);
    @Query("select * from botsecret where botId = :botId")
    Entities.BotSecret getBotSecretById(long botId);
    @Query("select * from botsecret where botId in (:botIds)")
    List<Entities.BotSecret> getBotSecretsByBotIds(List<Long> botIds);
    @Query("select * from botsecret where botId = :botId")
    Entities.BotSecret getBotSecretByBotId(long botId);
}
