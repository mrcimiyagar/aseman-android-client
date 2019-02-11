package kasper.android.pulse.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class PhotoView extends androidx.appcompat.widget.AppCompatImageView {

    private int frameWidth;
    private int frameHeight;

    private Bitmap originalImage;
    private int imageOriginalWidth;
    private int imageOriginalHeight;

    private Bitmap image;

    private float imageX;
    private float imageY;
    private float imageWidth;
    private float imageHeight;

    private double startDistance;

    private CollagePoint startTempPoint;
    private CollageSize startImageSize;

    private boolean multiTouch = false;

    boolean recentlySelected;

    public PhotoView(Context context) {
        super(context);
        init();
    }

    public PhotoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

    }

    public void setFrameDimensions(int frameWidth, int frameHeight) {
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    public Bitmap getImage() {
        return this.image;
    }

    public void setImage(Bitmap image, int imageWidth, int imageHeight) {
        this.imageOriginalWidth = imageWidth;
        this.imageOriginalHeight = imageHeight;
        this.originalImage = Bitmap.createScaledBitmap(image, imageWidth, imageHeight, false);

        // ***
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;

        this.imageX = (this.frameWidth - this.imageOriginalWidth) / 2;
        this.imageY = (this.frameHeight - this.imageOriginalHeight) / 2;

        // ***
        this.multiTouch = false;
        updateTempImage();
        this.updateImageView();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                startTempPoint = new CollagePoint(event.getX(), event.getY());
                startImageSize = new CollageSize(imageWidth, imageHeight);
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                this.startDistance = Math.sqrt(Math.pow(event.getX(0) - event.getX(1), 2) + Math.pow(event.getY(0) - event.getY(1), 2));
                multiTouch = true;
                break;
            }
            case MotionEvent.ACTION_MOVE: {

                if (this.originalImage != null) {

                    if (event.getPointerCount() == 1 && !multiTouch) {

                        if (this.imageX + event.getX() - startTempPoint.getX() <= 0
                                && this.imageX + event.getX() - startTempPoint.getX()
                                + imageWidth >= getResources().getDisplayMetrics().widthPixels ) {
                            this.imageX += event.getX() - startTempPoint.getX();
                        }

                        if (this.imageY + event.getY() - startTempPoint.getY() <= 0
                                && this.imageY + event.getY() - startTempPoint.getY()
                                + imageHeight >= getResources().getDisplayMetrics().heightPixels) {
                            this.imageY += event.getY() - startTempPoint.getY();
                        }

                        startTempPoint = new CollagePoint(event.getX(), event.getY());

                        this.updateTempImage();

                        this.updateImageView();
                    } else if (event.getPointerCount() > 1) {

                        double newDistance = Math.sqrt(Math.pow(event.getX(0) - event.getX(1), 2) + Math.pow(event.getY(0) - event.getY(1), 2));

                        float ratio = (float) ((startImageSize.getWidth() / imageOriginalWidth) - 1 + (newDistance / startDistance));

                        if (ratio > 3) {
                            ratio = 3;
                        } else if (ratio < 1) {
                            ratio = 1;
                        }

                        CollageSize oldSize = new CollageSize(this.imageWidth, this.imageHeight);

                        this.imageWidth = (imageOriginalWidth * ratio);
                        this.imageHeight = (imageOriginalHeight * ratio);

                        this.imageX -= (this.imageWidth - oldSize.getWidth()) / 2;
                        this.imageY -= (this.imageHeight - oldSize.getHeight()) / 2;

                        if (this.imageX > 0) {
                            this.imageX = 0;
                        }

                        if (this.imageX + imageWidth < getResources().getDisplayMetrics().widthPixels) {
                            this.imageX = getResources().getDisplayMetrics().widthPixels - imageWidth;
                        }

                        if (this.imageY > 0) {
                            this.imageY = 0;
                        }

                        if (this.imageY + imageHeight < getResources().getDisplayMetrics().heightPixels) {
                            this.imageY = (getResources().getDisplayMetrics().heightPixels - imageHeight) / 2;
                        }

                        this.updateTempImage();

                        this.updateImageView();
                    }
                }

                break;
            }
            case MotionEvent.ACTION_UP: {

                if (recentlySelected) {
                    recentlySelected = false;
                }

                multiTouch = false;

                break;
            }
        }

        return true;
    }

    private void updateTempImage() {

        this.image = Bitmap.createBitmap(this.frameWidth, this.frameHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(this.image);
        canvas.drawBitmap(this.originalImage, new Rect(0, 0, this.imageOriginalWidth, this.imageOriginalHeight),
                new RectF(imageX, imageY, imageX + this.imageWidth, imageY + this.imageHeight), null);
    }

    private void updateImageView() {
        if (this.image != null) {
            final Bitmap result = Bitmap.createBitmap(this.frameWidth, this.frameHeight, Bitmap.Config.ARGB_8888);
            Canvas mCanvas = new Canvas(result);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
            mCanvas.drawBitmap(image, 0, 0, null);
            paint.setXfermode(null);
            this.setImageBitmap(result);
        }
    }

    private class CollagePoint {

        private float x;
        private float y;

        float getX() {
            return x;
        }

        float getY() {
            return y;
        }

        CollagePoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }

    private class CollageSize {

        private float width;
        private float height;

        float getWidth() {
            return width;
        }

        float getHeight() {
            return height;
        }

        CollageSize(float width, float height) {
            this.width = width;
            this.height = height;
        }
    }
}