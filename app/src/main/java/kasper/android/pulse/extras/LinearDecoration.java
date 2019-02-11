package kasper.android.pulse.extras;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class LinearDecoration extends RecyclerView.ItemDecoration {

    private int topOffset;
    private int bottomOffset;

    public LinearDecoration(int top, int bottom) {
        this.topOffset = top;
        this.bottomOffset = bottom;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);

        if (position == 0) {
            outRect.top = this.topOffset;
        }
        else {
            outRect.top = 0;
        }

        if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = this.bottomOffset;
        }
        else {
            outRect.bottom = 0;
        }
    }
}