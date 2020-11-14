package kasper.android.pulse.rxbus.notifications;

public class BotViewRanCommands {

    private long complexId;
    private long roomId;
    private long botId;
    private String commandsData;
    private boolean batchData;
    private boolean botWindowMode;

    public BotViewRanCommands(long complexId, long roomId, long botId, String commandsData, boolean batchData, boolean botWindowMode) {
        this.complexId = complexId;
        this.roomId = roomId;
        this.botId = botId;
        this.commandsData = commandsData;
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

    public String getCommandsData() {
        return commandsData;
    }

    public boolean isBatchData() {
        return batchData;
    }

    public boolean isBotWindowMode() {
        return botWindowMode;
    }
}
