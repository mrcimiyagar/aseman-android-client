package kasper.android.pulse.extras;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import android.view.View;

import kasper.android.pulse.callbacks.ui.OnPageTransformListener;

public class StackTransformer implements ViewPager.PageTransformer {

    private float screenDensity;
    private OnPageTransformListener onPageTransformListener = null;

    public StackTransformer(float screenDensity) {
        this.screenDensity = screenDensity;
    }

    public void setOnPageTransformListener(OnPageTransformListener onPageTransformListener) {
        this.onPageTransformListener = onPageTransformListener;
    }

    @Override
    public void transformPage(@NonNull View view, float position) {
        onPreTransform(view, position);
        onTransform(view, position);
        onPostTransform(view, position);
    }

    private boolean hideOffscreenPages() {
        return true;
    }

    private boolean isPagingEnabled() {
        return false;
    }

    private void onPreTransform(View view, float position) {
        final float width = view.getWidth();
        view.setRotationX(0);
        view.setRotationY(0);
        view.setRotation(0);
        view.setScaleX(1);
        view.setScaleY(1);
        view.setPivotX(0);
        view.setPivotY(0);
        view.setTranslationY(0);
        if (isPagingEnabled()) {
            view.setTranslationX(0f);
        }
        else {
            view.setTranslationX(-width * position);
        }
        if (hideOffscreenPages()) {
            view.setAlpha(position <= -1f || position >= 1f ? 0f : 1f);
        } else {
            view.setAlpha(1f);
        }
    }

    private void onTransform(View view, float position) {
        if (position >= 0) {
            view.setTranslationX(position);
            view.setElevation(8 * screenDensity);
        }
        else if (position < 0 && position > -1) {
            view.setElevation(0);
            view.setAlpha(position + 1.5f);
            if (this.onPageTransformListener != null) {
                this.onPageTransformListener.run(position);
            }
        }
    }

    private void onPostTransform(View view, float position) { }
}