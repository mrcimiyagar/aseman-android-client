package kasper.android.pulse.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

public class ComplexRecyclerView extends RelativeLayout {

    private LayoutInflater inflater;

    private ArrayList<ViewHolder> holdersList;

    private ArrayList<ViewHolder> visibleViews;

    private float pageHeight;

    private float roofY;

    private float floorY;

    private float lastMDY;

    public ComplexRecyclerView(Context context) {

        super(context);

        init(context);
    }

    public ComplexRecyclerView(Context context, AttributeSet attrs) {

        super(context, attrs);

        init(context);
    }

    public ComplexRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {

        super(context, attrs, defStyleAttr);

        init(context);
    }

    Iterator<ViewHolder> roofItemPointer;
    Iterator<ViewHolder> floorItemPointer;

    float screenSizeY;

    private void init(Context context) {

        this.inflater = LayoutInflater.from(context);

        this.holdersList = new ArrayList<>();

        this.roofItemPointer = this.holdersList.iterator();

        this.floorItemPointer = this.holdersList.iterator();

        this.visibleViews = new ArrayList<>();

        //this.roofItem = this.holdersList.listIterator();

        //this.floorItem = this.holdersList.listIterator();

        this.screenSizeY = context.getResources().getDisplayMetrics().heightPixels;

        this.roofY = 0;
        this.floorY = screenSizeY;

        this.pageHeight = screenSizeY;

        this.mGestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float dY) {

                ComplexRecyclerView.this.roofY += dY;
                ComplexRecyclerView.this.floorY += dY;

                if (ComplexRecyclerView.this.roofY < 0) {
                    ComplexRecyclerView.this.floorY = screenSizeY;
                    ComplexRecyclerView.this.roofY = 0;
                }
                else if (ComplexRecyclerView.this.floorY > pageHeight) {
                    ComplexRecyclerView.this.roofY = pageHeight - screenSizeY;
                    ComplexRecyclerView.this.floorY = pageHeight;
                }

                for (ViewHolder holder : ComplexRecyclerView.this.visibleViews) {
                    holder.getView().setY(holder.getY() - ComplexRecyclerView.this.roofY);
                }

                render();

                return super.onScroll(e1, e2, distanceX, dY);
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                return super.onFling(e1, e2, velocityX, velocityY);
            }
        });

        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {

                if (mGestureDetector.onTouchEvent(motionEvent)) {
                    return true;
                }

                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    return true;
                }

                return ComplexRecyclerView.super.onTouchEvent(motionEvent);

                /*if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {

                    lastMDY = motionEvent.getY();

                    return true;
                }
                else if (motionEvent.getAction() == MotionEvent.ACTION_MOVE) {



                    return true;
                }

                return false;*/
            }
        });
    }

    GestureDetector mGestureDetector;

    private boolean isViewVisible(ViewHolder holder) {

        return holder != null && (holder.getY() + holder.getHeight()) >= roofY && holder.getY() < floorY;
    }

    public void insertViewHolder(ViewHolder holder) {

        boolean done = false;

        if (this.holdersList.size() > 0) {

            ListIterator<ViewHolder> iterator = this.holdersList.listIterator();

            ViewHolder tempHolder;

            while (iterator.hasNext()) {

                tempHolder = iterator.next();

                if (tempHolder.getY() > holder.getY()) {

                    done = true;
                    iterator.previous();
                    iterator.add(holder);
                    break;
                }
            }
        }

        if (!done) {
            this.holdersList.add(holder);
        }

        ViewHolder lastHolder = this.holdersList.get(this.holdersList.size() - 1);

        this.pageHeight = Math.max(this.screenSizeY, lastHolder.getY() + lastHolder.getHeight());

        render();
    }

    public void deleteViewHolder(ViewHolder holder) {

        this.holdersList.remove(holder);

        ViewHolder lastHolder = this.holdersList.get(this.holdersList.size() - 1);

        this.pageHeight = Math.max(this.screenSizeY, lastHolder.getY() + lastHolder.getHeight());

        render();
    }

    public void render() {

        for (ViewHolder holder : this.visibleViews) {

            if (!this.isViewVisible(holder)) {
                this.removeView(holder.getView());
                holder.destroyView();
            }
        }

        this.visibleViews = new ArrayList<>();

        for (ViewHolder holder : this.holdersList) {

            if (this.isViewVisible(holder)) {

                this.visibleViews.add(holder);

                if (!holder.exists()) {

                    holder.notifyViewCreated(holder.onCreateView(inflater));

                    holder.getView().setLayoutParams(new LayoutParams(holder.getWidth(), holder.getHeight()));
                    holder.getView().setVisibility(GONE);
                    this.addView(holder.getView());
                    holder.getView().setX(holder.getX());
                    holder.getView().setY(holder.getY());
                    holder.getView().setVisibility(VISIBLE);
                }
            }
        }
    }

    public static abstract class ViewHolder {

        View view;

        public final boolean exists() { return this.view != null; }

        public abstract float getX();
        public abstract int getWidth();
        public abstract float getY();
        public abstract int getHeight();
        public abstract View onCreateView(LayoutInflater inflater);
        public final void notifyViewCreated(View view) { this.view = view; }
        public final View getView() { return this.view; }
        public final void destroyView() { this.view = null; }
    }
}