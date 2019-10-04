package kasper.android.pulseframework.engines;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.graphics.drawable.shapes.RectShape;
import android.net.Uri;

import androidx.cardview.widget.CardView;
import androidx.core.widget.CompoundButtonCompat;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.HorizontalBarChartView;
import com.db.chart.view.HorizontalStackBarChartView;
import com.db.chart.view.LineChartView;
import com.db.chart.view.StackBarChartView;
import com.moos.library.CircleProgressView;
import com.moos.library.HorizontalProgressView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kasper.android.pulseframework.adapters.DropDownAdapter;
import kasper.android.pulseframework.adapters.RecyclerAdapter;
import kasper.android.pulseframework.components.CustomHashtable;
import kasper.android.pulseframework.components.CustomSeekBar;
import kasper.android.pulseframework.interfaces.IClickNotifier;
import kasper.android.pulseframework.interfaces.IMainThreadRunner;
import kasper.android.pulseframework.models.Data;
import kasper.android.pulseframework.models.Controls;
import kasper.android.pulseframework.models.Tuple;
import kasper.android.pulseframework.utils.FieldValidator;
import kasper.android.pulseframework.utils.GraphicsHelper;
import tcking.github.com.giraffeplayer2.VideoView;

public class UiInitiatorEngine {

    private Context context;
    private CustomHashtable<String, Pair<Controls.Control, View>> idTable;
    private IMainThreadRunner mainThreadRunner;
    private IClickNotifier clickNotifier;

    public UiInitiatorEngine(Context context, CustomHashtable<String, Pair<Controls.Control, View>> idTable
            , IMainThreadRunner mainThreadRunner, IClickNotifier clickNotifier) {
        this.context = context;
        this.idTable = idTable;
        this.mainThreadRunner = mainThreadRunner;
        this.clickNotifier = clickNotifier;
    }

