package kasper.android.pulse.helpers;

import java.util.Hashtable;

import kasper.android.pulseframework.components.PulseView;

public class PulseHelper {

    private static long currentComplexId;
    private static long currentRoomId;
    private static Hashtable<Long, PulseView> pulseViewTable = new Hashtable<>();
    private static Hashtable<Long, PulseView> pulseViewTablePreviews = new Hashtable<>();
    private static boolean onPreviewMode = false;

    public static void setOnPreviewMode(boolean onPreviewMode) {
        PulseHelper.onPreviewMode = onPreviewMode;
    }

    public static boolean isOnPreviewMode() {
        return onPreviewMode;
    }

    public static void setPulseViewTablePreviews(Hashtable<Long, PulseView> pulseViewTablePreviews) {
        PulseHelper.pulseViewTablePreviews = pulseViewTablePreviews;
    }

    public static Hashtable<Long, PulseView> getPulseViewTablePreviews() {
        return pulseViewTablePreviews;
    }

    public static long getCurrentComplexId() {
        return currentComplexId;
    }

    public static void setCurrentComplexId(long currentComplexId) {
        PulseHelper.currentComplexId = currentComplexId;
    }

    public static long getCurrentRoomId() {
        return currentRoomId;
    }

    public static void setCurrentRoomId(long currentRoomId) {
        PulseHelper.currentRoomId = currentRoomId;
    }

    public static Hashtable<Long, PulseView> getPulseViewTable() {
        return pulseViewTable;
    }

    public static void setPulseViewTable(Hashtable<Long, PulseView> pulseViewTable) {
        PulseHelper.pulseViewTable = pulseViewTable;
    }
}
