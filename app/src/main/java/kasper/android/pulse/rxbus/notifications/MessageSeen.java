package kasper.android.pulse.rxbus.notifications;

public class MessageSeen {

    private long messageId;
    private long seenCount;

    public MessageSeen(long messageId, long seenCount) {
        this.messageId = messageId;
        this.seenCount = seenCount;
    }

    public long getMessageId() {
        return messageId;
    }

    public long getSeenCount() {
        return seenCount;
    }
}
