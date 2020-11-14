package kasper.android.pulse.rxbus.notifications;

public class BotViewDelivered {

    private long complexId;
    private long roomId;
    private long botId;
    private String data;
    private boolean botWindowMode;

    public BotViewDelivered(long complexId, long roomId, long botId, String data, boolean botWindowMode) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.botId = botId;
        this.data = data;
        this.botWindowMode = botWindowMode;
    }

    public long getBotId() {
        return botId;
    }

    public long getComplexId() {
        return complexId;
    }

    public long getRoomId() {
        return roomId;
    }

    public String getData() {
        return data;
    }

    public boolean isBotWindowMode() {
        return botWindowMode;
    }
}
