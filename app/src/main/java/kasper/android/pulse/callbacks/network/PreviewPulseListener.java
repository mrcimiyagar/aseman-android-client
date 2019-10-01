package kasper.android.pulse.callbacks.network;

public interface PreviewPulseListener {
    void buildUi(long botId, String rawJson);
    void updateUi(long botId, String rawJson, boolean batch);
    void animateUi(long botId, String rawJson, boolean batch);
    void runCodeUi(long botId, String rawJson, boolean batch);
}
