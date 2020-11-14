package kasper.android.pulse.rxbus.notifications;

public class BotViewUpdated {

    private long complexId;
    private long roomId;
    private long botId;
    private String updateData;
    private boolean batchData;
    private boolean botWindowMode;

    public BotViewUpdated(long complexId, long roomId, long botId, String updateData, boolean batchData, boolean botWindowMode) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.botId = botId;
        this.updateData = updateData;
        this.batchData = batchData;
        this.botWindowMode = botWindowMode;
    }

    public long getComplexId() {
        return complexId;
    }

    public long getRoomId() {
        return roomId;
    }

    public long getBotId() {
        return botId;
    }

    public String getUpdateData() {
        return updateData;
    }

    public boolean isBatchData() {
        return batchData;
    }

    public boolean isBotWindowMode() {
        return botWindowMode;
    }
}
