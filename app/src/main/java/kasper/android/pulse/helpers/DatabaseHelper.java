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
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.FileMessageSending;
import kasper.android.pulse.models.extras.TextMessageSending;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.extras.YoloBoundingBox;
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
                LogHelper.log("Aseman", "Storage Dir initialized or checked successfully");
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

    public static void notifyYoloBoundingBoxCreated(YoloBoundingBox box) {
        AsemanDB.getInstance().getYoloBoundingBoxDao().insert(box);
    }

    public static List<YoloBoundingBox> getImageYoloBoundingBoxes(long imageId) {
        return AsemanDB.getInstance().getYoloBoundingBoxDao().getImageYoloBoundingBoxes(imageId);
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

    public static Entities.ComplexSecret getComplexSecretByComplexId(long complexId) {
        return AsemanDB.getInstance().getComplexSecretDao().getComplexSecretByComplexId(complexId);
    }

    public static void notifyComplexRemoved(long complexId) {
        AsemanDB.getInstance().getComplexDao().deleteComplexById(complexId);
        AsemanDB.getInstance().getRoomDao().deleteComplexRooms(complexId);
        AsemanDB.getInstance().getMembershipDao().deleteComplexMemberships(complexId);
        AsemanDB.getInstance().getInviteDao().deleteComplexInvites(complexId);
    }

    public static boolean notifyRoomCreated(Entities.BaseRoom room) {
        RoomDao roomDao = AsemanDB.getInstance().getRoomDao();
        if (roomDao.getRoomById(room.getRoomId()) != null) {
            if (room instanceof Entities.Room)
                roomDao.update((Entities.Room) room);
            else if (room instanceof Entities.SingleRoom)
                roomDao.update((Entities.SingleRoom) room);
            return false;
        } else {
            if (room instanceof Entities.Room) {
                roomDao.insert((Entities.Room) room);
            } else if (room instanceof Entities.SingleRoom) {
                roomDao.insert((Entities.SingleRoom) room);
            }
            return true;
        }
    }

    public static void notifyRoomRemoved(long roomId) {
        AsemanDB.getInstance().getRoomDao().deleteRoomById(roomId);
    }

    public static List<Entities.Complex> getComplexes() {
        List<Entities.Complex> complexes = AsemanDB.getInstance().getComplexDao().getComplexes();
        for (Entities.Complex complex : complexes) {
            List<Entities.Room> rooms = AsemanDB.getInstance().getRoomDao().getComplexNonSingleRooms(complex.getComplexId());
            List<Entities.SingleRoom> singleRooms = AsemanDB.getInstance().getRoomDao().getComplexSingleRooms(complex.getComplexId());
            for (Entities.Room room : rooms)
                room.setComplex(complex);
            complex.setRooms(rooms);
            for (Entities.SingleRoom singleRoom : singleRooms)
                singleRoom.setComplex(complex);
            complex.setSingleRooms(singleRooms);
        }
        return complexes;
    }

    public static List<Entities.Complex> getInvitableComplexes() {
        Entities.User me = getMe();
        List<Entities.Complex> result = new ArrayList<>();
        if (me != null) {
            List<Entities.Membership> memberships = AsemanDB.getInstance().getMembershipDao().getUserMemberships(me.getBaseUserId());
            for (Entities.Membership membership : memberships) {
                membership.setMemberAccess(AsemanDB.getInstance().getMemberAccessDao().getMemberAccessByMembershipId(membership.getMembershipId()));
                membership.setComplex(AsemanDB.getInstance().getComplexDao().getComplexById(membership.getComplexId()));
                membership.setUser(me);
                if (membership.getComplex().getMode() == 3 && membership.getMemberAccess() != null && membership.getMemberAccess().isCanSendInvite()) {
                    Entities.Complex complex = membership.getComplex();
                    List<Entities.Room> rooms = AsemanDB.getInstance().getRoomDao().getComplexNonSingleRooms(complex.getComplexId());
                    List<Entities.SingleRoom> singleRooms = AsemanDB.getInstance().getRoomDao().getComplexSingleRooms(complex.getComplexId());
                    for (Entities.Room room : rooms)
                        room.setComplex(complex);
                    complex.setRooms(rooms);
                    for (Entities.SingleRoom singleRoom : singleRooms)
                        singleRoom.setComplex(complex);
                    complex.setSingleRooms(singleRooms);
                    result.add(complex);
                }
            }
        }
        return result;
    }

    public static List<Entities.BaseRoom> getHomeRooms() {
        List<Entities.BaseRoom> rooms = new ArrayList<>();
        Entities.User me = getMe();
        if (me != null) {
            long homeId = me.getUserSecret().getHomeId();
            rooms = AsemanDB.getInstance().getRoomDao().getComplexRooms(homeId);
            Entities.Complex home = AsemanDB.getInstance().getComplexDao().getComplexById(homeId);
            for (Entities.BaseRoom room : rooms) {
                room.setComplex(home);
            }
        }
        return rooms;
    }

    public static List<Entities.BaseRoom> getContactRooms() {
        List<Entities.Room> rooms = AsemanDB.getInstance().getRoomDao().getAllContactRooms();
        List<Entities.BaseRoom> baseRooms = new ArrayList<>();
        Hashtable<Long, Entities.Complex> cache = new Hashtable<>();
        for (Entities.BaseRoom room : rooms) {
            if (cache.containsKey(room.getComplexId())) {
                Entities.Complex c = AsemanDB.getInstance().getComplexDao().getComplexById(room.getComplexId());
                room.setComplex(c);
                cache.put(c.getComplexId(), c);
            } else {
                room.setComplex(cache.get(room.getComplexId()));
            }
            baseRooms.add(room);
        }
        return baseRooms;
    }

    public static List<Entities.BaseRoom> getRooms(long complexId) {
        Entities.Complex complex = AsemanDB.getInstance().getComplexDao().getComplexById(complexId);
        List<Entities.BaseRoom> rooms = AsemanDB.getInstance().getRoomDao().getComplexRooms(complexId);
        for (Entities.BaseRoom room : rooms) {
            room.setComplex(complex);
            room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(room.getRoomId()));
            if (room.getLastAction() != null)
                room.getLastAction().setAuthor(getBaseUserById(room.getLastAction().getAuthorId()));
            if (room instanceof Entities.SingleRoom) {
                ((Entities.SingleRoom) room).setUser1(AsemanDB.getInstance().getUserDao().getUserById(((Entities.SingleRoom) room).getUser1Id()));
                ((Entities.SingleRoom) room).setUser2(AsemanDB.getInstance().getUserDao().getUserById(((Entities.SingleRoom) room).getUser2Id()));
            }
        }
        return rooms;
    }

    public static List<Entities.BaseRoom> getComplexSingleRooms(long complexId) {
        List<Entities.SingleRoom> singleRooms = AsemanDB.getInstance().getRoomDao().getComplexSingleRooms(complexId);
        List<Entities.BaseRoom> baseRooms = new ArrayList<>();
        Entities.Complex c = AsemanDB.getInstance().getComplexDao().getComplexById(complexId);
        for (Entities.SingleRoom sr : singleRooms) {
            sr.setComplex(c);
            baseRooms.add(sr);
        }
        return baseRooms;
    }

    public static List<Entities.BaseRoom> getComplexNonSingleRooms(long complexId) {
        List<Entities.Room> rooms = AsemanDB.getInstance().getRoomDao().getComplexNonSingleRooms(complexId);
        List<Entities.BaseRoom> baseRooms = new ArrayList<>();
        Entities.Complex c = AsemanDB.getInstance().getComplexDao().getComplexById(complexId);
        for (Entities.Room r : rooms) {
            r.setComplex(c);
            baseRooms.add(r);
        }
        return baseRooms;
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
            Entities.MemberAccess memberAccess = AsemanDB.getInstance().getMemberAccessDao().getMemberAccessById(membership.getMemberAccessId());
            if (memberAccess != null)
                memberAccess.setMembership(membership);
            membership.setMemberAccess(memberAccess);
        }
        return memberships;
    }

    public static Entities.Membership getMembershipById(long membershipId) {
        Entities.Membership membership = AsemanDB.getInstance().getMembershipDao().getMembershipById(membershipId);
        membership.setComplex(getComplexById(membership.getComplexId()));
        membership.setUser((Entities.User) getBaseUserById(membership.getUserId()));
        Entities.MemberAccess memberAccess = AsemanDB.getInstance().getMemberAccessDao().getMemberAccessByMembershipId(membershipId);
        if (memberAccess != null) {
            memberAccess.setMembership(membership);
            membership.setMemberAccess(memberAccess);
        }
        return membership;
    }

    public static Entities.Membership getMembershipByComplexAndUserId(long complexId, long userId) {
        Entities.Membership membership = AsemanDB.getInstance().getMembershipDao().getMembershipByUserAndComplexId(userId, complexId);
        Entities.MemberAccess memberAccess = AsemanDB.getInstance().getMemberAccessDao().getMemberAccessByMembershipId(membership.getMembershipId());
        if (memberAccess != null)
            memberAccess.setMembership(membership);
        membership.setMemberAccess(memberAccess);
        return membership;
    }

    public static Entities.MemberAccess getMemberAccessByComplexAndUserId(long complexId, long userId) {
        Entities.Membership membership = AsemanDB.getInstance().getMembershipDao().getMembershipByUserAndComplexId(userId, complexId);
        if (membership == null) return null;
        Entities.MemberAccess memberAccess = AsemanDB.getInstance().getMemberAccessDao().getMemberAccessByMembershipId(membership.getMembershipId());
        if (memberAccess == null) return null;
        memberAccess.setMembership(membership);
        membership.setMemberAccess(memberAccess);
        return memberAccess;
    }

    public static boolean isUserMemberOfComplex(long complexId, long userId) {
        return AsemanDB.getInstance().getMembershipDao().getMembershipByUserAndComplexId(userId, complexId) != null;
    }

    public static List<Entities.BaseRoom> getAllRooms() {
        List<Entities.BaseRoom> rooms = AsemanDB.getInstance().getRoomDao().getAllRooms();
        for (Entities.BaseRoom room : rooms) {
            room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(room.getRoomId()));
            if (room.getLastAction() != null)
                room.getLastAction().setAuthor(getBaseUserById(room.getLastAction().getAuthorId()));
            room.setComplex(getComplexById(room.getComplexId()));
            if (room instanceof Entities.SingleRoom) {
                ((Entities.SingleRoom) room).setUser1(AsemanDB.getInstance().getUserDao().getUserById(((Entities.SingleRoom) room).getUser1Id()));
                ((Entities.SingleRoom) room).setUser2(AsemanDB.getInstance().getUserDao().getUserById(((Entities.SingleRoom) room).getUser2Id()));
            }
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
        if (complex != null) {
            complex.setComplexSecret(AsemanDB.getInstance().getComplexSecretDao().getComplexSecretByComplexId(complexId));
            complex.setRooms(AsemanDB.getInstance().getRoomDao().getComplexNonSingleRooms(complexId));
            complex.setSingleRooms(AsemanDB.getInstance().getRoomDao().getComplexSingleRooms(complexId));
        }
        return complex;
    }

    public static Entities.BaseRoom getRoomById(long roomId) {
        Entities.BaseRoom room = AsemanDB.getInstance().getRoomDao().getRoomById(roomId);
        if (room != null) {
            room.setComplex(AsemanDB.getInstance().getComplexDao().getComplexById(room.getComplexId()));
            room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(roomId));
            if (room.getLastAction() != null)
                room.getLastAction().setAuthor(getBaseUserById(room.getLastAction().getAuthorId()));
        }
        return room;
    }

    public static void updateComplex(Entities.Complex mComplex) {
        AsemanDB.getInstance().getComplexDao().update(mComplex);
    }

    public static void updateRoom(Entities.BaseRoom mRoom) {
        if (mRoom instanceof Entities.Room)
            AsemanDB.getInstance().getRoomDao().update((Entities.Room) mRoom);
        else if (mRoom instanceof Entities.SingleRoom)
            AsemanDB.getInstance().getRoomDao().update((Entities.SingleRoom) mRoom);
        else {
            Entities.Room r = new Entities.Room();
            r.setRoomId(mRoom.getRoomId());
            r.setTitle(mRoom.getTitle());
            r.setAvatar(mRoom.getAvatar());
            r.setComplexId(mRoom.getComplexId());
            AsemanDB.getInstance().getRoomDao().update(r);
        }
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

    public static void notifyMembershipCreated(Entities.Membership membership) {
        if (AsemanDB.getInstance().getMembershipDao().getMembershipById(membership.getMembershipId()) == null)
            AsemanDB.getInstance().getMembershipDao().insert(membership);
        else
            AsemanDB.getInstance().getMembershipDao().update(membership);
    }

    public static void notifyMemberAccessCreated(Entities.MemberAccess memberAccess) {
        if (AsemanDB.getInstance().getMemberAccessDao().getMemberAccessById(memberAccess.getMemberAccessId()) == null) {
            AsemanDB.getInstance().getMemberAccessDao().insert(memberAccess);
            LogHelper.log("Adgar", "hello 0");
        } else {
            AsemanDB.getInstance().getMemberAccessDao().update(memberAccess);
            LogHelper.log("Adgar", "hello 1");
        }
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
            Entities.Session session = AsemanDB.getInstance().getSessionDao().getSessionByUserId(bot.getBaseUserId());
            if (session != null)
                sessions.add(session);
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
        if (msg != null) {
            messageDao.delete(msg);
            msg.setMessageId(messageId);
            msg.setTime(time);
            messageDao.insert(msg);
        }
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(localMessageId);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(messageId);
        messageLocal.setSent(true);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
    }

    public static void notifyAudioMessageSent(long localMessageId, long messageId, long time) {
        MessageDao messageDao = AsemanDB.getInstance().getMessageDao();
        Entities.AudioMessage msg = messageDao.getAudioMessageById(localMessageId);
        if (msg != null) {
            messageDao.delete(msg);
            msg.setMessageId(messageId);
            msg.setTime(time);
            messageDao.insert(msg);
        }
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(localMessageId);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(messageId);
        messageLocal.setSent(true);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
    }

    public static void notifyVideoMessageSent(long localMessageId, long messageId, long time) {
        MessageDao messageDao = AsemanDB.getInstance().getMessageDao();
        Entities.VideoMessage msg = messageDao.getVideoMessageById(localMessageId);
        if (msg != null) {
            messageDao.delete(msg);
            msg.setMessageId(messageId);
            msg.setTime(time);
            messageDao.insert(msg);
        }
        AsemanDB.getInstance().getMessageLocalDao().deleteMessageById(localMessageId);
        Entities.MessageLocal messageLocal = new Entities.MessageLocal();
        messageLocal.setMessageId(messageId);
        messageLocal.setSent(true);
        AsemanDB.getInstance().getMessageLocalDao().insert(messageLocal);
    }

    public static Entities.File getFileById(long fileId) {
        return AsemanDB.getInstance().getFileDao().getFileById(fileId);
    }

    public static Entities.FileLocal getFileLocalByFileId(long fileId) {
        return AsemanDB.getInstance().getFileLocalDao().getFileLocalById(fileId);
    }

    public static void notifyTextMessageSeen(long messageId, long seenCount) {
        AsemanDB.getInstance().getMessageDao().updateTextMessageSeenCount(messageId, seenCount);
    }

    public static void notifyPhotoMessageSeen(long messageId, long seenCount) {
        AsemanDB.getInstance().getMessageDao().updatePhotoMessageSeenCount(messageId, seenCount);
    }

    public static void notifyAudioMessageSeen(long messageId, long seenCount) {
        AsemanDB.getInstance().getMessageDao().updateAudioMessageSeenCount(messageId, seenCount);
    }

    public static void notifyVideoMessageSeen(long messageId, long seenCount) {
        AsemanDB.getInstance().getMessageDao().updateVideoMessageSeenCount(messageId, seenCount);
    }

    public static void notifyServiceMessageSeen(long messageId, long seenCount) {
        AsemanDB.getInstance().getMessageDao().updateServiceMessageSeenCount(messageId, seenCount);
    }

    public static void notifyTextMessageSeenByMe(long messageId) {
        AsemanDB.getInstance().getMessageDao().notifyTextMessageSeenByMe(messageId);
    }

    public static void notifyPhotoMessageSeenByMe(long messageId) {
        AsemanDB.getInstance().getMessageDao().notifyPhotoMessageSeenByMe(messageId);
    }

    public static void notifyAudioMessageSeenByMe(long messageId) {
        AsemanDB.getInstance().getMessageDao().notifyAudioMessageSeenByMe(messageId);
    }

    public static void notifyVideoMessageSeenByMe(long messageId) {
        AsemanDB.getInstance().getMessageDao().notifyVideoMessageSeenByMe(messageId);
    }

    public static void notifyServiceMessageSeenByMe(long messageId) {
        AsemanDB.getInstance().getMessageDao().notifyServiceMessageSeenByMe(messageId);
    }

    public static boolean notifyMessageUpdated(Entities.Message message) {
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
            return true;
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
            return false;
        }
    }

    public static boolean notifyTextMessageReceived(Entities.TextMessage message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null) {
            AsemanDB.getInstance().getMessageDao().insert(message);
            return true;
        } else {
            AsemanDB.getInstance().getMessageDao().update(message);
            return false;
        }
    }

    public static boolean notifyPhotoMessageReceived(Entities.PhotoMessage message) {
        boolean result;
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null) {
            AsemanDB.getInstance().getMessageDao().insert(message);
            result = true;
        } else {
            AsemanDB.getInstance().getMessageDao().update(message);
            result = false;
        }
        if (AsemanDB.getInstance().getFileDao().getFileById(message.getPhotoId()) == null)
            AsemanDB.getInstance().getFileDao().insert(message.getPhoto());
        else
            AsemanDB.getInstance().getFileDao().update(message.getPhoto());
        Entities.FileLocal fileLocal = new Entities.FileLocal();
        fileLocal.setFileId(message.getPhotoId());
        fileLocal.setPath("");
        fileLocal.setProgress(0);
        fileLocal.setTransferring(false);
        if (AsemanDB.getInstance().getFileLocalDao().getFileLocalById(message.getPhotoId()) == null)
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        else
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        if (message.getPhoto().getFileUsages() != null && message.getPhoto().getFileUsages().size() > 0) {
            Entities.FileUsage fileUsage = message.getPhoto().getFileUsages().get(0);
            if (AsemanDB.getInstance().getFileUsageDao().getFileUsageById(fileUsage.getFileUsageId()) == null)
                AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
        }
        return result;
    }

    public static boolean notifyAudioMessageReceived(Entities.AudioMessage message) {
        boolean result;
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null) {
            AsemanDB.getInstance().getMessageDao().insert(message);
            result = true;
        } else {
            AsemanDB.getInstance().getMessageDao().update(message);
            result = false;
        }
        if (AsemanDB.getInstance().getFileDao().getFileById(message.getAudioId()) == null)
            AsemanDB.getInstance().getFileDao().insert(message.getAudio());
        else
            AsemanDB.getInstance().getFileDao().update(message.getAudio());
        Entities.FileLocal fileLocal = new Entities.FileLocal();
        fileLocal.setFileId(message.getAudioId());
        fileLocal.setPath("");
        fileLocal.setProgress(0);
        fileLocal.setTransferring(false);
        if (AsemanDB.getInstance().getFileLocalDao().getFileLocalById(message.getAudioId()) == null)
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        else
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        if (message.getAudio().getFileUsages() != null && message.getAudio().getFileUsages().size() > 0) {
            Entities.FileUsage fileUsage = message.getAudio().getFileUsages().get(0);
            if (AsemanDB.getInstance().getFileUsageDao().getFileUsageById(fileUsage.getFileUsageId()) == null)
                AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
        }
        return result;
    }

    public static boolean notifyVideoMessageReceived(Entities.VideoMessage message) {
        boolean result;
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null) {
            AsemanDB.getInstance().getMessageDao().insert(message);
            result = true;
        } else {
            AsemanDB.getInstance().getMessageDao().update(message);
            result = false;
        }
        if (AsemanDB.getInstance().getFileDao().getFileById(message.getVideoId()) == null)
            AsemanDB.getInstance().getFileDao().insert(message.getVideo());
        else
            AsemanDB.getInstance().getFileDao().update(message.getVideo());
        Entities.FileLocal fileLocal = new Entities.FileLocal();
        fileLocal.setFileId(message.getVideoId());
        fileLocal.setPath("");
        fileLocal.setProgress(0);
        fileLocal.setTransferring(false);
        if (AsemanDB.getInstance().getFileLocalDao().getFileLocalById(message.getVideoId()) == null)
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
        else
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        if (message.getVideo().getFileUsages() != null && message.getVideo().getFileUsages().size() > 0) {
            Entities.FileUsage fileUsage = message.getVideo().getFileUsages().get(0);
            if (AsemanDB.getInstance().getFileUsageDao().getFileUsageById(fileUsage.getFileUsageId()) == null)
                AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
        }
        return result;
    }

    public static boolean notifyServiceMessageReceived(Entities.ServiceMessage message) {
        if (AsemanDB.getInstance().getMessageDao().getMessageById(message.getMessageId()) == null) {
            AsemanDB.getInstance().getMessageDao().insert(message);
            return true;
        } else {
            AsemanDB.getInstance().getMessageDao().update(message);
            return false;
        }
    }

    public static long getUnreadMessagesCount(long roomId) {
        Entities.User me = getMe();
        if (me != null) {
            return AsemanDB.getInstance().getMessageDao().getUnreadMessagesCount(me.getBaseUserId(), roomId);
        } else {
            return 0;
        }
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
            Entities.PhotoMessage msg = (Entities.PhotoMessage) message;
            msg.setPhotoId(fileId);
            AsemanDB.getInstance().getMessageDao().update(msg);
        } else if (message instanceof Entities.AudioMessage) {
            Entities.AudioMessage msg = (Entities.AudioMessage) message;
            msg.setAudioId(fileId);
            AsemanDB.getInstance().getMessageDao().update(msg);
        } else if (message instanceof Entities.VideoMessage) {
            Entities.VideoMessage msg = (Entities.VideoMessage) message;
            msg.setVideoId(fileId);
            AsemanDB.getInstance().getMessageDao().update(msg);
        }
        Entities.FileUsage fileUsage = new Entities.FileUsage();
        fileUsage.setFileUsageId(fileUsageId);
        fileUsage.setFileId(fileId);
        fileUsage.setRoomId(message.getRoomId());
        AsemanDB.getInstance().getFileUsageDao().insert(fileUsage);
    }

    public static void notifyFileUploaded(long fileId) {
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(fileId);
        if (fileLocal != null) {
            fileLocal.setTransferring(false);
            AsemanDB.getInstance().getFileLocalDao().update(fileLocal);
        }
    }

    public static Uploading notifyUploadingCreated(Uploading uploading) {
        uploading.setUploadingId(AsemanDB.getInstance().getUploadingDao().insert(uploading));
        return uploading;
    }

    public static void notifyUploadingUpdated(Uploading uploading) {
        AsemanDB.getInstance().getUploadingDao().update(uploading);
    }

    public static List<Uploading> fetchUploadings() {
        return AsemanDB.getInstance().getUploadingDao().getUploadings();
    }

    public static Uploading getUploadingById(long uploadingId) {
        return AsemanDB.getInstance().getUploadingDao().getUploadingById(uploadingId);
    }

    public static void deleteUploadingById(long uploadingId) {
        AsemanDB.getInstance().getUploadingDao().deleteUploadingById(uploadingId);
    }

    public static Downloading notifyDownloadingCreated(Downloading downloading) {
        downloading.setDownloadingId(AsemanDB.getInstance().getDownloadingDao().insert(downloading));
        return downloading;
    }

    public static void notifyDownloadingUpdated(Downloading downloading) {
        AsemanDB.getInstance().getDownloadingDao().update(downloading);
    }

    public static List<Downloading> fetchDownloadings() {
        return AsemanDB.getInstance().getDownloadingDao().getDownloadings();
    }

    public static void deleteDownloadingById(long downloadingId) {
        AsemanDB.getInstance().getDownloadingDao().deleteUploadingById(downloadingId);
    }

    public static TextMessageSending notifyMessageSendingCreated(TextMessageSending sending) {
        sending.setSendingId(AsemanDB.getInstance().getMessageSendingDao().insert(sending));
        return sending;
    }

    public static List<TextMessageSending> getTextMessageSendings() {
        return AsemanDB.getInstance().getMessageSendingDao().getTextMessageSendings();
    }

    public static void notifyTextMessageSendingDeleted(long sendingId) {
        AsemanDB.getInstance().getMessageSendingDao().deleteTextMessageSendingById(sendingId);
    }

    public static FileMessageSending notifyMessageSendingCreated(FileMessageSending sending) {
        sending.setSendingId(AsemanDB.getInstance().getMessageSendingDao().insert(sending));
        return sending;
    }

    public static List<FileMessageSending> getFileMessageSendings() {
        return AsemanDB.getInstance().getMessageSendingDao().getFileMessageSendings();
    }

    public static void notifyFileMessageSendingDeleted(long sendingId) {
        AsemanDB.getInstance().getMessageSendingDao().deleteFileMessageSendingById(sendingId);
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

    public static List<Entities.FileUsage> getFileUsages(long fileId) {
        return AsemanDB.getInstance().getFileUsageDao().getFileUsagesOfFile(fileId);
    }

    public static void notifyFileRegistered(long localFileId, long onlineFileId) {
        Entities.File file = AsemanDB.getInstance().getFileDao().getFileById(localFileId);
        if (file != null) {
            if (file instanceof Entities.Photo) {
                AsemanDB.getInstance().getFileDao().delete((Entities.Photo) file);
                file.setFileId(onlineFileId);
                AsemanDB.getInstance().getFileDao().insert((Entities.Photo) file);
            } else if (file instanceof Entities.Audio) {
                AsemanDB.getInstance().getFileDao().delete((Entities.Audio) file);
                file.setFileId(onlineFileId);
                AsemanDB.getInstance().getFileDao().insert((Entities.Audio) file);
            } else if (file instanceof Entities.Video) {
                AsemanDB.getInstance().getFileDao().delete((Entities.Video) file);
                file.setFileId(onlineFileId);
                AsemanDB.getInstance().getFileDao().insert((Entities.Video) file);
            }
        }
        Entities.FileLocal fileLocal = AsemanDB.getInstance().getFileLocalDao().getFileLocalById(localFileId);
        if (fileLocal != null) {
            AsemanDB.getInstance().getFileLocalDao().delete(fileLocal);
            fileLocal.setFileId(onlineFileId);
            AsemanDB.getInstance().getFileLocalDao().insert(fileLocal);
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
                LogHelper.log("Aseman", "Clearing cancelled download temp");
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
            LogHelper.log("Aseman", NetworkHelper.getMapper().writeValueAsString(messages));
            LogHelper.log("Aseman", NetworkHelper.getMapper().writeValueAsString(localMessages));
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

    public static void deleteFile(long fileId) {
        AsemanDB.getInstance().getFileDao().deleteFileById(fileId);
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

    public static Entities.MessageLocal getMessageLocalById(long messageId) {
        return AsemanDB.getInstance().getMessageLocalDao().getMessageLocalById(messageId);
    }

    public static List<Entities.Message> getMessages(long roomId) {
        Entities.BaseRoom room = getRoomById(roomId);
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
        return messages;
    }

    public static List<Entities.Invite> getMyInvites() {
        Entities.User user = DatabaseHelper.getMe();
        if (user != null) {
            List<Entities.Invite> invites = AsemanDB.getInstance().getInviteDao().getUserInvites(user.getBaseUserId());
            for (Entities.Invite invite : invites) {
                invite.setUser(AsemanDB.getInstance().getUserDao().getUserById(invite.getUserId()));
                invite.setComplex(AsemanDB.getInstance().getComplexDao().getComplexById(invite.getComplexId()));
            }
            return invites;
        }
        return new ArrayList<>();
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
        if (AsemanDB.getInstance().getInviteDao().getInviteByComplexIdAndUserId(invite.getComplexId(), invite.getUserId()) == null)
            AsemanDB.getInstance().getInviteDao().insert(invite);
        else
            AsemanDB.getInstance().getInviteDao().update(invite);
    }

    public static boolean doesInviteExist(long complexId, long userId) {
        return AsemanDB.getInstance().getInviteDao().getInviteByComplexIdAndUserId(complexId, userId) != null;
    }

    public static Entities.Invite getInviteByComplexAndUserId(long userId, long complexId) {
        return AsemanDB.getInstance().getInviteDao().getInviteByComplexIdAndUserId(complexId, userId);
    }

    public static void notifyInviteSent(Entities.Invite invite) {
        if (AsemanDB.getInstance().getInviteDao().getInviteByComplexIdAndUserId(invite.getComplexId(), invite.getUserId()) == null)
            AsemanDB.getInstance().getInviteDao().insert(invite);
        else
            AsemanDB.getInstance().getInviteDao().update(invite);
    }

    public static void notifyInviteResolved(Entities.Invite invite) {
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
        List<Entities.BaseRoom> rooms = AsemanDB.getInstance().getRoomDao()
                .getRoomsByComplexesIds(new ArrayList<>(complexIds));
        Hashtable<Long, Entities.Complex> complexTable = new Hashtable<>();
        for (Entities.Complex complex : complexes) {
            complex.setRooms(new ArrayList<>());
            complex.setSingleRooms(new ArrayList<>());
            complexTable.put(complex.getComplexId(), complex);
        }
        Hashtable<Long, Entities.User> userTable = new Hashtable<>();
        for (Entities.User user : users) {
            userTable.put(user.getBaseUserId(), user);
        }
        for (Entities.BaseRoom room : rooms) {
            room.setLastAction(AsemanDB.getInstance().getMessageDao().getLastAction(room.getRoomId()));
            if (room.getLastAction() != null)
                room.getLastAction().setAuthor(getBaseUserById(room.getLastAction().getAuthorId()));
            if (room instanceof Entities.Room)
                complexTable.get(room.getComplexId()).getAllRooms().add(((Entities.Room) room));
            else if (room instanceof Entities.SingleRoom)
                complexTable.get(room.getComplexId()).getSingleRooms().add(((Entities.SingleRoom) room));
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
