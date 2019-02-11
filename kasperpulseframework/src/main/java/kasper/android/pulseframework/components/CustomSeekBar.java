package kasper.android.pulseframework.components;

import android.content.Context;
import android.util.AttributeSet;

public class CustomSeekBar extends androidx.appcompat.widget.AppCompatSeekBar {

    private int maxHeightDp;
    public void setMaxHeightDp(int dp) {
        this.maxHeightDp = dp;
    }

    public CustomSeekBar(Context context) {
        super(context);
    }

    public CustomSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeightDp != 0)
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeightDp, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
