package kasper.android.pulse.repositories;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import kasper.android.pulse.models.extras.FileMessageSending;
import kasper.android.pulse.models.extras.TextMessageSending;

@Dao
public abstract class MessageSendingDao {

    @Insert
    public abstract long insert(TextMessageSending sending);
    @Update
    public abstract void update(TextMessageSending... sendings);
    @Delete
    public abstract void delete(TextMessageSending... sendings);

    @Insert
    public abstract long insert(FileMessageSending sending);
    @Update
    public abstract void update(FileMessageSending... sendings);
    @Delete
    public abstract void delete(FileMessageSending... sendings);

    @Query("select * from textmessagesending")
    public abstract List<TextMessageSending> getTextMessageSendings();
    @Query("delete from textmessagesending where sendingId = :sendingId")
    public abstract void deleteTextMessageSendingById(long sendingId);
    @Query("delete from textmessagesending")
    abstract void deleteTextMessageSendings();

    @Query("select * from filemessagesending")
    public abstract List<FileMessageSending> getFileMessageSendings();
    @Query("delete from filemessagesending where sendingId = :sendingId")
    public abstract void deleteFileMessageSendingById(long sendingId);
    @Query("delete from filemessagesending")
    abstract void deleteFileMessageSendings();

    @Transaction
    public void deleteAll() {
        deleteTextMessageSendings();
        deleteFileMessageSendings();
    }
}
