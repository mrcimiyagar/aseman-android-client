package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface BotDao {
    @Insert
    void insert(Entities.Bot... bots);
    @Update
    void update(Entities.Bot... bots);
    @Delete
    void delete(Entities.Bot... bots);
    @Query("select * from bot where baseUserId = :botId")
    Entities.Bot getBotById(long botId);
    @Query("select * from bot where baseUserId in (select botId from botsecret where token != '')")
    List<Entities.Bot> getCreatedBots();
    @Query("select * from bot where baseUserId in (select botId from botsubscription)")
    List<Entities.Bot> getSubscribedBots();
    @Query("delete from bot")
    void deleteAll();
}
