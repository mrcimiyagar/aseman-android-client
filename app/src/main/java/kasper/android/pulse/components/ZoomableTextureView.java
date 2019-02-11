package kasper.android.pulse.components;

/**
 * Created by root on 10/28/17.
 */
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.TextureView;
import android.view.View;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.VideoPlayerActivity;

public class ZoomableTextureView extends TextureView {
    private static final String SUPERSTATE_KEY = "superState";
    private static final String MIN_SCALE_KEY = "minScale";
    private static final String MAX_SCALE_KEY = "maxScale";

    private int CLICK_ACTION_THRESHOLD = 200;
    private float startX;
    private float startY;

    private Context context;

    private float minScale = 1f;
    private float maxScale = 5f;
    private float saveScale = 1f;

    public void setMinScale(float scale) {
        if (scale < 1.0f || scale > maxScale)
            throw new RuntimeException("minScale can't be lower than 1 or larger than maxScale(" + maxScale + ")");
        else minScale = scale;
    }

    public void setMaxScale(float scale) {
        if (scale < 1.0f || scale < minScale)
            throw new RuntimeException("maxScale can't be lower than 1 or minScale(" + minScale + ")");
        else minScale = scale;
    }

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private static final int TOUCH = 3;
    private int mode = NONE;

    private Matrix matrix = new Matrix();
    private ScaleGestureDetector mScaleDetector;
    private float[] m;

    private PointF last = new PointF();
    private PointF start = new PointF();
    private float right, bottom;


    public ZoomableTextureView(Context context) {
        super(context);
        this.context = context;
        initView(null);
    }

    public ZoomableTextureView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView(attrs);
    }

    public ZoomableTextureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        initView(attrs);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SUPERSTATE_KEY, super.onSaveInstanceState());
        bundle.putFloat(MIN_SCALE_KEY, minScale);
        bundle.putFloat(MAX_SCALE_KEY, maxScale);
        return bundle;

    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            this.minScale = bundle.getInt(MIN_SCALE_KEY);
            this.minScale = bundle.getInt(MAX_SCALE_KEY);
            state = bundle.getParcelable(SUPERSTATE_KEY);
        }
        super.onRestoreInstanceState(state);
    }

    private void initView(AttributeSet attrs) {
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ZoomableTextureView,
                0, 0);
        try {
            minScale = a.getFloat(R.styleable.ZoomableTextureView_minScale, minScale);
            maxScale = a.getFloat(R.styleable.ZoomableTextureView_maxScale, maxScale);
        } finally {
            a.recycle();
        }

        setOnTouchListener(new ZoomOnTouchListeners());
    }

    private class ZoomOnTouchListeners implements OnTouchListener {
        public ZoomOnTouchListeners() {
            super();
            m = new float[9];
            mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {


            mScaleDetector.onTouchEvent(motionEvent);

            matrix.getValues(m);
            float x = m[Matrix.MTRANS_X];
            float y = m[Matrix.MTRANS_Y];
            PointF curr = new PointF(motionEvent.getX(), motionEvent.getY());

            switch (motionEvent.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startX = motionEvent.getX();
                    startY = motionEvent.getY();

                    last.set(motionEvent.getX(), motionEvent.getY());
                    start.set(last);
                    mode = DRAG;
                    break;
                case MotionEvent.ACTION_POINTER_DOWN:
                    last.set(motionEvent.getX(), motionEvent.getY());
                    start.set(last);
                    mode = ZOOM;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == ZOOM || (mode == DRAG && saveScale > minScale)) {
                        float deltaX = curr.x - last.x;// x difference
                        float deltaY = curr.y - last.y;// y difference
                        if (y + deltaY > 0)
                            deltaY = -y;
                        else if (y + deltaY < -bottom)
                            deltaY = -(y + bottom);

                        if (x + deltaX > 0)
                            deltaX = -x;
                        else if (x + deltaX < -right)
                            deltaX = -(x + right);
                        matrix.postTranslate(deltaX, deltaY);
                        last.set(curr.x, curr.y);
                    }
                    break;
                case MotionEvent.ACTION_POINTER_UP:
                    mode = NONE;
                    break;
                case MotionEvent.ACTION_UP:
                    float endX = motionEvent.getX();
                    float endY = motionEvent.getY();

                    if (isAClick(startX, endX, startY, endY)) {
                        // WE HAVE A CLICK!!
                        if (VideoPlayerActivity.switched) {
                            VideoPlayerActivity.changeControllerVisibility(true);
                        }else {
                            VideoPlayerActivity.changeControllerVisibility(false);
                        }
                        VideoPlayerActivity.switched = !VideoPlayerActivity.switched;
                    }
                    mode = NONE;
                    break;
            }
            ZoomableTextureView.this.setTransform(matrix);
            ZoomableTextureView.this.invalidate();
            return true;
        }
        private boolean isAClick(float startX, float endX, float startY, float endY) {
            float differenceX = Math.abs(startX - endX);
            float differenceY = Math.abs(startY - endY);
            return !(differenceX > CLICK_ACTION_THRESHOLD/* =5 */ || differenceY > CLICK_ACTION_THRESHOLD);
        }

        private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                mode = ZOOM;
                return true;
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float mScaleFactor = detector.getScaleFactor();
                float origScale = saveScale;
                saveScale *= mScaleFactor;
                if (saveScale > maxScale) {
                    saveScale = maxScale;
                    mScaleFactor = maxScale / origScale;
                } else if (saveScale < minScale) {
                    saveScale = minScale;
                    mScaleFactor = minScale / origScale;
                }
                right = getWidth() * saveScale - getWidth();
                bottom = getHeight() * saveScale - getHeight();
                if (0 <= getWidth() || 0 <= getHeight()) {
                    matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                    if (mScaleFactor < 1) {
                        matrix.getValues(m);
                        float x = m[Matrix.MTRANS_X];
                        float y = m[Matrix.MTRANS_Y];
                        if (mScaleFactor < 1) {
                            if (0 < getWidth()) {
                                if (y < -bottom)
                                    matrix.postTranslate(0, -(y + bottom));
                                else if (y > 0)
                                    matrix.postTranslate(0, -y);
                            } else {
                                if (x < -right)
                                    matrix.postTranslate(-(x + right), 0);
                                else if (x > 0)
                                    matrix.postTranslate(-x, 0);
                            }
                        }
                    }
                } else {
                    matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(), detector.getFocusY());
                    matrix.getValues(m);
                    float x = m[Matrix.MTRANS_X];
                    float y = m[Matrix.MTRANS_Y];
                    if (mScaleFactor < 1) {
                        if (x < -right)
                            matrix.postTranslate(-(x + right), 0);
                        else if (x > 0)
                            matrix.postTranslate(-x, 0);
                        if (y < -bottom)
                            matrix.postTranslate(0, -(y + bottom));
                        else if (y > 0)
                            matrix.postTranslate(0, -y);
                    }
                }
                return true;
            }
        }
    }

}