package kasper.android.pulse.extras;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import kasper.android.pulse.helpers.GraphicHelper;

public class FeedDecoration extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int pos = parent.getChildAdapterPosition(view);
        if (pos == 0) {
            outRect.top = GraphicHelper.dpToPx(72);
        } else if (pos == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = GraphicHelper.dpToPx(64);
        }
    }
}
