package kasper.android.pulse.models.extras;

import kasper.android.pulse.models.entities.Entities;

public class Downloading {

    private Entities.File file;
    private long roomId;

    public Downloading(Entities.File file, long roomId) {
        this.file = file;
        this.roomId = roomId;
    }

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
}
