package kasper.android.pulse.rxbus.notifications;

public class FileRegistered {

    private long localFileId;
    private long onlineFileId;

    public FileRegistered(long localFileId, long onlineFileId) {
        this.localFileId = localFileId;
        this.onlineFileId = onlineFileId;
    }

    public long getLocalFileId() {
        return localFileId;
    }

    public long getOnlineFileId() {
        return onlineFileId;
    }
}
