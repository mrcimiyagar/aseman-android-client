package kasper.android.pulse.repositories;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.models.entities.Entities;

@Dao
public abstract class FileDao {
    @Insert
    public abstract void insert(Entities.Photo... photos);
    @Insert
    public abstract void insert(Entities.Audio... audios);
    @Insert
    public abstract void insert(Entities.Video... videos);
    @Update
    public abstract void update(Entities.Photo... photos);
    @Update
    public abstract void update(Entities.Audio... audios);
    @Update
    public abstract void update(Entities.Video... videos);
    @Delete
    public abstract void delete(Entities.Photo... photos);
    @Delete
    public abstract void delete(Entities.Audio... audios);
    @Delete
    public abstract void delete(Entities.Video... videos);
    @Query("select * from photo where fileId = :fId")
    public abstract Entities.Photo getPhotoById(long fId);
    @Query("select * from photo where fileId in (select fileId from fileusage where roomId = :roomId)")
    public abstract List<Entities.Photo> getPhotos(long roomId);
    @Query("select * from audio where fileId = :fId")
    public abstract Entities.Audio getAudioById(long fId);
    @Query("select * from audio where fileId in (select fileId from fileusage where roomId = :roomId)")
    public abstract List<Entities.Audio> getAudios(long roomId);
    @Query("select * from video where fileId = :fId")
    public abstract Entities.Video getVideoById(long fId);
    @Query("select * from video where fileId in (select fileId from fileusage where roomId = :roomId)")
    public abstract List<Entities.Video> getVideos(long roomId);
    @Query("select * from photo where fileId in (:fileIds)")
    public abstract List<Entities.Photo> getPhotosByIds(List<Long> fileIds);
    @Query("select * from audio where fileId in (:fileIds)")
    public abstract List<Entities.Audio> getAudiosByIds(List<Long> fileIds);
    @Query("select * from video where fileId in (:fileIds)")
    public abstract List<Entities.Video> getVideosByIds(List<Long> fileIds);
    @Transaction
    public Entities.File getFileById(long fId) {
        Entities.Photo photo = getPhotoById(fId);
        if (photo != null)
            return photo;
        Entities.Audio audio = getAudioById(fId);
        if (audio != null)
            return audio;
        Entities.Video video = getVideoById(fId);
        if (video != null)
            return video;
        return null;
    }
    @Transaction
    public List<Entities.File> getFiles(long roomId) {
        List<Entities.File> files = new ArrayList<>();
        files.addAll(getPhotos(roomId));
        files.addAll(getAudios(roomId));
        files.addAll(getVideos(roomId));
        return files;
    }
    @Transaction
    public List<Entities.File> getFilesByIds(List<Long> fileIds) {
        List<Entities.File> files = new ArrayList<>();
        files.addAll(getPhotosByIds(fileIds));
        files.addAll(getAudiosByIds(fileIds));
        files.addAll(getVideosByIds(fileIds));
        return files;
    }
}
