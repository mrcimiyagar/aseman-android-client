package kasper.android.pulse.models.extras;

import kasper.android.pulse.callbacks.network.OnFileUploadListener;
import kasper.android.pulse.models.entities.Entities;

public class Uploading {

    private Entities.File file;
    private String path;
    private long complexId;
    private long roomId;
    private ProgressListener progressListener;
    private OnFileUploadListener uploadListener;

    public Entities.File getFile() {
        return file;
    }

    public void setFile(Entities.File file) {
        this.file = file;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getComplexId() {
        return complexId;
    }

    public void setComplexId(long complexId) {
        this.complexId = complexId;
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

    public OnFileUploadListener getUploadListener() {
        return uploadListener;
    }

    public void setUploadListener(OnFileUploadListener uploadListener) {
        this.uploadListener = uploadListener;
    }
}
