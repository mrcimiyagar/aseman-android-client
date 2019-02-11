package kasper.android.pulse.models.network;

public class Authorization {

    private long sessionId;
    private String token;

    public Authorization(long sessionId, String token) {
        this.sessionId = sessionId;
        this.token = token;
    }

    public long getSessionId() {
        return sessionId;
    }

    public String getToken() {
        return token;
    }
}
