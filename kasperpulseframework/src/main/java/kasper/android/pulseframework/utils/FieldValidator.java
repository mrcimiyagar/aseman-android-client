package kasper.android.pulseframework.utils;

import java.util.List;

import kasper.android.pulseframework.models.Controls;

public class FieldValidator {

    public static boolean isFieldEmpty(String input) {
        return input == null || input.length() == 0;
    }

    public static boolean isFieldEmpty(int input) {
        return input == 0;
    }

    public static boolean isFieldEmpty(long input) {
        return input == 0;
    }

    public static boolean isFieldEmpty(float input) {
        return input == 0;
    }

    public static boolean isFieldEmpty(double input) {
        return input == 0;
    }

    public static boolean isFieldEmpty(Controls.PanelCtrl.LayoutType input) {
        return input == null;
    }

    public static boolean isFieldEmpty(Controls.ImageCtrl.ImageScaleType input) {
        return input == null || input == Controls.ImageCtrl.ImageScaleType.NONE;
    }

    public static boolean isFieldEmpty(Controls.TextCtrl.GravityType input) {
        return input == null || input == Controls.TextCtrl.GravityType.NONE;
    }

    public static boolean isFieldEmpty(List input) {
        return input == null || input.size() == 0;
    }
}
