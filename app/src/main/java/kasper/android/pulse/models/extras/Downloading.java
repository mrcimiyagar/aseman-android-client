package kasper.android.pulse.models.extras;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Downloading {

    @PrimaryKey
    private long downloadingId;
    private long fileId;
    private long roomId;

    public Downloading(long fileId, long roomId) {
        this.fileId = fileId;
        this.roomId = roomId;
    }

    public Downloading() {

    }

    public long getDownloadingId() {
        return downloadingId;
    }

    public void setDownloadingId(long downloadingId) {
        this.downloadingId = downloadingId;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
