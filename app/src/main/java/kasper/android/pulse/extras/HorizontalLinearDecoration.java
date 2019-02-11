package kasper.android.pulse.extras;

import android.graphics.Rect;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

public class HorizontalLinearDecoration extends RecyclerView.ItemDecoration {

    private int leftOffset;
    private int rightOffset;

    public HorizontalLinearDecoration(int left, int right) {
        this.leftOffset = left;
        this.rightOffset = right;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

        super.getItemOffsets(outRect, view, parent, state);

        int position = parent.getChildAdapterPosition(view);

        if (position == 0) {
            outRect.left = this.leftOffset;
        }
        else {
            outRect.left = 0;
        }

        if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.right = this.rightOffset;
        }
        else {
            outRect.right = 0;
        }
    }
}