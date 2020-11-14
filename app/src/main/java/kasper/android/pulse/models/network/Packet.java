package kasper.android.pulse.models.network;

import java.util.List;

import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.notifications.Notifications;

public class Packet {
    private String Status;
    private String Email;
    private String VerifyCode;
    private Entities.Session Session;
    private Entities.User User;
    private Entities.UserSecret UserSecret;
    private Entities.Contact Contact;
    private Entities.ServiceMessage ServiceMessage;
    private List<Entities.Contact> Contacts;
    private Entities.BaseRoom BaseRoom;
    private Entities.Complex Complex;
    private Entities.ComplexSecret ComplexSecret;
    private List<Entities.ComplexSecret> ComplexSecrets;
    private List<Entities.Workership> Workerships;
    private Entities.Workership Workership;
    private Entities.Bot Bot;
    private List<Entities.Bot> Bots;
    private List<Entities.Complex> Complexes;
    private List<Entities.BaseRoom> BaseRooms;
    private String SearchQuery;
    private List<Entities.User> Users;
    private List<Entities.Session> Sessions;
    private List<Entities.Membership> Memberships;
    private Entities.Membership membership;
    private List<Entities.File> Files;
    private List<Entities.Message> Messages;
    private Entities.File File;
    private Entities.Message Message;
    private Entities.TextMessage TextMessage;
    private Entities.PhotoMessage PhotoMessage;
    private Entities.AudioMessage AudioMessage;
    private Entities.VideoMessage VideoMessage;
    private Entities.BotStoreHeader BotStoreHeader;
    private List<Entities.BotStoreSection> BotStoreSections;
    private Entities.BotSubscription BotSubscription;
    private List<Entities.BotSubscription> botSubscriptions;
    private Entities.BotCreation BotCreation;
    private List<Entities.BotCreation> botCreations;
    private Entities.Invite Invite;
    private List<Entities.Invite> Invites;
    private Entities.FileUsage FileUsage;
    private Entities.BaseUser BaseUser;
    private Notifications.Notification Notif;
    private Entities.MemberAccess MemberAccess;
    private List<Entities.MemberAccess> MemberAccesses;
    private Boolean FetchNext;
    private String ControlId;
    private Boolean SingleRoomMode;
    private Boolean FileTransferFinished;
    private boolean BotWindowMode;

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public String getVerifyCode() {
        return VerifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        VerifyCode = verifyCode;
    }

    public Entities.Session getSession() {
        return Session;
    }

    public void setSession(Entities.Session session) {
        Session = session;
    }

    public Entities.User getUser() {
        return User;
    }

    public void setUser(Entities.User user) {
        User = user;
    }

    public Entities.UserSecret getUserSecret() {
        return UserSecret;
    }

    public void setUserSecret(Entities.UserSecret userSecret) {
        UserSecret = userSecret;
    }

    public Entities.Contact getContact() {
        return Contact;
    }

    public void setContact(Entities.Contact contact) {
        Contact = contact;
    }

    public Entities.ServiceMessage getServiceMessage() {
        return ServiceMessage;
    }

    public void setServiceMessage(Entities.ServiceMessage serviceMessage) {
        ServiceMessage = serviceMessage;
    }

    public List<Entities.Contact> getContacts() {
        return Contacts;
    }

    public void setContacts(List<Entities.Contact> contacts) {
        Contacts = contacts;
    }

    public Entities.BaseRoom getBaseRoom() {
        return BaseRoom;
    }

    public void setBaseRoom(Entities.BaseRoom baseRoom) {
        BaseRoom = baseRoom;
    }

    public Entities.Complex getComplex() {
        return Complex;
    }

    public void setComplex(Entities.Complex complex) {
        Complex = complex;
    }

    public Entities.ComplexSecret getComplexSecret() {
        return ComplexSecret;
    }

    public void setComplexSecret(Entities.ComplexSecret complexSecret) {
        ComplexSecret = complexSecret;
    }

    public List<Entities.ComplexSecret> getComplexSecrets() {
        return ComplexSecrets;
    }

    public void setComplexSecrets(List<Entities.ComplexSecret> complexSecrets) {
        this.ComplexSecrets = complexSecrets;
    }

    public List<Entities.Workership> getWorkerships() {
        return Workerships;
    }

    public void setWorkerships(List<Entities.Workership> workerships) {
        Workerships = workerships;
    }

    public Entities.Workership getWorkership() {
        return Workership;
    }

    public void setWorkership(Entities.Workership workership) {
        Workership = workership;
    }

    public Entities.Bot getBot() {
        return Bot;
    }

    public void setBot(Entities.Bot bot) {
        Bot = bot;
    }

    public List<Entities.Bot> getBots() {
        return Bots;
    }

    public void setBots(List<Entities.Bot> bots) {
        Bots = bots;
    }

    public List<Entities.Complex> getComplexes() {
        return Complexes;
    }

    public void setComplexes(List<Entities.Complex> complexes) {
        Complexes = complexes;
    }

    public List<Entities.BaseRoom> getBaseRooms() {
        return BaseRooms;
    }

    public void setBaseRooms(List<Entities.BaseRoom> baseRooms) {
        BaseRooms = baseRooms;
    }

    public String getSearchQuery() {
        return SearchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        SearchQuery = searchQuery;
    }

    public List<Entities.User> getUsers() {
        return Users;
    }

    public void setUsers(List<Entities.User> users) {
        Users = users;
    }

