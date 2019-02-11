package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import kasper.android.pulse.models.entities.Entities;

@Dao
public interface BotSubscriptionDao {
    @Insert
    void insert(Entities.BotSubscription... botSubscriptions);
    @Update
    void update(Entities.BotSubscription... botSubscriptions);
    @Delete
    void delete(Entities.BotSubscription... botSubscriptions);
    @Query("select * from botsubscription where botId = :botId")
    Entities.BotSubscription getBotSubscriptionByBotId(long botId);
}
