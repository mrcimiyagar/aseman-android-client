package kasper.android.pulse.helpers;

import android.os.Environment;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import kasper.android.pulse.core.AsemanDB;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.repositories.BotDao;
import kasper.android.pulse.repositories.BotSecretDao;
import kasper.android.pulse.repositories.ComplexDao;
import kasper.android.pulse.repositories.ComplexSecretDao;
import kasper.android.pulse.repositories.KeyValueDao;
import kasper.android.pulse.repositories.MessageDao;
import kasper.android.pulse.repositories.RoomDao;
import kasper.android.pulse.repositories.UserDao;
import kasper.android.pulse.repositories.UserSecretDao;

/**
 * Created by keyhan1376 on 1/29/2018
 */

public class DatabaseHelper {

    public static final String StorageDir = "Pulse";

    public static void setup() {
        java.io.File storageDir = new java.io.File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir);
        if (!storageDir.exists()) {
            if (storageDir.mkdirs()) {
                Log.d("Aseman", "Storage Dir initialized or checked successfully");
            }
        }
        KeyValueDao keyValueDao = AsemanDB.getInstance().getKeyValueDao();
        if (keyValueDao.getIdKeeper() == null) {
            Entities.IdKeeper idKeeper = new Entities.IdKeeper();
            idKeeper.setIdKeeperId(1);
            idKeeper.setLocalId(1);
            idKeeper.setSessionId(0);
            keyValueDao.insert(idKeeper);
        }
    }

    public static void createSession(Entities.Session session, boolean isMe) {
        AsemanDB.getInstance().getSessionDao().insert(session);
        if (isMe) {
            Entities.IdKeeper idKeeper = AsemanDB.getInstance().getKeyValueDao().getIdKeeper();
            idKeeper.setSessionId(session.getSessionId());
            AsemanDB.getInstance().getKeyValueDao().update(idKeeper);
        }
    }

    public static void updateSession(Entities.Session session) {
        AsemanDB.getInstance().getSessionDao().update(session);
    }

    public static Entities.Session getSingleSession() {
        long sessionId = AsemanDB.getInstance().getKeyValueDao().getIdKeeper().getSessionId();
        if (sessionId > 0) {
            Entities.Session session = AsemanDB.getInstance().getSessionDao().getSessionById(sessionId);
            session.setBaseUser(AsemanDB.getInstance().getUserDao().getUserById(session.getBaseUserId()));
            return session;
        } else {
            return null;
        }
    }

    public static Entities.User getMe() {
        Entities.Session session = getSingleSession();
        if (session == null) return null;
        Entities.User user = AsemanDB.getInstance().getUserDao().getUserById(session.getBaseUserId());
        Entities.UserSecret userSecret = AsemanDB.getInstance().getUserSecretDao().getUserSecretByUserId(user.getBaseUserId());
        userSecret.setHome(getComplexById(userSecret.getHomeId()));
        user.setUserSecret(userSecret);
        return user;
    }

    public static void updateMe(Entities.User user) {
        AsemanDB.getInstance().getUserDao().update(user);
    }

    public static boolean isComplexInDatabase(long complexId) {
        return AsemanDB.getInstance().getComplexDao().getComplexById(complexId) != null;
    }

    public static boolean isRoomInDatabase(long roomId) {
        return AsemanDB.getInstance().getRoomDao().getRoomById(roomId) != null;
    }

    public static boolean isContactInDatabase(long peerId) {
        return AsemanDB.getInstance().getContactDao().getContactByPeerId(peerId) != null;
    }

    public static boolean notifyComplexCreated(Entities.Complex complex) {
        ComplexDao complexDao = AsemanDB.getInstance().getComplexDao();
        if (complexDao.getComplexById(complex.getComplexId()) != null) {
            complexDao.update(complex);
            return false;
        } else {
            complexDao.insert(complex);
            return true;
        }
    }

    public static void notifyComplexSecretCreated(Entities.ComplexSecret complexSecret) {
        ComplexSecretDao dao = AsemanDB.getInstance().getComplexSecretDao();
        if (dao.getComplexSecretById(complexSecret.getComplexSecretId()) != null)
            dao.update(complexSecret);
        else
            dao.insert(complexSecret);
    }

    public static void notifyComplexRemoved(long complexId) {
        AsemanDB.getInstance().getComplexDao().deleteComplexById(complexId);
        AsemanDB.getInstance().getRoomDao().deleteComplexRooms(complexId);
        AsemanDB.getInstance().getMembershipDao().deleteComplexMemberships(complexId);
        AsemanDB.getInstance().getInviteDao().deleteComplexInvites(complexId);
    }

    public static boolean notifyRoomCreated(Entities.Room room) {
        RoomDao roomDao = AsemanDB.getInstance().getRoomDao();
        if (roomDao.getRoomById(room.getRoomId()) != null) {
            roomDao.update(room);
            return false;
        } else {
            roomDao.insert(room);
            return true;
        }
    }

    public static void notifyRoomRemoved(long roomId) {
        AsemanDB.getInstance().getRoomDao().deleteRoomById(roomId);
    }

    public static List<Entities.Complex> getComplexes() {
        return AsemanDB.getInstance().getComplexDao().getComplexes();
    }

    public static List<Entities.Room> getRooms(long complexId) {
        Entities.Complex complex = AsemanDB.getInstance().getComplexDao().getComplexById(complexId);
        List<Entities.Room> rooms = AsemanDB.getInstance().getRoomDao().getComplexRooms(complexId);
        for (Entities.Room room : rooms) {
            room.setComplex(complex);
            room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(room.getRoomId()));
            if (room.getLastAction() != null)
                room.getLastAction().setAuthor(AsemanDB.getInstance().getUserDao()
                        .getUserById(room.getLastAction().getAuthorId()));
        }
        return rooms;
    }

    public static long getMembersCount(long complexId) {
        return AsemanDB.getInstance().getMembershipDao().getComplexMemberships(complexId).size();
    }

    public static List<Entities.Membership> getMemberships(long complexId) {
        Entities.Complex complex = getComplexById(complexId);
        List<Entities.Membership> memberships = AsemanDB.getInstance().getMembershipDao().getComplexMemberships(complexId);
        for (Entities.Membership membership : memberships) {
            membership.setComplex(complex);
            Entities.BaseUser baseUser = getBaseUserById(membership.getUserId());
            if (baseUser != null)
                membership.setUser((Entities.User) baseUser);
        }
        return memberships;
    }

    public static List<Entities.Room> getAllRooms() {
        List<Entities.Room> rooms = AsemanDB.getInstance().getRoomDao().getAllRooms();
        for (Entities.Room room : rooms) {
            room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(room.getRoomId()));
            if (room.getLastAction() != null)
                room.getLastAction().setAuthor(AsemanDB.getInstance().getUserDao()
                        .getUserById(room.getLastAction().getAuthorId()));
            room.setComplex(getComplexById(room.getComplexId()));
        }
        Collections.sort(rooms, (room1, room2) -> {
            if (room1.getLastAction() == null && room2.getLastAction() != null) return -1;
            else if (room2.getLastAction() == null && room1.getLastAction() != null) return 1;
            else if (room1.getLastAction() == null && room2.getLastAction() == null) return 0;
            else {
                long diff = room1.getLastAction().getTime() - room2.getLastAction().getTime();
                return diff > 0 ? 1 : diff < 0 ? -1 : 0;
            }
        });
        Collections.reverse(rooms);
        return rooms;
    }

    public static Entities.Complex getComplexById(long complexId) {
        Entities.Complex complex = AsemanDB.getInstance().getComplexDao().getComplexById(complexId);
        complex.setComplexSecret(AsemanDB.getInstance().getComplexSecretDao().getComplexSecretByComplexId(complexId));
        complex.setRooms(AsemanDB.getInstance().getRoomDao().getComplexRooms(complexId));
        return complex;
    }

    public static Entities.Room getRoomById(long roomId) {
        Entities.Room room = AsemanDB.getInstance().getRoomDao().getRoomById(roomId);
        room.setComplex(AsemanDB.getInstance().getComplexDao().getComplexById(room.getComplexId()));
        room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(roomId));
        if (room.getLastAction() != null)
            room.getLastAction().setAuthor(AsemanDB.getInstance().getUserDao()
                    .getUserById(room.getLastAction().getAuthorId()));
        return room;
    }

    public static void updateComplex(Entities.Complex mComplex) {
        AsemanDB.getInstance().getComplexDao().update(mComplex);
    }

    public static void updateRoom(Entities.Room mRoom) {
        AsemanDB.getInstance().getRoomDao().update(mRoom);
    }

    public static void notifyUserCreated(Entities.User user) {
        UserDao userDao = AsemanDB.getInstance().getUserDao();
        if (userDao.getUserById(user.getBaseUserId()) != null)
            userDao.update(user);
        else
            userDao.insert(user);
    }

    public static void notifyUserSecretCreated(Entities.UserSecret userSecret) {
        UserSecretDao secretDao = AsemanDB.getInstance().getUserSecretDao();
        if (secretDao.getUserSecretByUserId(userSecret.getUserId()) == null)
            secretDao.insert(userSecret);
        else
            secretDao.update(userSecret);
    }

    public static void deleteHuman(long humanId) {
        AsemanDB.getInstance().getUserDao().deleteUserById(humanId);
    }

    public static List<Entities.User> getHumans() {
        Entities.Session session = getSingleSession();
        Entities.UserSecret userSecret = AsemanDB.getInstance().getUserSecretDao().getUserSecretByUserId(session.getBaseUserId());
        List<Entities.User> users = AsemanDB.getInstance().getUserDao().getUsers();
        for (Entities.User u : users) {
            if (u.getBaseUserId() == session.getBaseUserId()) {
                u.setUserSecret(userSecret);
            }
        }
        return users;
    }

    public static Entities.BaseUser getBaseUserById(long userId) {
        Entities.User user = AsemanDB.getInstance().getUserDao().getUserById(userId);
        if (user != null)
            return user;
        Entities.Bot bot = AsemanDB.getInstance().getBotDao().getBotById(userId);
        if (bot != null)
            return bot;
        return null;
    }

    public static Entities.User getHumanById(long humanId) {
        Entities.User user = AsemanDB.getInstance().getUserDao().getUserById(humanId);
        if (user != null)
            user.setUserSecret(AsemanDB.getInstance().getUserSecretDao().getUserSecretByUserId(user.getBaseUserId()));
        return user;
    }

    public static boolean isHumanInDatabase(long humanId) {
        return AsemanDB.getInstance().getUserDao().getUserById(humanId) != null;
    }

    public static void updateHuman(Entities.User mUser) {
        AsemanDB.getInstance().getUserDao().update(mUser);
    }

    public static void notifyBotOwned(long botId, String token) {
        BotSecretDao botSecretDao = AsemanDB.getInstance().getBotSecretDao();
        Entities.BotSecret botSecret = botSecretDao.getBotSecretById(botId);
        botSecret.setToken(token);
        botSecretDao.update(botSecret);
    }

    public static List<Entities.Bot> getCreatedBots() {
        List<Entities.Bot> bots = AsemanDB.getInstance().getBotDao().getCreatedBots();
        List<Long> botIds = new ArrayList<>();
        for (Entities.Bot b : bots) {
            botIds.add(b.getBaseUserId());
        }
        List<Entities.BotSecret> botSecrets = AsemanDB.getInstance().getBotSecretDao().getBotSecretsByBotIds(botIds);
        for (int counter = 0; counter < bots.size(); counter++) {
            bots.get(counter).setBotSecret(botSecrets.get(counter));
        }
        for (Entities.Bot bot : bots) {
            List<Entities.Session> sessions = new ArrayList<>();
            sessions.add(AsemanDB.getInstance().getSessionDao().getSessionByUserId(bot.getBaseUserId()));
            bot.setSessions(sessions);
        }
        return bots;
    }

    public static void notifyBotUpdated(Entities.Bot bot) {
        AsemanDB.getInstance().getBotDao().update(bot);
    }

    public static void notifyBotCreated(Entities.Bot bot, Entities.BotCreation creation) {
        BotDao botDao = AsemanDB.getInstance().getBotDao();
        if (botDao.getBotById(bot.getBaseUserId()) == null) {
            botDao.insert(bot);
            if (bot.getBotSecret() != null)
                AsemanDB.getInstance().getBotSecretDao().insert(bot.getBotSecret());
            if (creation != null)
                AsemanDB.getInstance().getBotCreationDao().insert(creation);
        } else {
            botDao.update(bot);
            if (bot.getBotSecret() != null)
                AsemanDB.getInstance().getBotSecretDao().update(bot.getBotSecret());
            if (creation != null)
                AsemanDB.getInstance().getBotCreationDao().update(creation);
        }
    }

    public static void notifyBotSubscribed(Entities.Bot bot, Entities.BotSubscription botSubscription) {
        notifyBotCreated(bot, null);
        if (AsemanDB.getInstance().getBotSubscriptionDao().getBotSubscriptionByBotId(bot.getBaseUserId()) == null) {
            AsemanDB.getInstance().getBotSubscriptionDao().insert(botSubscription);
        }
    }

    public static List<Entities.Bot> getSubscribedBots() {
        List<Entities.Bot> bots = AsemanDB.getInstance().getBotDao().getSubscribedBots();
        List<Long> botIds = new ArrayList<>();
        for (Entities.Bot b : bots) {
            botIds.add(b.getBaseUserId());
        }
        List<Entities.BotSecret> botSecrets = AsemanDB.getInstance().getBotSecretDao().getBotSecretsByBotIds(botIds);
        Hashtable<Long, Entities.BotSecret> botSecretTable = new Hashtable<>();
        for (Entities.BotSecret botSecret : botSecrets) {
            botSecretTable.put(botSecret.getBotId(), botSecret);
        }
        for (Entities.Bot bot : bots) {
            bot.setBotSecret(botSecretTable.get(bot.getBaseUserId()));
            List<Entities.Session> sessions = new ArrayList<>();
            sessions.add(AsemanDB.getInstance().getSessionDao().getSessionByUserId(bot.getBaseUserId()));
            bot.setSessions(sessions);
        }
        return bots;
    }

    public static Entities.Bot getBotById(long botId) {
        Entities.Bot bot = AsemanDB.getInstance().getBotDao().getBotById(botId);
        Entities.BotSecret botSecret = AsemanDB.getInstance().getBotSecretDao()
                .getBotSecretByBotId(botId);
        if (botSecret != null) {
            bot.setBotSecret(botSecret);
            bot.setSessions(Collections.singletonList(AsemanDB.getInstance()
                    .getSessionDao().getSessionByUserId(botId)));
        }
        return bot;
    }

    public static boolean isBotOwned(long botId) {
        return AsemanDB.getInstance().getBotSecretDao().getBotSecretByBotId(botId) != null;
    }

    public static boolean isBotSubscribed(long botId) {
        return AsemanDB.getInstance().getBotSubscriptionDao().getBotSubscriptionByBotId(botId) != null;
    }

    public static Pair<Entities.Message, Entities.MessageLocal> notifyTextMessageSending(long roomId, String text) {
        Entities.Session session = getSingleSession();
        Entities.TextMessage message = new Entities.TextMessage();
        message.setMessageId(generateLocalId());
        message.setText(text);
        message.setTime(System.currentTimeMillis());
        message.setAuthorId(session.getBaseUserId());
        message.setAuthor(AsemanDB.getInstance().getUserDao().getUserById(session.getBaseUserId()));
        message.setRoomId(roomId);
        message.setRoom(getRoomById(roomId));
        AsemanDB.getInstance().getMessageDao().insert(message);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(message.getMessageId());
        messageLocal.setSent(false);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
        return new Pair<>(message, messageLocal);
    }

    public static Pair<Entities.Message, Entities.MessageLocal> notifyPhotoMessageSending(long roomId, long fileId) {
        Entities.Session session = getSingleSession();
        Entities.PhotoMessage message = new Entities.PhotoMessage();
        message.setMessageId(generateLocalId());
        message.setPhotoId(fileId);
        message.setPhoto(AsemanDB.getInstance().getFileDao().getPhotoById(fileId));
        message.setTime(System.currentTimeMillis());
        message.setAuthorId(session.getBaseUserId());
        message.setAuthor(AsemanDB.getInstance().getUserDao().getUserById(session.getBaseUserId()));
        message.setRoomId(roomId);
        message.setRoom(getRoomById(roomId));
        AsemanDB.getInstance().getMessageDao().insert(message);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(message.getMessageId());
        messageLocal.setSent(false);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
        return new Pair<>(message, messageLocal);
    }

    public static Pair<Entities.Message, Entities.MessageLocal> notifyAudioMessageSending(long roomId, long fileId) {
        Entities.Session session = getSingleSession();
        Entities.AudioMessage message = new Entities.AudioMessage();
        message.setMessageId(generateLocalId());
        message.setAudioId(fileId);
        message.setAudio(AsemanDB.getInstance().getFileDao().getAudioById(fileId));
        message.setTime(System.currentTimeMillis());
        message.setAuthorId(session.getBaseUserId());
        message.setAuthor(AsemanDB.getInstance().getUserDao().getUserById(session.getBaseUserId()));
        message.setRoomId(roomId);
        message.setRoom(getRoomById(roomId));
        AsemanDB.getInstance().getMessageDao().insert(message);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(message.getMessageId());
        messageLocal.setSent(false);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
        return new Pair<>(message, messageLocal);
    }

    public static Pair<Entities.Message, Entities.MessageLocal> notifyVideoMessageSending(long roomId, long fileId) {
        Entities.Session session = getSingleSession();
        Entities.VideoMessage message = new Entities.VideoMessage();
        message.setMessageId(generateLocalId());
        message.setVideoId(fileId);
        message.setVideo(AsemanDB.getInstance().getFileDao().getVideoById(fileId));
        message.setTime(System.currentTimeMillis());
        message.setAuthorId(session.getBaseUserId());
        message.setAuthor(AsemanDB.getInstance().getUserDao().getUserById(session.getBaseUserId()));
        message.setRoomId(roomId);
        message.setRoom(getRoomById(roomId));
        AsemanDB.getInstance().getMessageDao().insert(message);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(message.getMessageId());
        messageLocal.setSent(false);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
        return new Pair<>(message, messageLocal);
    }

    public static Pair<Entities.Message, Entities.MessageLocal> notifyTextMessageSent(long localMessageId, long messageId, long time) {
        MessageDao messageDao = AsemanDB.getInstance().getMessageDao();
        Entities.TextMessage msg = messageDao.getTextMessageById(localMessageId);
        Log.d("AsemanLogger", localMessageId + " " + messageId + " " + time);
        messageDao.delete(msg);
        Entities.TextMessage textMessage = new Entities.TextMessage();
        textMessage.setMessageId(messageId);
        textMessage.setRoomId(msg.getRoomId());
        textMessage.setAuthorId(msg.getAuthorId());
        textMessage.setTime(time);
        textMessage.setText(msg.getText());
        messageDao.insert(textMessage);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(localMessageId);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(messageId);
        messageLocal.setSent(true);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
        return new Pair<>(msg, messageLocal);
    }

    public static void notifyPhotoMessageSent(long localMessageId, long messageId, long time) {
        MessageDao messageDao = AsemanDB.getInstance().getMessageDao();
        Entities.PhotoMessage msg = messageDao.getPhotoMessageById(localMessageId);
        messageDao.delete(msg);
        msg.setMessageId(messageId);
        msg.setTime(time);
        messageDao.insert(msg);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(localMessageId);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(messageId);
        messageLocal.setSent(true);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
    }

    public static void notifyAudioMessageSent(long localMessageId, long messageId, long time) {
        MessageDao messageDao = AsemanDB.getInstance().getMessageDao();
        Entities.AudioMessage msg = messageDao.getAudioMessageById(localMessageId);
        messageDao.delete(msg);
        msg.setMessageId(messageId);
        msg.setTime(time);
        messageDao.insert(msg);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(localMessageId);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(messageId);
        messageLocal.setSent(true);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
    }

    public static void notifyVideoMessageSent(long localMessageId, long messageId, long time) {
        MessageDao messageDao = AsemanDB.getInstance().getMessageDao();
        Entities.VideoMessage msg = messageDao.getVideoMessageById(localMessageId);
        messageDao.delete(msg);
        msg.setMessageId(messageId);
        msg.setTime(time);
        messageDao.insert(msg);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(localMessageId);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(messageId);
        messageLocal.setSent(true);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
    }

    public static Entities.File getFileById(long fileId) {
        return AsemanDB.getInstance().getFileDao().getFileById(fileId);
    }

    public static void notifyMessageUpdated(Entities.Message message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null) {
            if (message instanceof Entities.TextMessage) {
                AsemanDB.getInstance().getMessageDao().insert((Entities.TextMessage) message);
            } else if (message instanceof Entities.PhotoMessage) {
                AsemanDB.getInstance().getMessageDao().insert((Entities.PhotoMessage) message);
            } else if (message instanceof Entities.AudioMessage) {
                AsemanDB.getInstance().getMessageDao().insert((Entities.AudioMessage) message);
            } else if (message instanceof Entities.VideoMessage) {
                AsemanDB.getInstance().getMessageDao().insert((Entities.VideoMessage) message);
            } else if (message instanceof Entities.ServiceMessage) {
                AsemanDB.getInstance().getMessageDao().insert((Entities.ServiceMessage) message);
            }
        } else {
            if (message instanceof Entities.TextMessage) {
                AsemanDB.getInstance().getMessageDao().update((Entities.TextMessage) message);
            } else if (message instanceof Entities.PhotoMessage) {
                AsemanDB.getInstance().getMessageDao().update((Entities.PhotoMessage) message);
            } else if (message instanceof Entities.AudioMessage) {
                AsemanDB.getInstance().getMessageDao().update((Entities.AudioMessage) message);
            } else if (message instanceof Entities.VideoMessage) {
                AsemanDB.getInstance().getMessageDao().update((Entities.VideoMessage) message);
            } else if (message instanceof Entities.ServiceMessage) {
                AsemanDB.getInstance().getMessageDao().update((Entities.ServiceMessage) message);
            }
        }
    }

    public static void notifyTextMessageReceived(Entities.TextMessage message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null)
            AsemanDB.getInstance().getMessageDao().insert(message);
        else
            AsemanDB.getInstance().getMessageDao().update(message);
    }

    public static void notifyPhotoMessageReceived(Entities.PhotoMessage message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null)
            AsemanDB.getInstance().getMessageDao().insert(message);
        else
            AsemanDB.getInstance().getMessageDao().update(message);
        if (AsemanDB.getInstance().getFileDao().getFileById(message.getPhotoId()) == null) {
            AsemanDB.getInstance().getFileDao().insert(message.getPhoto());
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getPhotoId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        }
        Entities.FileUsage fileUsage = message.getPhoto().getFileUsages().get(0);
        if (AsemanDB.getInstance().getFileUsageDao().getFileUsageById(fileUsage.getFileUsageId()) == null)
            AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
    }

    public static void notifyAudioMessageReceived(Entities.AudioMessage message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null)
            AsemanDB.getInstance().getMessageDao().insert(message);
        else
            AsemanDB.getInstance().getMessageDao().update(message);
        if (AsemanDB.getInstance().getFileDao().getFileById(message.getAudioId()) == null) {
            AsemanDB.getInstance().getFileDao().insert(message.getAudio());
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getAudioId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        }
        Entities.FileUsage fileUsage = message.getAudio().getFileUsages().get(0);
        if (AsemanDB.getInstance().getFileUsageDao().getFileUsageById(fileUsage.getFileUsageId()) == null)
            AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
    }

    public static void notifyVideoMessageReceived(Entities.VideoMessage message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null)
            AsemanDB.getInstance().getMessageDao().insert(message);
        else
            AsemanDB.getInstance().getMessageDao().update(message);
        if (AsemanDB.getInstance().getFileDao().getFileById(message.getVideoId()) == null) {
            AsemanDB.getInstance().getFileDao().insert(message.getVideo());
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getVideoId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        }
        Entities.FileUsage fileUsage = message.getVideo().getFileUsages().get(0);
        if (AsemanDB.getInstance().getFileUsageDao().getFileUsageById(fileUsage.getFileUsageId()) == null)
            AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
    }

    public static void notifyServiceMessageReceived(Entities.ServiceMessage message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null)
            AsemanDB.getInstance().getMessageDao().insert(message);
        else
            AsemanDB.getInstance().getMessageDao().update(message);
    }

    public static Pair<Entities.File, Entities.FileLocal> notifyPhotoUploading(boolean isPublic, String path, int width, int height) {
        Entities.Photo file = new Entities.Photo();
        file.setPublic(isPublic);
        file.setWidth(width);
        file.setHeight(height);
        file.setFileId(generateLocalId());
        AsemanDB.getInstance().getFileDao().insert(file);
        Entities.FileLocal fileLocal = new Entities.FileLocal();
        fileLocal.setFileId(file.getFileId());
        fileLocal.setPath(path);
        fileLocal.setProgress(0);
        fileLocal.setTransferring(true);
        AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        return new Pair<>(file, fileLocal);
    }

    public static Pair<Entities.File, Entities.FileLocal> notifyAudioUploading(boolean isPublic, String path, String title, long duration) {
        Entities.Audio file = new Entities.Audio();
        file.setPublic(isPublic);
        file.setTitle(title);
        file.setDuration(duration);
        file.setFileId(generateLocalId());
        AsemanDB.getInstance().getFileDao().insert(file);
        Entities.FileLocal fileLocal = new Entities.FileLocal();
        fileLocal.setFileId(file.getFileId());
        fileLocal.setPath(path);
        fileLocal.setProgress(0);
        fileLocal.setTransferring(true);
        AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        return new Pair<>(file, fileLocal);
    }

    public static Pair<Entities.File, Entities.FileLocal> notifyVideoUploading(boolean isPublic, String path, String title, long duration) {
        Entities.Video file = new Entities.Video();
        file.setPublic(isPublic);
        file.setTitle(title);
        file.setDuration(duration);
        file.setFileId(generateLocalId());
        AsemanDB.getInstance().getFileDao().insert(file);
        Entities.FileLocal fileLocal = new Entities.FileLocal();
        fileLocal.setFileId(file.getFileId());
        fileLocal.setPath(path);
        fileLocal.setProgress(0);
        fileLocal.setTransferring(true);
        AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        return new Pair<>(file, fileLocal);
    }

    public static void notifyUpdateMessageAfterFileUpload(long messageId, long fileId, long fileUsageId) {
        Entities.Message message = AsemanDB.getInstance().getMessageDao().getMessageById(messageId);
        if (message instanceof Entities.PhotoMessage) {
            ((Entities.PhotoMessage) message).setPhotoId(fileId);
            AsemanDB.getInstance().getMessageDao().update((Entities.PhotoMessage) message);
        } else if (message instanceof Entities.AudioMessage) {
            ((Entities.AudioMessage) message).setAudioId(fileId);
            AsemanDB.getInstance().getMessageDao().update((Entities.AudioMessage) message);
        } else if (message instanceof Entities.VideoMessage) {
            ((Entities.VideoMessage) message).setVideoId(fileId);
            AsemanDB.getInstance().getMessageDao().update((Entities.VideoMessage) message);
        }
        Entities.FileUsage fileUsage = new Entities.FileUsage();
        fileUsage.setFileUsageId(fileUsageId);
        fileUsage.setFileId(fileId);
        fileUsage.setRoomId(message.getRoomId());
        AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
    }

    public static void notifyPhotoUploaded(long localFileId, long fileId) {
        Entities.Photo file = AsemanDB.getInstance().getFileDao().getPhotoById(localFileId);
        AsemanDB.getInstance().getFileDao().delete(file);
        file.setFileId(fileId);
        AsemanDB.getInstance().getFileDao().insert(file);
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(localFileId);
        if (fileLocal != null) {
            AsemanDB.getInstance().getFileLocalDao().delete(fileLocal);
            fileLocal.setFileId(fileId);
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        }
    }

    public static void notifyAudioUploaded(long localFileId, long fileId) {
        Entities.Audio file = AsemanDB.getInstance().getFileDao().getAudioById(localFileId);
        AsemanDB.getInstance().getFileDao().delete(file);
        file.setFileId(fileId);
        AsemanDB.getInstance().getFileDao().insert(file);
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(localFileId);
        if (fileLocal != null) {
            AsemanDB.getInstance().getFileLocalDao().delete(fileLocal);
            fileLocal.setFileId(fileId);
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        }
    }

    public static void notifyVideoUploaded(long localFileId, long fileId) {
        Entities.Video file = AsemanDB.getInstance().getFileDao().getVideoById(localFileId);
        AsemanDB.getInstance().getFileDao().delete(file);
        file.setFileId(fileId);
        AsemanDB.getInstance().getFileDao().insert(file);
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(localFileId);
        if (fileLocal != null) {
            AsemanDB.getInstance().getFileLocalDao().delete(fileLocal);
            fileLocal.setFileId(fileId);
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        }
    }

    public static void notifyFileDownloading(long fileId) {
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(fileId);
        if (fileLocal != null) {
            fileLocal.setTransferring(true);
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        }
    }

    public static void notifyFileDownloaded(long fileId) {
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(fileId);
        if (fileLocal != null) {
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        }
    }

    public static void notifyFileDownloadCancelled(long fileId) {
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(fileId);
        if (fileLocal != null) {
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        }
        java.io.File file = new java.io.File(getFilePath(fileId));
        if (file.exists()) {
            if (file.delete()) {
                Log.d("Aseman", "Clearing cancelled download temp");
            }
        }
    }

    public static Hashtable<Long, Entities.MessageLocal> getLocalMessages(List<Entities.Message> messages) {
        Hashtable<Long, Entities.MessageLocal> localMessages = new Hashtable<>();
        List<Long> messageIds = new ArrayList<>();
        for (Entities.Message message : messages) {
            messageIds.add(message.getMessageId());
        }
        List<Entities.MessageLocal> messageLocals = AsemanDB.getInstance()
                .getMessageLocalDao().getMessageLocalsByIds(messageIds);
        for (Entities.MessageLocal messageLocal : messageLocals) {
            localMessages.put(messageLocal.getMessageId(), messageLocal);
        }
        try {
            Log.d("Aseman", NetworkHelper.getMapper().writeValueAsString(messages));
            Log.d("Aseman", NetworkHelper.getMapper().writeValueAsString(localMessages));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return localMessages;
    }

    public static Hashtable<Long, Entities.FileLocal> getLocalFiles(List<Entities.Message> messages) {
        Hashtable<Long, Entities.FileLocal> fileLocals = new Hashtable<>();
        List<Long> fileIds = new ArrayList<>();
        for (Entities.Message message : messages) {
            if (message instanceof Entities.PhotoMessage) {
                fileIds.add(((Entities.PhotoMessage)message).getPhotoId());
            } else if (message instanceof Entities.AudioMessage) {
                fileIds.add(((Entities.AudioMessage)message).getAudioId());
            } else if (message instanceof Entities.VideoMessage) {
                fileIds.add(((Entities.VideoMessage)message).getVideoId());
            }
        }
        List<Entities.FileLocal> fileLocalsList = AsemanDB.getInstance()
                .getFileLocalDao().getFileLocalsByIds(fileIds);
        for (Entities.FileLocal fileLocal : fileLocalsList) {
            fileLocals.put(fileLocal.getFileId(), fileLocal);
        }
        return fileLocals;
    }

    public static void notifyFileTransferProgressed(long fileId, int progress) {
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(fileId);
        if (fileLocal != null) {
            fileLocal.setProgress(progress);
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        }
    }

    public static void deleteTextMessage(long roomId, long messageId) {
        AsemanDB.getInstance().getMessageDao().deleteMessageById(messageId);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(messageId);
    }

    public static void deletePhotoMessage(long messageId) {
        AsemanDB.getInstance().getMessageDao().deleteMessageById(messageId);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(messageId);
    }

    public static void deleteAudioMessage(long messageId) {
        AsemanDB.getInstance().getMessageDao().deleteMessageById(messageId);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(messageId);
    }

    public static void deleteVideoMessage(long messageId) {
        AsemanDB.getInstance().getMessageDao().deleteMessageById(messageId);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(messageId);
    }

    public static void deleteServiceMessage(long messageId) {
        AsemanDB.getInstance().getMessageDao().deleteMessageById(messageId);
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(messageId);
    }

    public static Entities.Message getMessageById(long messageId) {
        Entities.Message message = AsemanDB.getInstance().getMessageDao().getMessageById(messageId);
        if (message != null) {
            message.setAuthor(getBaseUserById(message.getAuthorId()));
            message.setRoom(getRoomById(message.getRoomId()));
        }
        return message;
    }

    public static List<Entities.Message> getMessages(long roomId) {
        Entities.Room room = getRoomById(roomId);
        List<Entities.Message> messages = AsemanDB.getInstance().getMessageDao().getMessages(roomId);
        HashSet<Long> authorIds = new HashSet<>();
        for (Entities.Message message : messages)
            authorIds.add(message.getAuthorId());
        List<Entities.User> users = AsemanDB.getInstance().getUserDao().getUsersByIds(new ArrayList<>(authorIds));
        Hashtable<Long, Entities.User> usersTable = new Hashtable<>();
        for (Entities.User user : users)
            usersTable.put(user.getBaseUserId(), user);
        HashSet<Long> fileIds = new HashSet<>();
        for (Entities.Message message : messages) {
            if (message instanceof Entities.PhotoMessage) {
                fileIds.add(((Entities.PhotoMessage) message).getPhotoId());
            } else if (message instanceof Entities.AudioMessage) {
                fileIds.add(((Entities.AudioMessage) message).getAudioId());
            } else if (message instanceof Entities.VideoMessage) {
                fileIds.add(((Entities.VideoMessage) message).getVideoId());
            }
        }
        List<Entities.File> files = AsemanDB.getInstance().getFileDao().getFilesByIds(new ArrayList<>(fileIds));
        Hashtable<Long, Entities.File> filesTable = new Hashtable<>();
        for (Entities.File file : files) {
            filesTable.put(file.getFileId(), file);
        }
        for (Entities.Message message : messages) {
            message.setAuthor(usersTable.get(message.getAuthorId()));
            message.setRoom(room);
            if (message instanceof Entities.PhotoMessage) {
                ((Entities.PhotoMessage) message).setPhoto((Entities.Photo)
                        filesTable.get(((Entities.PhotoMessage) message).getPhotoId()));
            } else if (message instanceof Entities.AudioMessage) {
                ((Entities.AudioMessage) message).setAudio((Entities.Audio)
                        filesTable.get(((Entities.AudioMessage) message).getAudioId()));
            } else if (message instanceof Entities.VideoMessage) {
                ((Entities.VideoMessage) message).setVideo((Entities.Video)
                        filesTable.get(((Entities.VideoMessage) message).getVideoId()));
            }
        }
        if (messages.size() == 0) {
            Entities.ServiceMessage serviceMessage = new Entities.ServiceMessage();
            serviceMessage.setMessageId(-1);
            serviceMessage.setText("Room created.");
            serviceMessage.setAuthor(new Entities.User());
            serviceMessage.setAuthorId(-1);
            serviceMessage.setRoomId(roomId);
            serviceMessage.setRoom(room);
            serviceMessage.setTime(System.currentTimeMillis());
        }
        return messages;
    }

    public static List<Entities.Invite> getMyInvites() {
        List<Entities.Invite> invites = AsemanDB.getInstance().getInviteDao().getInvites();
        for (Entities.Invite invite : invites) {
            invite.setUser(AsemanDB.getInstance().getUserDao().getUserById(invite.getUserId()));
            invite.setComplex(AsemanDB.getInstance().getComplexDao().getComplexById(invite.getComplexId()));
        }
        return invites;
    }

    public static List<Entities.Invite> getComplexInvites(long complexId) {
        List<Entities.Invite> invites = AsemanDB.getInstance().getInviteDao().getComplexInvites(complexId);
        for (Entities.Invite invite : invites) {
            invite.setUser(AsemanDB.getInstance().getUserDao().getUserById(invite.getUserId()));
            invite.setComplex(AsemanDB.getInstance().getComplexDao().getComplexById(invite.getComplexId()));
        }
        return invites;
    }

    public static void notifyInviteReceived(Entities.Invite invite) {
        AsemanDB.getInstance().getInviteDao().insert(invite);
    }

    public static void notifyInviteSent(Entities.Invite invite) {
        AsemanDB.getInstance().getInviteDao().insert(invite);
    }

    public void notifyInviteResolved(Entities.Invite invite) {
        AsemanDB.getInstance().getInviteDao().delete(invite);
    }

    public static boolean notifyContactCreated(Entities.Contact contact) {
        if (AsemanDB.getInstance().getContactDao().getContactByPeerId(contact.getPeerId()) == null) {
            AsemanDB.getInstance().getContactDao().insert(contact);
            return true;
        } else {
            AsemanDB.getInstance().getContactDao().update(contact);
            return false;
        }
    }

    public static Entities.Contact getContactByComplexId(long complexId) {
        Entities.Contact contact = AsemanDB.getInstance().getContactDao().getContactByComplexId(complexId);
        contact.setComplex(getComplexById(contact.getComplexId()));
        contact.setUser((Entities.User)getBaseUserById(contact.getUserId()));
        contact.setPeer((Entities.User)getBaseUserById(contact.getPeerId()));
        return contact;
    }

    public static List<Entities.Contact> getContacts() {
        List<Entities.Contact> contacts = AsemanDB.getInstance().getContactDao().getContacts();
        HashSet<Long> userIds = new HashSet<>();
        HashSet<Long> complexIds = new HashSet<>();
        for (Entities.Contact contact : contacts) {
            userIds.add(contact.getPeerId());
            userIds.add(contact.getUserId());
            complexIds.add(contact.getComplexId());
        }
        List<Entities.Complex> complexes = AsemanDB.getInstance().getComplexDao()
                .getComplexesByIds(new ArrayList<>(complexIds));
        List<Entities.User> users = AsemanDB.getInstance().getUserDao()
                .getUsersByIds(new ArrayList<>(userIds));
        List<Entities.Room> rooms = AsemanDB.getInstance().getRoomDao()
                .getRoomsByComplexesIds(new ArrayList<>(complexIds));
        Hashtable<Long, Entities.Complex> complexTable = new Hashtable<>();
        for (Entities.Complex complex : complexes) {
            complex.setRooms(new ArrayList<>());
            complexTable.put(complex.getComplexId(), complex);
        }
        Hashtable<Long, Entities.User> userTable = new Hashtable<>();
        for (Entities.User user : users) {
            userTable.put(user.getBaseUserId(), user);
        }
        for (Entities.Room room : rooms) {
            room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(room.getRoomId()));
            if (room.getLastAction() != null)
                room.getLastAction().setAuthor(AsemanDB.getInstance().getUserDao()
                        .getUserById(room.getLastAction().getAuthorId()));
            complexTable.get(room.getComplexId()).getRooms().add(room);
        }
        for (Entities.Contact contact : contacts) {
            contact.setUser(userTable.get(contact.getUserId()));
            contact.setPeer(userTable.get(contact.getPeerId()));
            contact.setComplex(complexTable.get(contact.getComplexId()));
        }
        return contacts;
    }

    public static Pair<List<Entities.Photo>, Hashtable<Long, Entities.FileLocal>> getPhotos(long roomId) {
        List<Entities.Photo> files = AsemanDB.getInstance().getFileDao().getPhotos(roomId);
        List<Long> fileIds = new ArrayList<>();
        for (Entities.File file : files) {
            fileIds.add(file.getFileId());
        }
        Hashtable<Long, Entities.FileLocal> fileLocalsTable = new Hashtable<>();
        List<Entities.FileLocal> fileLocals = AsemanDB.getInstance().getFileLocalDao().getFileLocalsByIds(fileIds);
        for (Entities.FileLocal fileLocal : fileLocals) {
            fileLocalsTable.put(fileLocal.getFileId(), fileLocal);
        }
        return new Pair<>(files, fileLocalsTable);
    }

    public static Pair<List<Entities.Audio>, Hashtable<Long, Entities.FileLocal>> getAudios(long roomId) {
        List<Entities.Audio> files = AsemanDB.getInstance().getFileDao().getAudios(roomId);
        List<Long> fileIds = new ArrayList<>();
        for (Entities.File file : files) {
            fileIds.add(file.getFileId());
        }
        Hashtable<Long, Entities.FileLocal> fileLocalsTable = new Hashtable<>();
        List<Entities.FileLocal> fileLocals = AsemanDB.getInstance().getFileLocalDao().getFileLocalsByIds(fileIds);
        for (Entities.FileLocal fileLocal : fileLocals) {
            fileLocalsTable.put(fileLocal.getFileId(), fileLocal);
        }
        return new Pair<>(files, fileLocalsTable);
    }

    public static Pair<List<Entities.Video>, Hashtable<Long, Entities.FileLocal>> getVideos(long roomId) {
        List<Entities.Video> files = AsemanDB.getInstance().getFileDao().getVideos(roomId);
        List<Long> fileIds = new ArrayList<>();
        for (Entities.File file : files) {
            fileIds.add(file.getFileId());
        }
        Hashtable<Long, Entities.FileLocal> fileLocalsTable = new Hashtable<>();
        List<Entities.FileLocal> fileLocals = AsemanDB.getInstance().getFileLocalDao().getFileLocalsByIds(fileIds);
        for (Entities.FileLocal fileLocal : fileLocals) {
            fileLocalsTable.put(fileLocal.getFileId(), fileLocal);
        }
        return new Pair<>(files, fileLocalsTable);
    }

    public static String getFilePath(long fileId) {
        return new java.io.File(new java.io.File(Environment.getExternalStorageDirectory()
                , DatabaseHelper.StorageDir), fileId + "").getPath();
    }

    private static long generateLocalId() {
        KeyValueDao keyValueDao = AsemanDB.getInstance().getKeyValueDao();
        Entities.IdKeeper idKeeper = keyValueDao.getIdKeeper();
        long newId = idKeeper.getLocalId() + 1;
        idKeeper.setLocalId(newId);
        keyValueDao.update(idKeeper);
        return -1 * newId;
    }
}
