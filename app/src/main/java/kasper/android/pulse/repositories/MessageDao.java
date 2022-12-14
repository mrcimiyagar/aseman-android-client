package kasper.android.pulse.repositories;

import android.util.Log;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import kasper.android.pulse.core.AsemanDB;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;

@Dao
public abstract class MessageDao {
    @Insert
    public abstract void insert(Entities.TextMessage... messages);
    @Insert
    public abstract void insert(Entities.PhotoMessage... messages);
    @Insert
    public abstract void insert(Entities.AudioMessage... messages);
    @Insert
    public abstract void insert(Entities.VideoMessage... messages);
    @Insert
    public abstract void insert(Entities.ServiceMessage... messages);
    @Update
    public abstract void update(Entities.TextMessage... messages);
    @Update
    public abstract void update(Entities.PhotoMessage... messages);
    @Update
    public abstract void update(Entities.AudioMessage... messages);
    @Update
    public abstract void update(Entities.VideoMessage... messages);
    @Update
    public abstract void update(Entities.ServiceMessage... messages);
    @Delete
    public abstract void delete(Entities.TextMessage... messages);
    @Delete
    public abstract void delete(Entities.PhotoMessage... messages);
    @Delete
    public abstract void delete(Entities.AudioMessage... messages);
    @Delete
    public abstract void delete(Entities.VideoMessage... messages);
    @Delete
    public abstract void delete(Entities.ServiceMessage... messages);
    @Query("delete from textmessage")
    abstract void deleteAllTextMessages();
    @Query("delete from photomessage")
    abstract void deleteAllPhotoMessages();
    @Query("delete from audiomessage")
    abstract void deleteAllAudioMessages();
    @Query("delete from videomessage")
    abstract void deleteAllVideoMessages();
    @Query("delete from servicemessage")
    abstract void deleteAllServiceMessages();
    @Query("select * from textmessage where messageId = :mId")
    public abstract Entities.TextMessage getTextMessageById(long mId);
    @Query("select * from textmessage where roomId = :roomId")
    public abstract List<Entities.TextMessage> getTextMessages(long roomId);
    @Query("select * from photomessage where messageId = :mId")
    public abstract Entities.PhotoMessage getPhotoMessageById(long mId);
    @Query("select * from photomessage where roomId = :roomId")
    public abstract List<Entities.PhotoMessage> getPhotoMessages(long roomId);
    @Query("select * from audiomessage where messageId = :mId")
    public abstract Entities.AudioMessage getAudioMessageById(long mId);
    @Query("select * from audiomessage where roomId = :roomId")
    public abstract List<Entities.AudioMessage> getAudioMessages(long roomId);
    @Query("select * from videomessage where messageId = :mId")
    public abstract Entities.VideoMessage getVideoMessageById(long mId);
    @Query("select * from videomessage where roomId = :roomId")
    public abstract List<Entities.VideoMessage> getVideoMessages(long roomId);
    @Query("select * from servicemessage where messageId = :mId")
    public abstract Entities.ServiceMessage getServiceMessageById(long mId);
    @Query("select * from servicemessage where roomId = :roomId")
    public abstract List<Entities.ServiceMessage> getServiceMessages(long roomId);
    @Query("delete from textmessage where messageId = :messageId")
    public abstract void deleteTextMessageById(long messageId);
    @Query("delete from photomessage where messageId = :messageId")
    public abstract void deletePhotoMessageById(long messageId);
    @Query("delete from audiomessage where messageId = :messageId")
    public abstract void deleteAudioMessageById(long messageId);
    @Query("delete from videomessage where messageId = :messageId")
    public abstract void deleteVideoMessageById(long messageId);
    @Query("delete from servicemessage where messageId = :messageId")
    public abstract void deleteServiceMessageById(long messageId);
    @Query("select * from textmessage where roomId = :roomId " +
            "and time = (select max(time) from textmessage where roomId = :roomId)")
    abstract Entities.TextMessage getLastTextMessage(long roomId);
    @Query("select * from photomessage where roomId = :roomId " +
            "and time = (select max(time) from photomessage where roomId = :roomId)")
    abstract Entities.PhotoMessage getLastPhotoMessage(long roomId);
    @Query("select * from audiomessage where roomId = :roomId " +
            "and time = (select max(time) from audiomessage where roomId = :roomId)")
    abstract Entities.AudioMessage getLastAudioMessage(long roomId);
    @Query("select * from videomessage where roomId = :roomId " +
            "and time = (select max(time) from videomessage where roomId = :roomId)")
    abstract Entities.VideoMessage getLastVideoMessage(long roomId);
    @Query("select * from servicemessage where roomId = :roomId " +
            "and time = (select max(time) from servicemessage where roomId = :roomId)")
    abstract Entities.ServiceMessage getLastServiceMessage(long roomId);
    @Query("select count(*) from textmessage where roomId = :roomId and authorId != :myId and seenByMe = 0")
    abstract long getUnreadTextMessagesCount(long myId, long roomId);
    @Query("select count(*) from photomessage where roomId = :roomId and authorId != :myId and seenByMe = 0")
    abstract long getUnreadPhotoMessagesCount(long myId, long roomId);
    @Query("select count(*) from audiomessage where roomId = :roomId and authorId != :myId and seenByMe = 0")
    abstract long getUnreadAudioMessagesCount(long myId, long roomId);
    @Query("select count(*) from videomessage where roomId = :roomId and authorId != :myId and seenByMe = 0")
    abstract long getUnreadVideoMessagesCount(long myId, long roomId);
    @Query("select count(*) from servicemessage where roomId = :roomId and authorId != :myId and seenByMe = 0")
    abstract long getUnreadServiceMessagesCount(long myId, long roomId);
    @Query("update textmessage set seenCount = :seenCount where messageId = :messageId")
    public abstract void updateTextMessageSeenCount(long messageId, long seenCount);
    @Query("update photomessage set seenCount = :seenCount where messageId = :messageId")
    public abstract void updatePhotoMessageSeenCount(long messageId, long seenCount);
    @Query("update audiomessage set seenCount = :seenCount where messageId = :messageId")
    public abstract void updateAudioMessageSeenCount(long messageId, long seenCount);
    @Query("update videomessage set seenCount = :seenCount where messageId = :messageId")
    public abstract void updateVideoMessageSeenCount(long messageId, long seenCount);
    @Query("update servicemessage set seenCount = :seenCount where messageId = :messageId")
    public abstract void updateServiceMessageSeenCount(long messageId, long seenCount);
    @Query("update textmessage set seenByMe = 1 where messageId = :messageId")
    public abstract void notifyTextMessageSeenByMe(long messageId);
    @Query("update photomessage set seenByMe = 1 where messageId = :messageId")
    public abstract void notifyPhotoMessageSeenByMe(long messageId);
    @Query("update audiomessage set seenByMe = 1 where messageId = :messageId")
    public abstract void notifyAudioMessageSeenByMe(long messageId);
    @Query("update videomessage set seenByMe = 1 where messageId = :messageId")
    public abstract void notifyVideoMessageSeenByMe(long messageId);
    @Query("update servicemessage set seenByMe = 1 where messageId = :messageId")
    public abstract void notifyServiceMessageSeenByMe(long messageId);
    @Transaction
    public Entities.Message getMessageById(long mId) {
        Entities.TextMessage textMessage = getTextMessageById(mId);
        if (textMessage != null)
            return textMessage;
        Entities.PhotoMessage photoMessage = getPhotoMessageById(mId);
        if (photoMessage != null)
            return photoMessage;
        Entities.AudioMessage audioMessage = getAudioMessageById(mId);
        if (audioMessage != null)
            return audioMessage;
        Entities.VideoMessage videoMessage = getVideoMessageById(mId);
        if (videoMessage != null)
            return videoMessage;
        Entities.ServiceMessage serviceMessage = getServiceMessageById(mId);
        if (serviceMessage != null)
            return serviceMessage;
        return null;
    }
    @Transaction
    public long getUnreadMessagesCount(long myId, long roomId) {
        return getUnreadTextMessagesCount(myId, roomId) +
                getUnreadPhotoMessagesCount(myId, roomId) +
                getUnreadAudioMessagesCount(myId, roomId) +
                getUnreadVideoMessagesCount(myId, roomId) +
                getUnreadServiceMessagesCount(myId, roomId);
    }
    @Transaction
    public List<Entities.Message> getMessages(long roomId) {
        List<Entities.Message> messages = new ArrayList<>();
        messages.addAll(getTextMessages(roomId));
        messages.addAll(getPhotoMessages(roomId));
        messages.addAll(getAudioMessages(roomId));
        messages.addAll(getVideoMessages(roomId));
        messages.addAll(getServiceMessages(roomId));
        Collections.sort(messages, (m1, m2) -> {
            long diff = m1.getTime() - m2.getTime();
            return diff > 0 ? 1 : diff == 0 ? 0 : -1;
        });
        return messages;
    }
    @Transaction
    public void deleteMessageById(long messageId) {
        deleteTextMessageById(messageId);
        deletePhotoMessageById(messageId);
        deleteAudioMessageById(messageId);
        deleteVideoMessageById(messageId);
        deleteServiceMessageById(messageId);
    }
    @Transaction
    public Entities.Message getLastAction(long roomId) {
        List<Entities.Message> messages = new ArrayList<>();
        Entities.Message textMessage = getLastTextMessage(roomId);
        if (textMessage != null) messages.add(textMessage);
        Entities.Message photoMessage = getLastPhotoMessage(roomId);
        if (photoMessage != null) messages.add(photoMessage);
        Entities.Message audioMessage = getLastAudioMessage(roomId);
        if (audioMessage != null) messages.add(audioMessage);
        Entities.Message videoMessage = getLastVideoMessage(roomId);
        if (videoMessage != null) messages.add(videoMessage);
        Entities.Message serviceMessage = getLastServiceMessage(roomId);
        if (serviceMessage != null) messages.add(serviceMessage);
        Collections.sort(messages, (m1, m2) -> {
            long diff = m1.getTime() - m2.getTime();
            return diff > 0 ? 1 : diff < 0 ? -1 : 0;
        });
        if (messages.size() > 0)
            return messages.get(messages.size() - 1);
        else
            return null;
    }
    @Transaction
    public void deleteAll() {
        deleteAllTextMessages();
        deleteAllPhotoMessages();
        deleteAllAudioMessages();
        deleteAllVideoMessages();
        deleteAllServiceMessages();
    }
}
