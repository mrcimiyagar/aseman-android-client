package kasper.android.pulse.models.extras;

import kasper.android.pulse.callbacks.network.OnFileDownloadListener;
import kasper.android.pulse.models.entities.Entities;

public class Downloading {

    private Entities.File file;
    private long roomId;
    private ProgressListener progressListener;
    private OnFileDownloadListener downloadListener;

    public Entities.File getFile() {
        return file;
    }

    public void setFile(Entities.File file) {
        this.file = file;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public ProgressListener getProgressListener() {
        return progressListener;
    }

    public void setProgressListener(ProgressListener progressListener) {
        this.progressListener = progressListener;
    }

    public OnFileDownloadListener getDownloadListener() {
        return downloadListener;
    }

    public void setDownloadListener(OnFileDownloadListener downloadListener) {
        this.downloadListener = downloadListener;
    }
}
