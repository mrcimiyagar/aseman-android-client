package kasper.android.pulse.components;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;

public class CustomRecyclerView extends RecyclerView {

    private int maxHeight;
    public int getMaxHeight() {
        return maxHeight;
    }
    public void setMaxHeight(int maxHeight) {
        this.maxHeight = maxHeight;
    }

    public CustomRecyclerView(Context context) {
        super(context);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (maxHeight > 0){
            int hSize = MeasureSpec.getSize(heightMeasureSpec);
            int hMode = MeasureSpec.getMode(heightMeasureSpec);

            switch (hMode){
                case MeasureSpec.AT_MOST:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.AT_MOST);
                    break;
                case MeasureSpec.UNSPECIFIED:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
                    break;
                case MeasureSpec.EXACTLY:
                    heightMeasureSpec = MeasureSpec.makeMeasureSpec(Math.min(hSize, maxHeight), MeasureSpec.EXACTLY);
                    break;
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
