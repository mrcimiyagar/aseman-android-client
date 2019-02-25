package kasper.android.pulse.models.entities;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;
import java.util.List;

import kasper.android.pulse.models.notifications.Notifications;

/**
 * Created by keyhan1376 on 2/6/2018
 */

public class Entities {

    @Entity
    public static class Session implements Serializable {
        @PrimaryKey
        private long sessionId;
        private String token;
        private long baseUserId;
        @Ignore
        private BaseUser baseUser;
        private boolean online;
        private String connectionId;
        @Ignore
        private List<Notifications.Notification> notifications;

        public long getSessionId() {
            return sessionId;
        }

        public void setSessionId(long sessionId) {
            this.sessionId = sessionId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public long getBaseUserId() {
            return baseUserId;
        }

        public void setBaseUserId(long baseUserId) {
            this.baseUserId = baseUserId;
        }

        public BaseUser getBaseUser() {
            return baseUser;
        }

        public void setBaseUser(BaseUser baseUser) {
            this.baseUser = baseUser;
        }

        public boolean isOnline() {
            return online;
        }

        public void setOnline(boolean online) {
            this.online = online;
        }

        public String getConnectionId() {
            return connectionId;
        }

        public void setConnectionId(String connectionId) {
            this.connectionId = connectionId;
        }

        public List<Notifications.Notification> getNotifications() {
            return notifications;
        }

        public void setNotifications(List<Notifications.Notification> notifications) {
            this.notifications = notifications;
        }
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = User.class, name = "User"),
            @JsonSubTypes.Type(value = Bot.class, name = "Bot")
    })
    @Entity
    public static class BaseUser implements Serializable {
        @PrimaryKey
        private long baseUserId;
        private String title;
        private long avatar;
        @Ignore
        private List<Session> sessions;
        private String type;

        public long getBaseUserId() {
            return baseUserId;
        }

        public void setBaseUserId(long baseUserId) {
            this.baseUserId = baseUserId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getAvatar() {
            return avatar;
        }

        public void setAvatar(long avatar) {
            this.avatar = avatar;
        }

        public List<Session> getSessions() {
            return sessions;
        }

        public void setSessions(List<Session> sessions) {
            this.sessions = sessions;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    @Entity
    public static class Bot extends BaseUser implements Serializable {
        private String description;
        private String viewURL;
        @Ignore
        private BotSecret botSecret;

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getViewURL() {
            return viewURL;
        }

        public void setViewURL(String viewURL) {
            this.viewURL = viewURL;
        }

        public BotSecret getBotSecret() {
            return botSecret;
        }

        public void setBotSecret(BotSecret botSecret) {
            this.botSecret = botSecret;
        }
    }

    @Entity
    public static class BotCreation implements Serializable {
        @PrimaryKey
        private long botCreationId;
        private long botId;
        @Ignore
        private Bot bot;
        private long creatorId;
        @Ignore
        private User creator;

        public long getBotCreationId() {
            return botCreationId;
        }

        public void setBotCreationId(long botCreationId) {
            this.botCreationId = botCreationId;
        }

        public long getBotId() {
            return botId;
        }

        public void setBotId(long botId) {
            this.botId = botId;
        }

        public Bot getBot() {
            return bot;
        }

        public void setBot(Bot bot) {
            this.bot = bot;
        }

        public long getCreatorId() {
            return creatorId;
        }

        public void setCreatorId(long creatorId) {
            this.creatorId = creatorId;
        }

        public User getCreator() {
            return creator;
        }

        public void setCreator(User creator) {
            this.creator = creator;
        }
    }

    @Entity
    public static class BotSubscription implements Serializable {
        @PrimaryKey
        private long botSubscriptionId;
        private long botId;
        @Ignore
        private Bot bot;
        private long subscriberId;
        @Ignore
        private User subscriber;

        public long getBotSubscriptionId() {
            return botSubscriptionId;
        }

        public void setBotSubscriptionId(long botSubscriptionId) {
            this.botSubscriptionId = botSubscriptionId;
        }

        public long getBotId() {
            return botId;
        }

        public void setBotId(long botId) {
            this.botId = botId;
        }

        public Bot getBot() {
            return bot;
        }

        public void setBot(Bot bot) {
            this.bot = bot;
        }

        public long getSubscriberId() {
            return subscriberId;
        }

        public void setSubscriberId(long subscriberId) {
            this.subscriberId = subscriberId;
        }

        public User getSubscriber() {
            return subscriber;
        }

        public void setSubscriber(User subscriber) {
            this.subscriber = subscriber;
        }
    }

    @Entity
    public static class BotSecret implements Serializable {
        @PrimaryKey
        private long botSecretId;
        private String token;
        private long creatorId;
        @Ignore
        private User creator;
        private long botId;
        @Ignore
        private Bot bot;

        public long getBotSecretId() {
            return botSecretId;
        }

        public void setBotSecretId(long botSecretId) {
            this.botSecretId = botSecretId;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public long getCreatorId() {
            return creatorId;
        }

        public void setCreatorId(long creatorId) {
            this.creatorId = creatorId;
        }

        public User getCreator() {
            return creator;
        }

        public void setCreator(User creator) {
            this.creator = creator;
        }

        public long getBotId() {
            return botId;
        }

        public void setBotId(long botId) {
            this.botId = botId;
        }

        public Bot getBot() {
            return bot;
        }

        public void setBot(Bot bot) {
            this.bot = bot;
        }
    }

    @Entity
    public static class User extends BaseUser implements Serializable {
        @Ignore
        private List<Membership> memberships;
        @Ignore
        private List<Contact> contacts;
        @Ignore
        private List<Contact> peereds;
        @Ignore
        private List<Invite> invites;
        @Ignore
        private List<BotCreation> createdBots;
        @Ignore
        private List<BotSubscription> subscribedBots;
        @Ignore
        private UserSecret userSecret;

        public List<Membership> getMemberships() {
            return memberships;
        }

        public void setMemberships(List<Membership> memberships) {
            this.memberships = memberships;
        }

        public List<Contact> getContacts() {
            return contacts;
        }

        public void setContacts(List<Contact> contacts) {
            this.contacts = contacts;
        }

        public List<Contact> getPeereds() {
            return peereds;
        }

        public void setPeereds(List<Contact> peereds) {
            this.peereds = peereds;
        }

        public List<Invite> getInvites() {
            return invites;
        }

        public void setInvites(List<Invite> invites) {
            this.invites = invites;
        }

        public List<BotCreation> getCreatedBots() {
            return createdBots;
        }

        public void setCreatedBots(List<BotCreation> createdBots) {
            this.createdBots = createdBots;
        }

        public List<BotSubscription> getSubscribedBots() {
            return subscribedBots;
        }

        public void setSubscribedBots(List<BotSubscription> subscribedBots) {
            this.subscribedBots = subscribedBots;
        }

        public UserSecret getUserSecret() {
            return userSecret;
        }

        public void setUserSecret(UserSecret userSecret) {
            this.userSecret = userSecret;
        }
    }

    @Entity
    public static class UserSecret implements Serializable {
        @PrimaryKey
        private long userSecretId;
        private long homeId;
        @Ignore
        private Complex home;
        private String email;
        private long userId;
        @Ignore
        private User user;

        public long getUserSecretId() {
            return userSecretId;
        }

        public void setUserSecretId(long userSecretId) {
            this.userSecretId = userSecretId;
        }

        public long getHomeId() {
            return homeId;
        }

        public void setHomeId(long homeId) {
            this.homeId = homeId;
        }

        public Complex getHome() {
            return home;
        }

        public void setHome(Complex home) {
            this.home = home;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    @Entity
    public static class Contact implements Serializable {
        @PrimaryKey
        private long contactId;

        private long complexId;
        @Ignore
        private Complex complex;

        private long userId;
        @Ignore
        private User user;

        private long peerId;
        @Ignore
        private User peer;

        public long getContactId() {
            return contactId;
        }

        public void setContactId(long contactId) {
            this.contactId = contactId;
        }

        public long getComplexId() {
            return complexId;
        }

        public void setComplexId(long complexId) {
            this.complexId = complexId;
        }

        public Complex getComplex() {
            return complex;
        }

        public void setComplex(Complex complex) {
            this.complex = complex;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public long getPeerId() {
            return peerId;
        }

        public void setPeerId(long peerId) {
            this.peerId = peerId;
        }

        public User getPeer() {
            return peer;
        }

        public void setPeer(User peer) {
            this.peer = peer;
        }
    }

    @Entity
    public static class Invite implements Serializable {
        @PrimaryKey
        private long inviteId;
        private long complexId;
        @Ignore
        private Complex complex;
        private long userId;
        @Ignore
        private User user;

        public long getInviteId() {
            return inviteId;
        }

        public void setInviteId(long inviteId) {
            this.inviteId = inviteId;
        }

        public long getComplexId() {
            return complexId;
        }

        public void setComplexId(long complexId) {
            this.complexId = complexId;
        }

        public Complex getComplex() {
            return complex;
        }

        public void setComplex(Complex complex) {
            this.complex = complex;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = Photo.class, name = "Photo"),
            @JsonSubTypes.Type(value = Audio.class, name = "Audio"),
            @JsonSubTypes.Type(value = Video.class, name = "Video")
    })
    @Entity
    public static class File implements Serializable, Cloneable {
        @PrimaryKey
        private long fileId;
        private long size;
        private boolean isPublic;
        @Ignore
        private List<FileUsage> fileUsages;

        public File clone() {
            try {
                super.clone();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            File file = new File();
            file.setFileId(this.fileId);
            file.setPublic(this.isPublic);
            file.setFileUsages(this.fileUsages);
            return file;
        }

        public long getFileId() {
            return fileId;
        }

        public void setFileId(long fileId) {
            this.fileId = fileId;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean isPublic() {
            return isPublic;
        }

        public void setPublic(boolean aPublic) {
            isPublic = aPublic;
        }

        public List<FileUsage> getFileUsages() {
            return fileUsages;
        }

        public void setFileUsages(List<FileUsage> fileUsages) {
            this.fileUsages = fileUsages;
        }
    }

    @Entity
    public static class FileUsage implements Serializable {
        @PrimaryKey
        private long fileUsageId;
        private long fileId;
        @Ignore
        private File file;
        private long roomId;
        @Ignore
        private Room room;

        public long getFileUsageId() {
            return fileUsageId;
        }

        public void setFileUsageId(long fileUsageId) {
            this.fileUsageId = fileUsageId;
        }

        public long getFileId() {
            return fileId;
        }

        public void setFileId(long fileId) {
            this.fileId = fileId;
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }
    }

    @Entity
    public static class Photo extends File {
        private int width;
        private int height;

        public Photo clone() {
            super.clone();
            Photo photo = new Photo();
            photo.setFileId(getFileId());
            photo.setPublic(isPublic());
            photo.setWidth(width);
            photo.setHeight(height);
            return photo;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    @Entity
    public static class Audio extends File {
        private String title;
        private long duration;

        public Audio clone() {
            super.clone();
            Audio audio = new Audio();
            audio.setFileId(getFileId());
            audio.setPublic(isPublic());
            audio.setTitle(title);
            audio.setDuration(duration);
            return audio;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }

    @Entity
    public static class Video extends File {
        private String title;
        private long duration;

        public Video clone() {
            super.clone();
            Video video = new Video();
            video.setFileId(getFileId());
            video.setPublic(isPublic());
            video.setTitle(title);
            video.setDuration(duration);
            return video;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }
    }

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = TextMessage.class, name = "TextMessage"),
            @JsonSubTypes.Type(value = PhotoMessage.class, name = "PhotoMessage"),
            @JsonSubTypes.Type(value = AudioMessage.class, name = "AudioMessage"),
            @JsonSubTypes.Type(value = VideoMessage.class, name = "VideoMessage"),
            @JsonSubTypes.Type(value = ServiceMessage.class, name = "ServiceMessage")
    })
    @Entity
    public static class Message implements Serializable {
        @PrimaryKey
        private long messageId;
        private long time;
        private long seenCount;
        private boolean seenByMe;
        private long authorId;
        @Ignore
        private BaseUser author;
        private long roomId;
        @Ignore
        private Room room;

        public Message clone() {
            Message message = new Message();
            message.setMessageId(messageId);
            message.setTime(time);
            message.setAuthorId(authorId);
            message.setAuthor(author);
            message.setRoomId(roomId);
            message.setRoom(room);
            return message;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public long getTime() {
            return time;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public long getSeenCount() {
            return seenCount;
        }

        public void setSeenCount(long seenCount) {
            this.seenCount = seenCount;
        }

        public boolean isSeenByMe() {
            return seenByMe;
        }

        public void setSeenByMe(boolean seenByMe) {
            this.seenByMe = seenByMe;
        }

        public long getAuthorId() {
            return authorId;
        }

        public void setAuthorId(long authorId) {
            this.authorId = authorId;
        }

        public BaseUser getAuthor() {
            return author;
        }

        public void setAuthor(BaseUser author) {
            this.author = author;
        }

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }
    }

    @Entity
    public static class TextMessage extends Message {
        private String text;

        @Override
        public Message clone() {
            super.clone();
            TextMessage message = new TextMessage();
            message.setMessageId(getMessageId());
            message.setTime(getTime());
            message.setAuthorId(getAuthorId());
            message.setAuthor(getAuthor());
            message.setRoomId(getRoomId());
            message.setRoom(getRoom());
            message.setText(text);
            return message;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @Entity
    public static class PhotoMessage extends Message {
        private long photoId;
        @Ignore
        private Photo photo;

        @Override
        public Message clone() {
            super.clone();
            PhotoMessage message = new PhotoMessage();
            message.setMessageId(getMessageId());
            message.setTime(getTime());
            message.setAuthorId(getAuthorId());
            message.setAuthor(getAuthor());
            message.setRoomId(getRoomId());
            message.setRoom(getRoom());
            message.setPhotoId(photoId);
            message.setPhoto(photo);
            return message;
        }

        public long getPhotoId() {
            return photoId;
        }

        public void setPhotoId(long photoId) {
            this.photoId = photoId;
        }

        public Photo getPhoto() {
            return photo;
        }

        public void setPhoto(Photo photo) {
            this.photo = photo;
        }
    }

    @Entity
    public static class AudioMessage extends Message {
        private long audioId;
        @Ignore
        private Audio audio;

        @Override
        public Message clone() {
            super.clone();
            AudioMessage message = new AudioMessage();
            message.setMessageId(getMessageId());
            message.setTime(getTime());
            message.setAuthorId(getAuthorId());
            message.setAuthor(getAuthor());
            message.setRoomId(getRoomId());
            message.setRoom(getRoom());
            message.setAudioId(audioId);
            message.setAudio(audio);
            return message;
        }

        public long getAudioId() {
            return audioId;
        }

        public void setAudioId(long audioId) {
            this.audioId = audioId;
        }

        public Audio getAudio() {
            return audio;
        }

        public void setAudio(Audio audio) {
            this.audio = audio;
        }
    }

    @Entity
    public static class VideoMessage extends Message {
        private long videoId;
        @Ignore
        private Video video;

        @Override
        public Message clone() {
            super.clone();
            VideoMessage message = new VideoMessage();
            message.setMessageId(getMessageId());
            message.setTime(getTime());
            message.setAuthorId(getAuthorId());
            message.setAuthor(getAuthor());
            message.setRoomId(getRoomId());
            message.setRoom(getRoom());
            message.setVideoId(videoId);
            message.setVideo(video);
            return message;
        }

        public long getVideoId() {
            return videoId;
        }

        public void setVideoId(long videoId) {
            this.videoId = videoId;
        }

        public Video getVideo() {
            return video;
        }

        public void setVideo(Video video) {
            this.video = video;
        }
    }

    @Entity
    public static class ServiceMessage extends Message {
        private String text;

        @Override
        public Message clone() {
            super.clone();
            ServiceMessage message = new ServiceMessage();
            message.setMessageId(getMessageId());
            message.setTime(getTime());
            message.setAuthorId(getAuthorId());
            message.setAuthor(getAuthor());
            message.setRoomId(getRoomId());
            message.setRoom(getRoom());
            message.setText(text);
            return message;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @Entity
    public static class Complex implements Serializable {
        @PrimaryKey
        private long complexId;
        private String title;
        private long avatar;
        private short mode;
        @Ignore
        private List<Membership> members;
        @Ignore
        private List<Room> rooms;
        @Ignore
        private List<Invite> invites;
        @Ignore
        private ComplexSecret complexSecret;

        public long getComplexId() {
            return complexId;
        }

        public void setComplexId(long complexId) {
            this.complexId = complexId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getAvatar() {
            return avatar;
        }

        public void setAvatar(long avatar) {
            this.avatar = avatar;
        }

        public short getMode() {
            return mode;
        }

        public void setMode(short mode) {
            this.mode = mode;
        }

        public List<Membership> getMembers() {
            return members;
        }

        public void setMembers(List<Membership> members) {
            this.members = members;
        }

        public List<Room> getRooms() {
            return rooms;
        }

        public void setRooms(List<Room> rooms) {
            this.rooms = rooms;
        }

        public List<Invite> getInvites() {
            return invites;
        }

        public void setInvites(List<Invite> invites) {
            this.invites = invites;
        }

        public ComplexSecret getComplexSecret() {
            return complexSecret;
        }

        public void setComplexSecret(ComplexSecret complexSecret) {
            this.complexSecret = complexSecret;
        }
    }

    @Entity
    public static class ComplexSecret implements Serializable {
        @PrimaryKey
        private long complexSecretId;
        private long complexId;
        @Ignore
        private Complex complex;
        private long adminId;
        @Ignore
        private User admin;

        public long getComplexSecretId() {
            return complexSecretId;
        }

        public void setComplexSecretId(long complexSecretId) {
            this.complexSecretId = complexSecretId;
        }

        public long getComplexId() {
            return complexId;
        }

        public void setComplexId(long complexId) {
            this.complexId = complexId;
        }

        public Complex getComplex() {
            return complex;
        }

        public void setComplex(Complex complex) {
            this.complex = complex;
        }

        public long getAdminId() {
            return adminId;
        }

        public void setAdminId(long adminId) {
            this.adminId = adminId;
        }

        public User getAdmin() {
            return admin;
        }

        public void setAdmin(User admin) {
            this.admin = admin;
        }
    }

    @Entity
    public static class Room implements Serializable {
        @PrimaryKey
        private long roomId;
        private String title;
        private long avatar;
        private long complexId;
        @Ignore
        private Complex complex;
        @Ignore
        private List<Workership> workers;
        @Ignore
        private List<Message> messages;
        @Ignore
        private List<FileUsage> files;
        @Ignore
        private Message lastAction;

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public long getAvatar() {
            return avatar;
        }

        public void setAvatar(long avatar) {
            this.avatar = avatar;
        }

        public long getComplexId() {
            return complexId;
        }

        public void setComplexId(long complexId) {
            this.complexId = complexId;
        }

        public Complex getComplex() {
            return complex;
        }

        public void setComplex(Complex complex) {
            this.complex = complex;
        }

        public List<Workership> getWorkers() {
            return workers;
        }

        public void setWorkers(List<Workership> workers) {
            this.workers = workers;
        }

        public List<Message> getMessages() {
            return messages;
        }

        public void setMessages(List<Message> messages) {
            this.messages = messages;
        }

        public List<FileUsage> getFiles() {
            return files;
        }

        public void setFiles(List<FileUsage> files) {
            this.files = files;
        }

        public Message getLastAction() {
            return lastAction;
        }

        public void setLastAction(Message lastAction) {
            this.lastAction = lastAction;
        }
    }

    @Entity
    public static class Membership implements Serializable {
        @PrimaryKey
        private long membershipId;
        private long userId;
        @Ignore
        private User user;
        private long complexId;
        @Ignore
        private Complex complex;

        public long getMembershipId() {
            return membershipId;
        }

        public void setMembershipId(long membershipId) {
            this.membershipId = membershipId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }

        public User getUser() {
            return user;
        }

        public void setUser(User user) {
            this.user = user;
        }

        public long getComplexId() {
            return complexId;
        }

        public void setComplexId(long complexId) {
            this.complexId = complexId;
        }

        public Complex getComplex() {
            return complex;
        }

        public void setComplex(Complex complex) {
            this.complex = complex;
        }
    }

    @Entity
    public static class Workership implements Serializable {
        @PrimaryKey
        private long workershipId;
        private long botId;
        private long roomId;
        @Ignore
        private Room room;
        private int posX;
        private int posY;
        private int width;
        private int height;

        public long getWorkershipId() {
            return workershipId;
        }

        public void setWorkershipId(long workershipId) {
            this.workershipId = workershipId;
        }

        public long getBotId() {
            return botId;
        }

        public void setBotId(long botId) {
            this.botId = botId;
        }

        public long getRoomId() {
            return roomId;
        }

        public void setRoomId(long roomId) {
            this.roomId = roomId;
        }

        public Room getRoom() {
            return room;
        }

        public void setRoom(Room room) {
            this.room = room;
        }

        public int getPosX() {
            return posX;
        }

        public void setPosX(int posX) {
            this.posX = posX;
        }

        public int getPosY() {
            return posY;
        }

        public void setPosY(int posY) {
            this.posY = posY;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }
    }

    @Entity
    public static class MessageLocal implements Serializable {
        @PrimaryKey
        private long messageId;
        private boolean sent;

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public boolean isSent() {
            return sent;
        }

        public void setSent(boolean sent) {
            this.sent = sent;
        }
    }

    @Entity
    public static class FileLocal implements Serializable, Cloneable {
        @PrimaryKey
        private long fileId;
        private String path;
        private int progress;
        private boolean transferring;

        @Override
        public FileLocal clone() {
            try {
                super.clone();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            FileLocal fileLocal = new FileLocal();
            fileLocal.setFileId(this.fileId);
            fileLocal.setTransferring(this.transferring);
            fileLocal.setProgress(this.progress);
            fileLocal.setPath(this.path);
            return fileLocal;
        }

        public long getFileId() {
            return fileId;
        }

        public void setFileId(long fileId) {
            this.fileId = fileId;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

        public boolean isTransferring() {
            return transferring;
        }

        public void setTransferring(boolean transferring) {
            this.transferring = transferring;
        }
    }

    public static class BotStoreHeader implements Serializable {
        private long botStoreHeaderId;
        private List<BotStoreBanner> banners;

        public long getBotStoreHeaderId() {
            return botStoreHeaderId;
        }

        public void setBotStoreHeaderId(long botStoreHeaderId) {
            this.botStoreHeaderId = botStoreHeaderId;
        }

        public List<BotStoreBanner> getBanners() {
            return banners;
        }

        public void setBanners(List<BotStoreBanner> banners) {
            this.banners = banners;
        }
    }

    public static class BotStoreBanner implements Serializable {
        private long botStoreBannerId;
        private long botId;
        private Bot bot;
        private String title;
        private String imagePath;

        public long getBotStoreBannerId() {
            return botStoreBannerId;
        }

        public void setBotStoreBannerId(long botStoreBannerId) {
            this.botStoreBannerId = botStoreBannerId;
        }

        public long getBotId() {
            return botId;
        }

        public void setBotId(long botId) {
            this.botId = botId;
        }

        public Bot getBot() {
            return bot;
        }

        public void setBot(Bot bot) {
            this.bot = bot;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImagePath() {
            return imagePath;
        }

        public void setImagePath(String imagePath) {
            this.imagePath = imagePath;
        }
    }

    public static class BotStoreSection implements Serializable {
        private long botStoreSectionId;
        private List<BotStoreBot> botStoreBots;

        public long getBotStoreSectionId() {
            return botStoreSectionId;
        }

        public void setBotStoreSectionId(long botStoreSectionId) {
            this.botStoreSectionId = botStoreSectionId;
        }

        public List<BotStoreBot> getBotStoreBots() {
            return botStoreBots;
        }

        public void setBotStoreBots(List<BotStoreBot> botStoreBots) {
            this.botStoreBots = botStoreBots;
        }
    }

    public static class BotStoreBot implements Serializable {
        private long botStoreBotId;
        private long botId;
        private Bot bot;
        private long botStoreSectionId;
        private BotStoreSection botStoreSection;

        public long getBotStoreBotId() {
            return botStoreBotId;
        }

        public void setBotStoreBotId(long botStoreBotId) {
            this.botStoreBotId = botStoreBotId;
        }

        public long getBotId() {
            return botId;
        }

        public void setBotId(long botId) {
            this.botId = botId;
        }

        public Bot getBot() {
            return bot;
        }

        public void setBot(Bot bot) {
            this.bot = bot;
        }

        public long getBotStoreSectionId() {
            return botStoreSectionId;
        }

        public void setBotStoreSectionId(long botStoreSectionId) {
            this.botStoreSectionId = botStoreSectionId;
        }

        public BotStoreSection getBotStoreSection() {
            return botStoreSection;
        }

        public void setBotStoreSection(BotStoreSection botStoreSection) {
            this.botStoreSection = botStoreSection;
        }
    }

    @Entity
    public static class IdKeeper implements Serializable {
        @PrimaryKey
        private long idKeeperId;
        private long localId;
        private long sessionId;

        public long getIdKeeperId() {
            return idKeeperId;
        }

        public void setIdKeeperId(long idKeeperId) {
            this.idKeeperId = idKeeperId;
        }

        public long getLocalId() {
            return localId;
        }

        public void setLocalId(long localId) {
            this.localId = localId;
        }

        public long getSessionId() {
            return sessionId;
        }

        public void setSessionId(long sessionId) {
            this.sessionId = sessionId;
        }
    }
}
