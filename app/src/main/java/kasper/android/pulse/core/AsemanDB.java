package kasper.android.pulse.core;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.FileMessageSending;
import kasper.android.pulse.models.extras.TextMessageSending;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.repositories.BotCreationDao;
import kasper.android.pulse.repositories.BotDao;
import kasper.android.pulse.repositories.BotSecretDao;
import kasper.android.pulse.repositories.BotSubscriptionDao;
import kasper.android.pulse.repositories.ComplexDao;
import kasper.android.pulse.repositories.ComplexSecretDao;
import kasper.android.pulse.repositories.ContactDao;
import kasper.android.pulse.repositories.DownloadingDao;
import kasper.android.pulse.repositories.FileDao;
import kasper.android.pulse.repositories.FileLocalDao;
import kasper.android.pulse.repositories.FileUsageDao;
import kasper.android.pulse.repositories.InviteDao;
import kasper.android.pulse.repositories.KeyValueDao;
import kasper.android.pulse.repositories.MembershipDao;
import kasper.android.pulse.repositories.MessageDao;
import kasper.android.pulse.repositories.MessageLocalDao;
import kasper.android.pulse.repositories.MessageSendingDao;
import kasper.android.pulse.repositories.RoomDao;
import kasper.android.pulse.repositories.SessionDao;
import kasper.android.pulse.repositories.UploadingDao;
import kasper.android.pulse.repositories.UserDao;
import kasper.android.pulse.repositories.UserSecretDao;
import kasper.android.pulse.repositories.WorkershipDao;

@Database(entities = {Entities.Session.class, Entities.Bot.class, Entities.BotCreation.class
        , Entities.BotSubscription.class, Entities.BotSecret.class, Entities.User.class
        , Entities.UserSecret.class, Entities.Contact.class, Entities.Invite.class
        , Entities.File.class, Entities.FileUsage.class, Entities.Photo.class
        , Entities.Audio.class, Entities.Video.class, Entities.Message.class
        , Entities.TextMessage.class, Entities.PhotoMessage.class, Entities.AudioMessage.class
        , Entities.VideoMessage.class, Entities.ServiceMessage.class, Entities.Complex.class
        , Entities.ComplexSecret.class, Entities.Room.class, Entities.Membership.class
        , Entities.Workership.class, Entities.MessageLocal.class, Entities.FileLocal.class
        , Entities.IdKeeper.class, Uploading.class, Downloading.class, TextMessageSending.class
        , FileMessageSending.class}, version = 1, exportSchema = false)
public abstract class AsemanDB extends RoomDatabase {
    public abstract BotCreationDao getBotCreationDao();
    public abstract BotDao getBotDao();
    public abstract BotSecretDao getBotSecretDao();
    public abstract BotSubscriptionDao getBotSubscriptionDao();
    public abstract ComplexDao getComplexDao();
    public abstract ComplexSecretDao getComplexSecretDao();
    public abstract ContactDao getContactDao();
    public abstract FileDao getFileDao();
    public abstract FileLocalDao getFileLocalDao();
    public abstract FileUsageDao getFileUsageDao();
    public abstract InviteDao getInviteDao();
    public abstract MembershipDao getMembershipDao();
    public abstract MessageDao getMessageDao();
    public abstract MessageLocalDao getMessageLocalDao();
    public abstract RoomDao getRoomDao();
    public abstract SessionDao getSessionDao();
    public abstract UserDao getUserDao();
    public abstract UserSecretDao getUserSecretDao();
    public abstract WorkershipDao getWorkershipDao();
    public abstract KeyValueDao getKeyValueDao();
    public abstract UploadingDao getUploadingDao();
    public abstract DownloadingDao getDownloadingDao();
    public abstract MessageSendingDao getMessageSendingDao();

    public static void deleteAllData() {
        getInstance().getBotCreationDao().deleteAll();
        getInstance().getBotDao().deleteAll();
        getInstance().getBotSecretDao().deleteAll();
        getInstance().getBotSubscriptionDao().deleteAll();
        getInstance().getComplexDao().deleteAll();
        getInstance().getComplexSecretDao().deleteAll();
        getInstance().getContactDao().deleteAll();
        getInstance().getFileDao().deleteAll();
        getInstance().getFileLocalDao().deleteAll();
        getInstance().getFileUsageDao().deleteAll();
        getInstance().getInviteDao().deleteAll();
        getInstance().getMembershipDao().deleteAll();
        getInstance().getMessageDao().deleteAll();
        getInstance().getMessageLocalDao().deleteAll();
        getInstance().getRoomDao().deleteAll();
        getInstance().getSessionDao().deleteAll();
        getInstance().getUserDao().deleteAll();
        getInstance().getUserSecretDao().deleteAll();
        getInstance().getWorkershipDao().deleteAll();
        getInstance().getKeyValueDao().deleteAll();
        getInstance().getUploadingDao().deleteAll();
        getInstance().getDownloadingDao().deleteAll();
        getInstance().getMessageSendingDao().deleteAll();

        DatabaseHelper.setup();
    }

    private static AsemanDB db;
    public static AsemanDB getInstance() {
        if (db ==  null)
            db = Room.databaseBuilder(Core.getInstance(),
                    AsemanDB.class,
                    "AsemanDB")
                    .allowMainThreadQueries()
                    .build();
        return db;
    }
}
