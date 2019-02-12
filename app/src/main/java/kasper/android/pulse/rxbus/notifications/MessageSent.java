package kasper.android.pulse.rxbus.notifications;

public class MessageSent {

    private long localMessageId;
    private long onlineMessageId;

    public MessageSent(long localMessageId, long onlineMessageId) {
        this.localMessageId = localMessageId;
        this.onlineMessageId = onlineMessageId;
    }

    public long getLocalMessageId() {
        return localMessageId;
    }

    public long getOnlineMessageId() {
        return onlineMessageId;
    }
}