    public List<Entities.Session> getSessions() {
        return Sessions;
    }

    public void setSessions(List<Entities.Session> sessions) {
        Sessions = sessions;
    }

    public List<Entities.Membership> getMemberships() {
        return Memberships;
    }

    public void setMemberships(List<Entities.Membership> memberships) {
        Memberships = memberships;
    }

    public Entities.Membership getMembership() {
        return membership;
    }

    public void setMembership(Entities.Membership membership) {
        this.membership = membership;
    }

    public List<Entities.File> getFiles() {
        return Files;
    }

    public void setFiles(List<Entities.File> files) {
        Files = files;
    }

    public List<Entities.Message> getMessages() {
        return Messages;
    }

    public void setMessages(List<Entities.Message> messages) {
        Messages = messages;
    }

    public Entities.File getFile() {
        return File;
    }

    public void setFile(Entities.File file) {
        File = file;
    }

    public Entities.Message getMessage() {
        return Message;
    }

    public void setMessage(Entities.Message message) {
        this.Message = message;
    }

    public Entities.TextMessage getTextMessage() {
        return TextMessage;
    }

    public void setTextMessage(Entities.TextMessage textMessage) {
        TextMessage = textMessage;
    }

    public Entities.PhotoMessage getPhotoMessage() {
        return PhotoMessage;
    }

    public void setPhotoMessage(Entities.PhotoMessage photoMessage) {
        PhotoMessage = photoMessage;
    }

    public Entities.AudioMessage getAudioMessage() {
        return AudioMessage;
    }

    public void setAudioMessage(Entities.AudioMessage audioMessage) {
        AudioMessage = audioMessage;
    }

    public Entities.VideoMessage getVideoMessage() {
        return VideoMessage;
    }

    public void setVideoMessage(Entities.VideoMessage videoMessage) {
        VideoMessage = videoMessage;
    }

    public Entities.BotStoreHeader getBotStoreHeader() {
        return BotStoreHeader;
    }

    public void setBotStoreHeader(Entities.BotStoreHeader botStoreHeader) {
        BotStoreHeader = botStoreHeader;
    }

    public List<Entities.BotStoreSection> getBotStoreSections() {
        return BotStoreSections;
    }

    public void setBotStoreSections(List<Entities.BotStoreSection> botStoreSections) {
        BotStoreSections = botStoreSections;
    }

    public Entities.BotSubscription getBotSubscription() {
        return BotSubscription;
    }

    public void setBotSubscription(Entities.BotSubscription botSubscription) {
        BotSubscription = botSubscription;
    }

    public Entities.BotCreation getBotCreation() {
        return BotCreation;
    }

    public void setBotCreation(Entities.BotCreation botCreation) {
        BotCreation = botCreation;
    }

    public Entities.Invite getInvite() {
        return Invite;
    }

    public List<Entities.Invite> getInvites() {
        return Invites;
    }

    public void setInvites(List<Entities.Invite> invites) {
        Invites = invites;
    }

    public void setInvite(Entities.Invite invite) {
        Invite = invite;
    }

    public Entities.FileUsage getFileUsage() {
        return FileUsage;
    }

    public void setFileUsage(Entities.FileUsage fileUsage) {
        FileUsage = fileUsage;
    }

    public List<Entities.BotCreation> getBotCreations() {
        return botCreations;
    }

    public void setBotCreations(List<Entities.BotCreation> botCreations) {
        this.botCreations = botCreations;
    }

    public List<Entities.BotSubscription> getBotSubscriptions() {
        return botSubscriptions;
    }

    public void setBotSubscriptions(List<Entities.BotSubscription> botSubscriptions) {
        this.botSubscriptions = botSubscriptions;
    }

    public Entities.BaseUser getBaseUser() {
        return BaseUser;
    }

    public void setBaseUser(Entities.BaseUser baseUser) {
        BaseUser = baseUser;
    }

    public Notifications.Notification getNotif() {
        return Notif;
    }

    public void setNotif(Notifications.Notification notif) {
        Notif = notif;
    }

    public Entities.MemberAccess getMemberAccess() {
        return MemberAccess;
    }

    public void setMemberAccess(Entities.MemberAccess memberAccess) {
        this.MemberAccess = memberAccess;
    }

    public List<Entities.MemberAccess> getMemberAccesses() {
        return MemberAccesses;
    }

    public void setMemberAccesses(List<Entities.MemberAccess> memberAccesses) {
        this.MemberAccesses = memberAccesses;
    }

    public Boolean getFetchNext() {
        return FetchNext;
    }

    public void setFetchNext(Boolean fetchNext) {
        FetchNext = fetchNext;
    }

    public String getControlId() {
        return ControlId;
    }

    public void setControlId(String controlId) {
        ControlId = controlId;
    }

    public Boolean isSingleRoomMode() {
        return SingleRoomMode;
    }

    public void setSingleRoomMode(Boolean singleRoomMode) {
        this.SingleRoomMode = singleRoomMode;
    }

    public Boolean isFileTransferFinished() {
        return this.FileTransferFinished;
    }

    public void setFileTransferFinished(Boolean fileTransferFinished) {
        this.FileTransferFinished = fileTransferFinished;
    }

    public boolean isBotWindowMode() {
        return BotWindowMode;
    }

    public void setBotWindowMode(boolean botWindowMode) {
        BotWindowMode = botWindowMode;
    }
}
