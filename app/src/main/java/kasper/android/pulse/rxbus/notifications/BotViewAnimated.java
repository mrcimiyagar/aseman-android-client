package kasper.android.pulse.rxbus.notifications;

public class BotViewAnimated {

    private long complexId;
    private long roomId;
    private long botId;
    private String animData;
    private boolean batchData;
    private boolean botWindowMode;

    public BotViewAnimated(long complexId, long roomId, long botId, String animData, boolean batchData, boolean botWindowMode) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.botId = botId;
        this.animData = animData;
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

    public String getAnimData() {
        return animData;
    }

    public boolean isBatchData() {
        return batchData;
    }

    public boolean isBotWindowMode() {
        return botWindowMode;
    }

    public void setBotWindowMode(boolean botWindowMode) {
        this.botWindowMode = botWindowMode;
    }
}
