package kasper.android.pulse.helpers;

import android.content.Context;

import kasper.android.pulse.core.Core;

/**
 * Created by keyhan1376 on 3/9/2018.
 */

public class GraphicHelper {

    public static void setup(Context context) {

    }

    public static int dpToPx(float dp) {
        return (int)(dp * Core.getInstance().getResources().getDisplayMetrics().density);
    }

    public static int pxToDp(float px) {
        return (int)(px / Core.getInstance().getResources().getDisplayMetrics().density);
    }

    public static float getDensity() {
        return Core.getInstance().getResources().getDisplayMetrics().density;
    }

    public static int getScreenWidth() {
        return Core.getInstance().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenHeight() {
        return Core.getInstance().getResources().getDisplayMetrics().heightPixels;
    }
}