    @SuppressLint("RtlHardcoded")
    public Tuple<View, List<Pair<Controls.Control, View>>
            , CustomHashtable<String, Pair<Controls.Control, View>>> buildViewTree(
            Controls.PanelCtrl.LayoutType parentLayoutType, Controls.Control control) {
        View result = null;
        List<Pair<Controls.Control, View>> statefulCtrls = new ArrayList<>();
        if (control instanceof Controls.PanelCtrl) {
            Controls.PanelCtrl panelCtrl = (Controls.PanelCtrl) control;
            if (!FieldValidator.isFieldEmpty(panelCtrl.getLayoutType())) {
                if (panelCtrl.getLayoutType() == Controls.PanelCtrl.LayoutType.RELATIVE) {
                    RelativeLayout relativeLayout = new RelativeLayout(context);
                    for (Controls.Control el : panelCtrl.getControls()) {
                        Tuple<View, List<Pair<Controls.Control, View>>
                                , CustomHashtable<String, Pair<Controls.Control, View>>> pair =
                                buildViewTree(panelCtrl.getLayoutType(), el);
                        View childView = pair.getItem1();
                        statefulCtrls.addAll(pair.getItem2());
                        idTable.putAll(pair.getItem3());
                        relativeLayout.addView(childView);
                    }
                    result = relativeLayout;
                } else if (panelCtrl.getLayoutType() == Controls.PanelCtrl.LayoutType.LINEAR_VERTICAL) {
                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(LinearLayout.VERTICAL);
                    for (Controls.Control el : panelCtrl.getControls()) {
                        Tuple<View, List<Pair<Controls.Control, View>>
                                , CustomHashtable<String, Pair<Controls.Control, View>>> pair =
                                buildViewTree(panelCtrl.getLayoutType(), el);
                        View childView = pair.getItem1();
                        statefulCtrls.addAll(pair.getItem2());
                        idTable.putAll(pair.getItem3());
                        linearLayout.addView(childView);
                    }
                    result = linearLayout;
                } else if (panelCtrl.getLayoutType() == Controls.PanelCtrl.LayoutType.LINEAR_HORIZONTAL) {
                    LinearLayout linearLayout = new LinearLayout(context);
                    linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                    for (Controls.Control el : panelCtrl.getControls()) {
                        Tuple<View, List<Pair<Controls.Control, View>>
                                , CustomHashtable<String, Pair<Controls.Control, View>>> pair =
                                buildViewTree(panelCtrl.getLayoutType(), el);
                        View childView = pair.getItem1();
                        statefulCtrls.addAll(pair.getItem2());
                        idTable.putAll(pair.getItem3());
                        linearLayout.addView(childView);
                    }
                    result = linearLayout;
                }
            } else {
                RelativeLayout relativeLayout = new RelativeLayout(context);
                for (Controls.Control el : panelCtrl.getControls()) {
                    Tuple<View, List<Pair<Controls.Control, View>>
                            , CustomHashtable<String, Pair<Controls.Control, View>>> pair =
                            buildViewTree(panelCtrl.getLayoutType(), el);
                    View childView = pair.getItem1();
                    statefulCtrls.addAll(pair.getItem2());
                    idTable.putAll(pair.getItem3());
                    relativeLayout.addView(childView);
                }
                result = relativeLayout;
            }
        } else if (control instanceof Controls.TextCtrl) {
            Controls.TextCtrl textCtrl = (Controls.TextCtrl) control;
            TextView textView = new TextView(context);
            if (!FieldValidator.isFieldEmpty(textCtrl.getText()))
                textView.setText(textCtrl.getText());
            if (!FieldValidator.isFieldEmpty(textCtrl.getTextSize()))
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, textCtrl.getTextSize());
            if (!FieldValidator.isFieldEmpty(textCtrl.getTextColor()))
                textView.setTextColor(Color.parseColor(textCtrl.getTextColor()));
            if (!FieldValidator.isFieldEmpty(textCtrl.getLineCount()))
                textView.setLines(textCtrl.getLineCount());
            if (!textCtrl.isMultiline())
                textView.setEllipsize(TextUtils.TruncateAt.END);
            textView.setSingleLine(!textCtrl.isMultiline());
            if (!FieldValidator.isFieldEmpty(textCtrl.getGravityType())) {
                if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.LEFT) {
                    textView.setGravity(Gravity.LEFT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.TOP) {
                    textView.setGravity(Gravity.TOP);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.RIGHT) {
                    textView.setGravity(Gravity.RIGHT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.BOTTOM) {
                    textView.setGravity(Gravity.BOTTOM);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.TOP_LEFT) {
                    textView.setGravity(Gravity.TOP | Gravity.LEFT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.TOP_RIGHT) {
                    textView.setGravity(Gravity.TOP | Gravity.RIGHT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.BOTTOM_LEFT) {
                    textView.setGravity(Gravity.BOTTOM | Gravity.LEFT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.BOTTOM_RIGHT) {
                    textView.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.LEFT_CENTER_VERTICAL) {
                    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.RIGHT_CENTER_VERTICAL) {
                    textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.TOP_CENTER_HORIZONTAL) {
                    textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.BOTTOM_CENTER_HORIZONTAL) {
                    textView.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                } else if (textCtrl.getGravityType() == Controls.TextCtrl.GravityType.CENTER) {
                    textView.setGravity(Gravity.CENTER);
                }
            }
            result = textView;
        } else if (control instanceof Controls.ImageCtrl) {
            Controls.ImageCtrl imageCtrl = (Controls.ImageCtrl) control;
            ImageView imageView = new ImageView(context);
            if (!FieldValidator.isFieldEmpty(imageCtrl.getScaleType()))
                imageView.setScaleType(ImageView.ScaleType.valueOf(
                        ImageView.ScaleType.class,
                        imageCtrl.getScaleType().toString()));
            if (!FieldValidator.isFieldEmpty(imageCtrl.getImageUrl()))
                Glide.with(context)
                        .load(imageCtrl.getImageUrl())
                        .into(imageView);
            result = imageView;
        } else if (control instanceof Controls.InputFieldCtrl) {
            Controls.InputFieldCtrl inputFieldCtrl = (Controls.InputFieldCtrl) control;
            EditText editText = new EditText(context);
            if (!FieldValidator.isFieldEmpty(inputFieldCtrl.getHint()))
                editText.setHint(inputFieldCtrl.getHint());
            if (!FieldValidator.isFieldEmpty(inputFieldCtrl.getHintColor()))
                editText.setHintTextColor(Color.parseColor(inputFieldCtrl.getHintColor()));
            else
                editText.setHintTextColor(Color.GRAY);
            if (!FieldValidator.isFieldEmpty(inputFieldCtrl.getTextColor()))
                editText.setTextColor(Color.parseColor(inputFieldCtrl.getTextColor()));
            else
                editText.setTextColor(Color.BLACK);
            if (!FieldValidator.isFieldEmpty(inputFieldCtrl.getTextSize()))
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, inputFieldCtrl.getTextSize());
            else
                editText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            if (!FieldValidator.isFieldEmpty(inputFieldCtrl.getLineColor()))
                editText.getBackground().setColorFilter(
                        Color.parseColor(inputFieldCtrl.getLineColor()), PorterDuff.Mode.SRC_ATOP);
            if (!FieldValidator.isFieldEmpty(inputFieldCtrl.getText()))
                editText.setText(inputFieldCtrl.getText());
            editText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    inputFieldCtrl.setText(charSequence.toString());
                }

                @Override
                public void afterTextChanged(Editable editable) {

                }
            });
            result = editText;
        } else if (control instanceof Controls.ButtonCtrl) {
            Controls.ButtonCtrl buttonCtrl = (Controls.ButtonCtrl) control;
            Button button = new Button(context);
            if (!FieldValidator.isFieldEmpty(buttonCtrl.getCaption()))
                button.setText(buttonCtrl.getCaption());
            if (!FieldValidator.isFieldEmpty(buttonCtrl.getCaptionSize()))
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, buttonCtrl.getCaptionSize());
            else
                button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            if (!FieldValidator.isFieldEmpty(buttonCtrl.getCaptionColor()))
                button.setTextColor(Color.parseColor(buttonCtrl.getCaptionColor()));
            else
                button.setTextColor(Color.BLACK);
            result = button;
        } else if (control instanceof Controls.ProgressCtrl) {
            Controls.ProgressCtrl progressCtrl = (Controls.ProgressCtrl) control;
            if (progressCtrl.getProgressType() == Controls.ProgressCtrl.ProgressType.CIRCULAR) {
                CircleProgressView circleProgressView = new CircleProgressView(context);
                circleProgressView.setCircleBroken(progressCtrl.isCircleBroken());
                circleProgressView.setTrackWidth(progressCtrl.getTrackWidth());
                circleProgressView.setFillEnabled(progressCtrl.isFillEnabled());
                if (!FieldValidator.isFieldEmpty(progressCtrl.getProgressTextSize()))
                    circleProgressView.setProgressTextSize(progressCtrl.getProgressTextSize());
                if (!FieldValidator.isFieldEmpty(progressCtrl.getProgressTextColor()))
                    circleProgressView.setProgressTextColor(
                            Color.parseColor(progressCtrl.getProgressTextColor()));
                circleProgressView.setTrackEnabled(progressCtrl.isTrackEnabled());
                if (!FieldValidator.isFieldEmpty(progressCtrl.getTrackColor()))
                    circleProgressView.setTrackColor(Color.parseColor(progressCtrl.getTrackColor()));
                circleProgressView.setProgressTextVisibility(progressCtrl.isProgressTextVisibility());
                circleProgressView.setGraduatedEnabled(progressCtrl.isGraduatedEnabled());
                circleProgressView.setScaleZoneWidth(
                        GraphicsHelper.dpToPx(progressCtrl.getScaleZoneWidth()));
                circleProgressView.setScaleZoneLength(
                        GraphicsHelper.dpToPx(progressCtrl.getScaleZoneLength()));
                circleProgressView.setScaleZonePadding(
                        GraphicsHelper.dpToPx(progressCtrl.getScaleZonePadding()));
                circleProgressView.setScaleZoneCornerRadius(
                        GraphicsHelper.dpToPx(progressCtrl.getScaleZoneCornerRadius()));
                result = circleProgressView;
            } else if (progressCtrl.getProgressType() == Controls.ProgressCtrl.ProgressType.HORIZONTAL) {
                HorizontalProgressView horizontalProgressView = new HorizontalProgressView(context);
                if (!FieldValidator.isFieldEmpty(progressCtrl.getTrackWidth()))
                    horizontalProgressView.setTrackWidth(progressCtrl.getTrackWidth());
                if (!FieldValidator.isFieldEmpty(progressCtrl.getProgressTextSize()))
                    horizontalProgressView.setProgressTextSize(progressCtrl.getProgressTextSize());
                if (!FieldValidator.isFieldEmpty(progressCtrl.getProgressTextColor()))
                    horizontalProgressView.setProgressTextColor(
                            Color.parseColor(progressCtrl.getProgressTextColor()));
                horizontalProgressView.setTrackEnabled(progressCtrl.isTrackEnabled());
                if (!FieldValidator.isFieldEmpty(progressCtrl.getTrackColor()))
                    horizontalProgressView.setTrackColor(Color.parseColor(progressCtrl.getTrackColor()));
                horizontalProgressView.setProgressTextVisibility(progressCtrl.isProgressTextVisibility());
                horizontalProgressView.setProgressCornerRadius(
                        GraphicsHelper.dpToPx(progressCtrl.getProgressCornerRadius()));
                if (!FieldValidator.isFieldEmpty(progressCtrl.getProgressTextPaddingBottom()))
                    horizontalProgressView.setProgressTextPaddingBottom(
                            GraphicsHelper.dpToPx(progressCtrl.getProgressTextPaddingBottom()));
                horizontalProgressView.setProgressTextMoved(progressCtrl.isProgressTextMoved());
                result = horizontalProgressView;
            }
        } else if (control instanceof Controls.VideoPlayerCtrl) {
            Controls.VideoPlayerCtrl videoPlayerCtrl = (Controls.VideoPlayerCtrl) control;
            VideoView videoView = new VideoView(context);
            videoView.getVideoInfo().setPortraitWhenFullScreen(true);
            videoView.setVideoPath(Uri.parse(videoPlayerCtrl
                    .getVideoUrl()).toString());
            if (!FieldValidator.isFieldEmpty(videoPlayerCtrl.getProgress()))
                videoView.getPlayer().seekTo(videoPlayerCtrl.getProgress());
            if (videoPlayerCtrl.isPlaying())
                videoView.getPlayer().start();
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mainThreadRunner.runOnMainThread(() ->
                            videoPlayerCtrl.setProgress(videoView.getPlayer().getCurrentPosition()));
                }
            }, 1, 1000);
            statefulCtrls.add(new Pair<>(videoPlayerCtrl, videoView));
            result = videoView;
        } else if (control instanceof Controls.LineChartCtrl) {
            Controls.LineChartCtrl chartEl = (Controls.LineChartCtrl) control;
            LineChartView lineChartView = new LineChartView(context);
            LineSet dataset = initLineChartView((Controls.LineChartCtrl) control);
            lineChartView.addData(dataset);
            if (!FieldValidator.isFieldEmpty(chartEl.getAxisColor()))
                lineChartView.setAxisColor(Color.parseColor(chartEl.getAxisColor()));
            if (!FieldValidator.isFieldEmpty(chartEl.getLabelsColor()))
                lineChartView.setLabelsColor(Color.parseColor(chartEl.getLabelsColor()));
            lineChartView.setClickablePointRadius(GraphicsHelper.dpToPx(chartEl.getDotsRadius()));
            lineChartView.show();
            result = lineChartView;
        } else if (control instanceof Controls.HorizontalBarChartCtrl) {
            Controls.HorizontalBarChartCtrl chartEl = (Controls.HorizontalBarChartCtrl) control;
            HorizontalBarChartView horizontalBarChartView = new HorizontalBarChartView(context);
            if (!FieldValidator.isFieldEmpty(chartEl.getBarSpacing()))
                horizontalBarChartView.setBarSpacing(GraphicsHelper.dpToPx(chartEl.getBarSpacing()));
            if (!FieldValidator.isFieldEmpty(chartEl.getSetSpacing()))
                horizontalBarChartView.setSetSpacing(GraphicsHelper.dpToPx(chartEl.getSetSpacing()));
            if (!FieldValidator.isFieldEmpty(chartEl.getBarBackgroundColor()))
                horizontalBarChartView.setBarBackgroundColor(Color.parseColor(chartEl.getBarBackgroundColor()));
            if (!FieldValidator.isFieldEmpty(chartEl.getRoundCorners()))
                horizontalBarChartView.setRoundCorners(GraphicsHelper.dpToPx(chartEl.getRoundCorners()));
            BarSet dataset = new BarSet();
            if (!FieldValidator.isFieldEmpty(chartEl.getBarsColor()))
                dataset.setColor(Color.parseColor(chartEl.getBarsColor()));
            Iterator<Data.Point> pointIterator = chartEl.getPoints().iterator();
            Iterator<Data.StringValue> barColorIterator = chartEl.getBarColors().iterator();
            while (pointIterator.hasNext() && barColorIterator.hasNext()) {
                Data.Point point = pointIterator.next();
                Data.StringValue barColor = barColorIterator.next();
                Bar bar = new Bar(point.getLabel(), point.getValue());
                bar.setColor(Color.parseColor(barColor.getValue()));
                dataset.addBar(bar);
            }
            horizontalBarChartView.addData(dataset);
            if (!FieldValidator.isFieldEmpty(chartEl.getAxisColor()))
                horizontalBarChartView.setAxisColor(Color.parseColor(chartEl.getAxisColor()));
            if (!FieldValidator.isFieldEmpty(chartEl.getLabelsColor()))
                horizontalBarChartView.setLabelsColor(Color.parseColor(chartEl.getLabelsColor()));
            horizontalBarChartView.show();
            result = horizontalBarChartView;
        } else if (control instanceof Controls.StackBarChartCtrl) {
            Controls.StackBarChartCtrl stackBarChartCtrl = (Controls.StackBarChartCtrl) control;
            StackBarChartView stackBarChartView = new StackBarChartView(context);
            if (!FieldValidator.isFieldEmpty(stackBarChartCtrl.getBarSpacing()))
                stackBarChartView.setBarSpacing(GraphicsHelper.dpToPx(stackBarChartCtrl.getBarSpacing()));
            if (!FieldValidator.isFieldEmpty(stackBarChartCtrl.getBarBackgroundColor()))
                stackBarChartView.setBarBackgroundColor(
                        Color.parseColor(stackBarChartCtrl.getBarBackgroundColor()));
            if (!FieldValidator.isFieldEmpty(stackBarChartCtrl.getRoundCorners()))
                stackBarChartView.setRoundCorners(
                        GraphicsHelper.dpToPx(stackBarChartCtrl.getRoundCorners()));
            BarSet dataset = new BarSet();
            if (!FieldValidator.isFieldEmpty(stackBarChartCtrl.getBarsColor()))
                dataset.setColor(Color.parseColor(stackBarChartCtrl.getBarsColor()));
            Iterator<Data.Point> pointIterator = stackBarChartCtrl.getPoints().iterator();
            Iterator<Data.StringValue> barColorIterator = stackBarChartCtrl.getBarColors().iterator();
            while (pointIterator.hasNext() && barColorIterator.hasNext()) {
                Data.Point point = pointIterator.next();
                Data.StringValue barColor = barColorIterator.next();
                Bar bar = new Bar(point.getLabel(), point.getValue());
                bar.setColor(Color.parseColor(barColor.getValue()));
                dataset.addBar(bar);
            }
            stackBarChartView.addData(dataset);
            if (!FieldValidator.isFieldEmpty(stackBarChartCtrl.getAxisColor()))
                stackBarChartView.setAxisColor(Color.parseColor(stackBarChartCtrl.getAxisColor()));
            if (!FieldValidator.isFieldEmpty(stackBarChartCtrl.getLabelsColor()))
                stackBarChartView.setLabelsColor(Color.parseColor(stackBarChartCtrl.getLabelsColor()));
            stackBarChartView.show();
            result = stackBarChartView;
        } else if (control instanceof Controls.HorizontalStackBarChartCtrl) {
            Controls.HorizontalStackBarChartCtrl horizontalStackBarChartCtrl =
                    (Controls.HorizontalStackBarChartCtrl) control;
            HorizontalStackBarChartView horizontalStackBarChartView =
                    new HorizontalStackBarChartView(context);
            if (!FieldValidator.isFieldEmpty(horizontalStackBarChartCtrl.getBarSpacing()))
                horizontalStackBarChartView.setBarSpacing(
                        GraphicsHelper.dpToPx(horizontalStackBarChartCtrl.getBarSpacing()));
            if (!FieldValidator.isFieldEmpty(horizontalStackBarChartCtrl.getBarBackgroundColor()))
                horizontalStackBarChartView.setBarBackgroundColor(
                        Color.parseColor(horizontalStackBarChartCtrl.getBarBackgroundColor()));
            if (!FieldValidator.isFieldEmpty(horizontalStackBarChartCtrl.getRoundCorners()))
                horizontalStackBarChartView.setRoundCorners(
                        GraphicsHelper.dpToPx(horizontalStackBarChartCtrl.getRoundCorners()));
            BarSet dataset = new BarSet();
            if (!FieldValidator.isFieldEmpty(horizontalStackBarChartCtrl.getBarsColor()))
                dataset.setColor(Color.parseColor(horizontalStackBarChartCtrl.getBarsColor()));
            Iterator<Data.Point> pointIterator = horizontalStackBarChartCtrl.getPoints().iterator();
            Iterator<Data.StringValue> barColorIterator =
                    horizontalStackBarChartCtrl.getBarColors().iterator();
            while (pointIterator.hasNext() && barColorIterator.hasNext()) {
                Data.Point point = pointIterator.next();
                Data.StringValue barColor = barColorIterator.next();
                Bar bar = new Bar(point.getLabel(), point.getValue());
                bar.setColor(Color.parseColor(barColor.getValue()));
                dataset.addBar(bar);
            }
            horizontalStackBarChartView.addData(dataset);
            if (!FieldValidator.isFieldEmpty(horizontalStackBarChartCtrl.getAxisColor()))
                horizontalStackBarChartView.setAxisColor(
                        Color.parseColor(horizontalStackBarChartCtrl.getAxisColor()));
            if (!FieldValidator.isFieldEmpty(horizontalStackBarChartCtrl.getLabelsColor()))
                horizontalStackBarChartView.setLabelsColor(
                        Color.parseColor(horizontalStackBarChartCtrl.getLabelsColor()));
            horizontalStackBarChartView.show();
            result = horizontalStackBarChartView;
        } else if (control instanceof Controls.BarChartCtrl) {
            Controls.BarChartCtrl chartEl = (Controls.BarChartCtrl) control;
            BarChartView barChartView = new BarChartView(context);
            if (!FieldValidator.isFieldEmpty(chartEl.getBarSpacing()))
                barChartView.setBarSpacing(GraphicsHelper.dpToPx(chartEl.getBarSpacing()));
            if (!FieldValidator.isFieldEmpty(chartEl.getSetSpacing()))
                barChartView.setSetSpacing(GraphicsHelper.dpToPx(chartEl.getSetSpacing()));
            if (!FieldValidator.isFieldEmpty(chartEl.getBarBackgroundColor()))
                barChartView.setBarBackgroundColor(Color.parseColor(chartEl.getBarBackgroundColor()));
            if (!FieldValidator.isFieldEmpty(chartEl.getRoundCorners()))
                barChartView.setRoundCorners(GraphicsHelper.dpToPx(chartEl.getRoundCorners()));
            BarSet dataset = new BarSet();
            if (!FieldValidator.isFieldEmpty(chartEl.getBarsColor()))
                dataset.setColor(Color.parseColor(chartEl.getBarsColor()));
            Iterator<Data.Point> pointIterator = chartEl.getPoints().iterator();
            Iterator<Data.StringValue> barColorIterator = chartEl.getBarColors().iterator();
            while (pointIterator.hasNext() && barColorIterator.hasNext()) {
                Data.Point point = pointIterator.next();
                Data.StringValue barColor = barColorIterator.next();
                Bar bar = new Bar(point.getLabel(), point.getValue());
                bar.setColor(Color.parseColor(barColor.getValue()));
                dataset.addBar(bar);
            }
            barChartView.addData(dataset);
            if (!FieldValidator.isFieldEmpty(chartEl.getAxisColor()))
                barChartView.setAxisColor(Color.parseColor(chartEl.getAxisColor()));
            if (!FieldValidator.isFieldEmpty(chartEl.getLabelsColor()))
                barChartView.setLabelsColor(Color.parseColor(chartEl.getLabelsColor()));
            barChartView.show();
            result = barChartView;
        } else if (control instanceof Controls.ScrollerCtrl) {
            Controls.ScrollerCtrl scrollerCtrl = (Controls.ScrollerCtrl) control;
            NestedScrollView scrollView = new NestedScrollView(context);
            Tuple<View, List<Pair<Controls.Control, View>>
                    , CustomHashtable<String, Pair<Controls.Control, View>>> pair =
                    buildViewTree(Controls.PanelCtrl.LayoutType.RELATIVE, scrollerCtrl.getPanel());
            View panelView = pair.getItem1();
            statefulCtrls.addAll(pair.getItem2());
            idTable.putAll(pair.getItem3());
            panelView.setLayoutParams(new ScrollView.LayoutParams(
                    ScrollView.LayoutParams.MATCH_PARENT,
                    ScrollView.LayoutParams.WRAP_CONTENT));
            scrollView.addView(panelView);
            scrollView.scrollTo(0, scrollerCtrl.getPosition());
            scrollView.getViewTreeObserver()
                    .addOnScrollChangedListener(() -> scrollerCtrl.setPosition(scrollView.getScrollY()));
            result = scrollView;
        } else if (control instanceof Controls.OptionCtrl) {
            Controls.OptionCtrl optionCtrl = (Controls.OptionCtrl) control;
            RadioButton radioButton = new RadioButton(context);
            if (!FieldValidator.isFieldEmpty(optionCtrl.getCaption()))
                radioButton.setText(optionCtrl.getCaption());
            if (!FieldValidator.isFieldEmpty(optionCtrl.getCaptionColor()))
                radioButton.setTextColor(Color.parseColor(optionCtrl.getCaptionColor()));
            if (!FieldValidator.isFieldEmpty(optionCtrl.getCaptionSize()))
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, optionCtrl.getCaptionSize());
            if (!FieldValidator.isFieldEmpty(optionCtrl.getTintColor())) {
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_checked}, // unchecked
                                new int[]{android.R.attr.state_checked}, // checked
                        },
                        new int[]{
                                Color.parseColor(optionCtrl.getTintColor()),
                                Color.parseColor(optionCtrl.getTintColor()),
                        }
                );
                CompoundButtonCompat.setButtonTintList(radioButton, colorStateList);
            }
            radioButton.setChecked(optionCtrl.isChecked());
            radioButton.setOnCheckedChangeListener((compoundButton, b)
                    -> optionCtrl.setChecked(radioButton.isChecked()));
            result = radioButton;
        } else if (control instanceof Controls.CheckCtrl) {
            Controls.CheckCtrl checkCtrl = (Controls.CheckCtrl) control;
            CheckBox checkBox = new CheckBox(context);
            if (!FieldValidator.isFieldEmpty(checkCtrl.getCaption()))
                checkBox.setText(checkCtrl.getCaption());
            if (!FieldValidator.isFieldEmpty(checkCtrl.getCaptionColor()))
                checkBox.setTextColor(Color.parseColor(checkCtrl.getCaptionColor()));
            if (!FieldValidator.isFieldEmpty(checkCtrl.getCaptionSize()))
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_SP, checkCtrl.getCaptionSize());
            if (!FieldValidator.isFieldEmpty(checkCtrl.getTintColor())) {
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[]{-android.R.attr.state_checked}, // unchecked
                                new int[]{android.R.attr.state_checked} , // checked
                        },
                        new int[]{
                                Color.parseColor(checkCtrl.getTintColor()),
                                Color.parseColor(checkCtrl.getTintColor()),
                        }
                );
                CompoundButtonCompat.setButtonTintList(checkBox,colorStateList);
            }
            checkBox.setChecked(checkCtrl.isChecked());
            checkBox.setOnCheckedChangeListener((compoundButton, b)
                    -> checkCtrl.setChecked(checkBox.isChecked()));
            result = checkBox;
        } else if (control instanceof Controls.DropDownCtrl) {
            Controls.DropDownCtrl dropDownCtrl = (Controls.DropDownCtrl) control;
            Spinner spinner = new Spinner(context);
            spinner.setAdapter(new DropDownAdapter(
                    dropDownCtrl.getItems(), idTable, this));
            spinner.setSelection(dropDownCtrl.getSelectedPos());
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    dropDownCtrl.setSelectedPos(spinner.getSelectedItemPosition());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
            result = spinner;
        } else if (control instanceof Controls.RecyclerCtrl) {
            Controls.RecyclerCtrl recyclerCtrl = (Controls.RecyclerCtrl) control;
            RecyclerView recyclerView = new RecyclerView(context);
            if (recyclerCtrl.getRecyclerType() ==
                    Controls.RecyclerCtrl.RecyclerLayoutType.LINEAR) {
                if (recyclerCtrl.getOrientation() ==
                        Controls.RecyclerCtrl.RecyclerOrientation.HORIZONTAL)
                    recyclerView.setLayoutManager(new LinearLayoutManager(
                            context, RecyclerView.HORIZONTAL, false));
                else if (recyclerCtrl.getOrientation() ==
                        Controls.RecyclerCtrl.RecyclerOrientation.VERTICAL)
                    recyclerView.setLayoutManager(new LinearLayoutManager(
                            context, RecyclerView.VERTICAL, false));
            } else {
                if (recyclerCtrl.getOrientation() ==
                        Controls.RecyclerCtrl.RecyclerOrientation.HORIZONTAL)
                    recyclerView.setLayoutManager(new GridLayoutManager(
                            context, recyclerCtrl.getGridSpanCount()
                            , RecyclerView.HORIZONTAL, false));
                else if (recyclerCtrl.getOrientation() ==
                        Controls.RecyclerCtrl.RecyclerOrientation.VERTICAL)
                    recyclerView.setLayoutManager(new GridLayoutManager(
                            context, recyclerCtrl.getGridSpanCount()
                            , RecyclerView.VERTICAL, false));
            }

            recyclerView.setAdapter(new RecyclerAdapter(
                    context, this, idTable, recyclerCtrl.getItems()));

            statefulCtrls.add(new Pair<>(recyclerCtrl, recyclerView));
            result = recyclerView;
        } else if (control instanceof Controls.SeekBarCtrl) {
            Controls.SeekBarCtrl seekBarCtrl = (Controls.SeekBarCtrl) control;
            CustomSeekBar seekBar = new CustomSeekBar(context);
            seekBar.setProgress(seekBarCtrl.getProgress());
            if (!FieldValidator.isFieldEmpty(seekBarCtrl.getThumbColor())) {
                seekBar.getThumb().setColorFilter(Color.parseColor(
                        seekBarCtrl.getThumbColor()), PorterDuff.Mode.SRC_ATOP);
                seekBar.getProgressDrawable().setColorFilter(Color.parseColor(
                        seekBarCtrl.getThumbColor()), PorterDuff.Mode.SRC_ATOP);
            }
            if (!FieldValidator.isFieldEmpty(seekBarCtrl.getTrackThickness())) {
                ShapeDrawable shapeDrawable = new ShapeDrawable();
                shapeDrawable.setShape(new RectShape());
                shapeDrawable.setIntrinsicWidth(GraphicsHelper.dpToPx(seekBarCtrl.getWidth()));
                shapeDrawable.setIntrinsicHeight(GraphicsHelper.dpToPx(seekBarCtrl.getTrackThickness()));
                seekBar.setProgressDrawable(shapeDrawable);
                if (!FieldValidator.isFieldEmpty(seekBarCtrl.getTrackColor()))
                    shapeDrawable.getPaint().setColor(Color.parseColor(seekBarCtrl.getTrackColor()));
                else
                    shapeDrawable.getPaint().setColor(Color.WHITE);
            }
            if (!FieldValidator.isFieldEmpty(seekBarCtrl.getTrackColor())) {
                seekBar.getProgressDrawable().setColorFilter(Color.parseColor(
                        seekBarCtrl.getTrackColor()), PorterDuff.Mode.MULTIPLY);
                if (seekBar.getProgressDrawable() instanceof ShapeDrawable)
                    ((ShapeDrawable) seekBar.getProgressDrawable())
                            .getPaint().setColor(Color.parseColor(
                                    seekBarCtrl.getTrackColor()));
            }
            if (!FieldValidator.isFieldEmpty(seekBarCtrl.getThumbSize())) {
                ShapeDrawable th = new ShapeDrawable(new OvalShape());
                th.setIntrinsicWidth(GraphicsHelper.dpToPx(seekBarCtrl.getThumbSize()));
                th.setIntrinsicHeight(GraphicsHelper.dpToPx(seekBarCtrl.getThumbSize()));
                if (!FieldValidator.isFieldEmpty(seekBarCtrl.getThumbColor()))
                    th.getPaint().setColor(Color.parseColor(seekBarCtrl.getThumbColor()));
                else
                    th.getPaint().setColor(Color.WHITE);
                seekBar.setThumb(th);
            }
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                    seekBarCtrl.setProgress(i);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            result = seekBar;
        }

        if (result != null) {
            result = fillGeneralProps(result, parentLayoutType, control);
        }

        if (!FieldValidator.isFieldEmpty(control.getId()))
            idTable.put(control.getId(), new Pair<>(control, result));

        return new Tuple<>(result, statefulCtrls, idTable);
    }

    public void initFingerprint(List<Pair<Controls.Control, View>> list, int i) {
        for (Pair<Controls.Control, View> statefulCtrl : list) {
            View target = statefulCtrl.second;
            if (target instanceof CardView) {
                target = ((CardView) target).getChildAt(0);
                if (target instanceof CardView) {
                    target = ((CardView) target).getChildAt(0);
                }
            }
            if (statefulCtrl.first instanceof Controls.VideoPlayerCtrl) {
                VideoView videoView = (VideoView) target;
                videoView.setFingerprint(i);
            }
        }
    }

    private View fillGeneralProps(View view, Controls.PanelCtrl.LayoutType parentLayoutType, Controls.Control el) {
        if (!(el.getPaddingLeft() == 0 && el.getPaddingTop() == 0 &&
                el.getPaddingTop() == 0 && el.getPaddingBottom() == 0))
            view.setPadding(
                    GraphicsHelper.dpToPx(el.getPaddingLeft()),
                    GraphicsHelper.dpToPx(el.getPaddingTop()),
                    GraphicsHelper.dpToPx(el.getPaddingRight()),
                    GraphicsHelper.dpToPx(el.getPaddingBottom()));

        if (!FieldValidator.isFieldEmpty(el.getBackColor()))
            view.setBackgroundColor(Color.parseColor(el.getBackColor()));

        CardView outerCV = new CardView(context);
        outerCV.setRadius(GraphicsHelper.dpToPx(el.getCornerRadius()));
        if (!FieldValidator.isFieldEmpty(el.getBorderColor()))
            outerCV.setCardBackgroundColor(Color.parseColor(el.getBorderColor()));
        else if (FieldValidator.isFieldEmpty(el.getCornerRadius()))
            outerCV.setCardBackgroundColor(Color.TRANSPARENT);
        else if (!FieldValidator.isFieldEmpty(el.getBackColor()) && FieldValidator.isFieldEmpty(el.getCornerRadius()))
            outerCV.setBackgroundColor(Color.parseColor(el.getBackColor()));
        //            outerCV.setBackgroundResource(R.drawable.empty_background);
        CardView innerCV = new CardView(context);
        CardView.LayoutParams innerCvParams = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.MATCH_PARENT);
        int bw = GraphicsHelper.dpToPx(el.getBorderWidth());
        innerCvParams.setMargins(bw, bw, bw, bw);
        innerCV.setLayoutParams(innerCvParams);
        innerCV.setRadius(GraphicsHelper.dpToPx(el.getCornerRadius()));
        innerCV.setCardElevation(0);
        if (FieldValidator.isFieldEmpty(el.getBackColor()))
            innerCV.setCardBackgroundColor(Color.TRANSPARENT);
            //    innerCV.setBackgroundResource(R.drawable.empty_background);
        innerCV.addView(view);
        outerCV.addView(innerCV);
        view = outerCV;
        ((CardView) view).setCardElevation(GraphicsHelper.dpToPx(el.getElevation()));

        ViewGroup.LayoutParams mlp = null;
        if (parentLayoutType == Controls.PanelCtrl.LayoutType.RELATIVE) {
            mlp = new RelativeLayout.LayoutParams(0, 0);
            ((RelativeLayout.LayoutParams) mlp).setMargins(
                    GraphicsHelper.dpToPx(el.getMarginLeft()),
                    GraphicsHelper.dpToPx(el.getMarginTop()),
                    GraphicsHelper.dpToPx(el.getMarginRight()),
                    GraphicsHelper.dpToPx(el.getMarginBottom()));
        } else if (parentLayoutType == Controls.PanelCtrl.LayoutType.LINEAR_VERTICAL ||
                parentLayoutType == Controls.PanelCtrl.LayoutType.LINEAR_HORIZONTAL) {
            mlp = new LinearLayout.LayoutParams(0, 0);
            ((LinearLayout.LayoutParams) mlp).setMargins(
                    GraphicsHelper.dpToPx(el.getMarginLeft()),
                    GraphicsHelper.dpToPx(el.getMarginTop()),
                    GraphicsHelper.dpToPx(el.getMarginRight()),
                    GraphicsHelper.dpToPx(el.getMarginBottom()));
        }

        if (mlp != null) {

            if (!FieldValidator.isFieldEmpty(el.getWidth())) {
                if (el.getWidth() == Controls.Control.MATCH_PARENT)
                    mlp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                else if (el.getWidth() == Controls.Control.WRAP_CONTENT)
                    mlp.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                else
                    mlp.width = GraphicsHelper.dpToPx(el.getWidth());
            }
            else
                mlp.width = ViewGroup.LayoutParams.WRAP_CONTENT;

            if (!FieldValidator.isFieldEmpty(el.getHeight())) {
                if (el.getHeight() == Controls.Control.MATCH_PARENT)
                    mlp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                else if (el.getHeight() == Controls.Control.WRAP_CONTENT)
                    mlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                else
                    mlp.height = GraphicsHelper.dpToPx(el.getHeight());
            }
            else
                mlp.height = ViewGroup.LayoutParams.WRAP_CONTENT;

            if (el.getX() == Controls.Control.CENTER) {
                if (mlp instanceof RelativeLayout.LayoutParams)
                    ((RelativeLayout.LayoutParams) mlp).addRule(RelativeLayout.CENTER_HORIZONTAL);
                else if (mlp instanceof LinearLayout.LayoutParams)
                    ((LinearLayout.LayoutParams) mlp).gravity = Gravity.CENTER_HORIZONTAL;
            } else
                view.setX(GraphicsHelper.dpToPx(el.getX()));

            if (el.getY() == Controls.Control.CENTER) {
                if (mlp instanceof RelativeLayout.LayoutParams)
                    ((RelativeLayout.LayoutParams) mlp).addRule(RelativeLayout.CENTER_VERTICAL);
                else if (mlp instanceof LinearLayout.LayoutParams) {
                    if (((LinearLayout.LayoutParams) mlp).gravity == Gravity.CENTER_HORIZONTAL)
                        ((LinearLayout.LayoutParams) mlp).gravity = Gravity.CENTER;
                    else
                        ((LinearLayout.LayoutParams) mlp).gravity = Gravity.CENTER_VERTICAL;
                }
            } else
                view.setY(GraphicsHelper.dpToPx(el.getY()));

            view.setLayoutParams(mlp);
        }

        view.setRotationX(el.getRotationX());
        view.setRotationY(el.getRotationY());
        view.setRotation(el.getRotation());

        if (el.isClickable()) {
            view.setOnClickListener(v -> clickNotifier.controlClicked(el.getId()));
            view.setClickable(true);
        } else {
            view.setOnClickListener(null);
            view.setClickable(false);
        }

        return view;
    }

    LineSet initLineChartView(Controls.LineChartCtrl chartEl) {
        LineSet dataset = new LineSet();
        for (Data.Point point : chartEl.getPoints()) {
            dataset.addPoint(point.getLabel(), point.getValue());
        }
        if (!FieldValidator.isFieldEmpty(chartEl.getDotsColor()))
            dataset.setDotsColor(Color.parseColor(chartEl.getDotsColor()));
        if (!FieldValidator.isFieldEmpty(chartEl.getDotsRadius()))
            dataset.setDotsRadius(GraphicsHelper.dpToPx(chartEl.getDotsRadius()));
        if (!FieldValidator.isFieldEmpty(chartEl.getDotsStrokeThickness()))
            dataset.setDotsStrokeThickness(GraphicsHelper.dpToPx(chartEl.getDotsStrokeThickness()));
        if (!FieldValidator.isFieldEmpty(chartEl.getDotsStrokeColor()))
            dataset.setDotsStrokeColor(Color.parseColor(chartEl.getDotsStrokeColor()));
        if (!FieldValidator.isFieldEmpty(chartEl.getLineDashedIntervals())) {
            float[] lineDashedIntervals = new float[chartEl.getLineDashedIntervals().size()];
            int counter = 0;
            for (Data.FloatValue floatValue : chartEl.getLineDashedIntervals()) {
                lineDashedIntervals[counter] = floatValue.getValue();
                counter++;
            }
            dataset.setDashed(lineDashedIntervals);
        }
        dataset.setSmooth(chartEl.isLineSmooth());
        if (!FieldValidator.isFieldEmpty(chartEl.getLineThickness()))
            dataset.setThickness(GraphicsHelper.dpToPx(chartEl.getLineThickness()));
        if (!FieldValidator.isFieldEmpty(chartEl.getLineColor()))
            dataset.setColor(Color.parseColor(chartEl.getLineColor()));
        if (!(chartEl.getLineBeginAt() == 0 && chartEl.getLineEndAt() == 0)) {
            dataset.beginAt(chartEl.getLineBeginAt());
            dataset.endAt(chartEl.getLineEndAt());
        }
        if (!FieldValidator.isFieldEmpty(chartEl.getFillColor()))
            dataset.setFill(Color.parseColor(chartEl.getFillColor()));
        if (!FieldValidator.isFieldEmpty(chartEl.getGradientColors())) {
            int[] gradientColors = new int[chartEl.getGradientColors().size()];
            float[] gradientValues = new float[chartEl.getGradientValues().size()];
            int counter = 0;
            Iterator<Data.StringValue> colorIterator = chartEl.getGradientColors().iterator();
            Iterator<Data.FloatValue> valueIterator = chartEl.getGradientValues().iterator();
            while (colorIterator.hasNext() && valueIterator.hasNext()) {
                Data.StringValue color = colorIterator.next();
                Data.FloatValue value = valueIterator.next();
                gradientColors[counter] = Color.parseColor(color.getValue());
                gradientValues[counter] = value.getValue();
                counter++;
            }
            if (gradientColors.length > 0)
                dataset.setGradientFill(gradientColors, gradientValues);
        }
        return dataset;
    }
}
