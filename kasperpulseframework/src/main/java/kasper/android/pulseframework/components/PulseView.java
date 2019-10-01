package kasper.android.pulseframework.components;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import kasper.android.pulseframework.engines.EREngine;
import kasper.android.pulseframework.engines.UiAnimatorEngine;
import kasper.android.pulseframework.engines.UiInitiatorEngine;
import kasper.android.pulseframework.engines.UiUpdaterEngine;
import kasper.android.pulseframework.interfaces.IClickNotifier;
import kasper.android.pulseframework.interfaces.IFetchVarValue;
import kasper.android.pulseframework.interfaces.IHandleValueAnimator;
import kasper.android.pulseframework.locks.Locks;
import kasper.android.pulseframework.models.Anims;
import kasper.android.pulseframework.models.Bindings;
import kasper.android.pulseframework.models.Codes;
import kasper.android.pulseframework.models.Controls;
import kasper.android.pulseframework.models.Tuple;
import kasper.android.pulseframework.models.Updates;
import kasper.android.pulseframework.utils.GraphicsHelper;
import kasper.android.pulseframework.utils.JsonHelper;

public class PulseView extends RelativeLayout {

    private CustomHashtable<String, Pair<Controls.Control, View>> idTable;
    private UiInitiatorEngine uiInitiatorEngine;
    private UiUpdaterEngine uiUpdaterEngine;
    private UiAnimatorEngine uiAnimatorEngine;
    private EREngine erEngine;
    private Controls.Control root;

    public Controls.Control getRoot() {
        return root;
    }

    private boolean childrenNotTouch = false;

    public void enableChildrenTouch() {
        this.childrenNotTouch = false;
    }

    public void disableChildrenTouch() {
        this.childrenNotTouch = true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return childrenNotTouch;
    }

    public PulseView(Context context) {
        super(context);
        init();
    }

    public PulseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PulseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public PulseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    ProgressBar pb;

    private void init() {
        this.idTable = new CustomHashtable<>();
        GraphicsHelper.setup(getContext());
        this.setFocusableInTouchMode(true);
    }

    public void setup(AppCompatActivity activity, IClickNotifier controlClickNotifier) {
        JsonHelper.setup();
        Locks.setup(activity::runOnUiThread);
        pb = new ProgressBar(this.getContext());
        LayoutParams lp = new RelativeLayout.LayoutParams(GraphicsHelper.dpToPx(56), GraphicsHelper.dpToPx(56));
        lp.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        pb.setLayoutParams(lp);
        this.addView(pb);
        this.uiInitiatorEngine = new UiInitiatorEngine(
                getContext(),
                idTable,
                activity::runOnUiThread,
                controlClickNotifier);
        this.uiUpdaterEngine = new UiUpdaterEngine(
                getContext(),
                activity::runOnUiThread,
                controlClickNotifier);
        this.uiAnimatorEngine = new UiAnimatorEngine(
                update -> uiUpdaterEngine.updateUi(idTable, update),
                activity::runOnUiThread,
                variableName -> {
                    Codes.Variable var = erEngine.getVariableTable().get(variableName);
                    if (var != null) {
                        return var.getValue().getValue();
                    } else {
                        return new Object();
                    }
                },
                (controlId, property, value) -> {
                    Pair<Controls.Control, View> pair = idTable.get(controlId);
                    if (pair != null) {
                        View view = pair.second;
                        Controls.Control control = pair.first;
                        switch (property) {
                            case "x": {
                                control.setX((int)value);
                                if ((int)value != Controls.Control.CENTER)
                                    view.setX(GraphicsHelper.dpToPx((int)value));
                                else
                                    view.setX((int)value);
                                break;
                            }
                            case "y": {
                                control.setY((int)value);
                                if ((int)value != Controls.Control.CENTER)
                                    view.setY(GraphicsHelper.dpToPx((int)value));
                                else
                                    view.setY((int)value);
                                break;
                            }
                            case "width": {
                                control.setWidth((int)value);
                                ViewGroup.LayoutParams params = view.getLayoutParams();
                                if ((int)value != Controls.Control.MATCH_PARENT
                                        && (int)value != Controls.Control.WRAP_CONTENT)
                                    params.width = GraphicsHelper.dpToPx((int)value);
                                else
                                    params.width = (int)value;
                                view.setLayoutParams(params);
                                break;
                            }
                            case "height": {
                                control.setHeight((int)value);
                                ViewGroup.LayoutParams params = view.getLayoutParams();
                                if ((int)value != Controls.Control.MATCH_PARENT
                                        && (int)value != Controls.Control.WRAP_CONTENT)
                                    params.height = GraphicsHelper.dpToPx((int)value);
                                else
                                    params.height = (int)value;
                                view.setLayoutParams(params);
                                break;
                            }
                            case "marginLeft": {
                                control.setMarginLeft((int)value);
                                ViewGroup.LayoutParams params = view.getLayoutParams();
                                if (params instanceof RelativeLayout.LayoutParams) {
                                    ((RelativeLayout.LayoutParams) params).setMargins(
                                            GraphicsHelper.dpToPx((int)value),
                                            ((RelativeLayout.LayoutParams) params).topMargin,
                                            ((RelativeLayout.LayoutParams) params).rightMargin,
                                            ((RelativeLayout.LayoutParams) params).bottomMargin);
                                } else if (params instanceof LinearLayout.LayoutParams) {
                                    ((LinearLayout.LayoutParams) params).setMargins(
                                            GraphicsHelper.dpToPx((int)value),
                                            ((LinearLayout.LayoutParams) params).topMargin,
                                            ((LinearLayout.LayoutParams) params).rightMargin,
                                            ((LinearLayout.LayoutParams) params).bottomMargin);
                                }
                                view.setLayoutParams(params);
                                break;
                            }
                            case "marginTop": {
                                control.setMarginLeft((int)value);
                                ViewGroup.LayoutParams params = view.getLayoutParams();
                                if (params instanceof RelativeLayout.LayoutParams) {
                                    ((RelativeLayout.LayoutParams) params).setMargins(
                                            ((RelativeLayout.LayoutParams) params).leftMargin,
                                            GraphicsHelper.dpToPx((int)value),
                                            ((RelativeLayout.LayoutParams) params).rightMargin,
                                            ((RelativeLayout.LayoutParams) params).bottomMargin);
                                } else if (params instanceof LinearLayout.LayoutParams) {
                                    ((LinearLayout.LayoutParams) params).setMargins(
                                            ((LinearLayout.LayoutParams) params).leftMargin,
                                            GraphicsHelper.dpToPx((int)value),
                                            ((LinearLayout.LayoutParams) params).rightMargin,
                                            ((LinearLayout.LayoutParams) params).bottomMargin);
                                }
                                view.setLayoutParams(params);
                                break;
                            }
                            case "marginRight": {
                                control.setMarginLeft((int)value);
                                ViewGroup.LayoutParams params = view.getLayoutParams();
                                if (params instanceof RelativeLayout.LayoutParams) {
                                    ((RelativeLayout.LayoutParams) params).setMargins(
                                            ((RelativeLayout.LayoutParams) params).leftMargin,
                                            ((RelativeLayout.LayoutParams) params).topMargin,
                                            GraphicsHelper.dpToPx((int)value),
                                            ((RelativeLayout.LayoutParams) params).bottomMargin);
                                } else if (params instanceof LinearLayout.LayoutParams) {
                                    ((LinearLayout.LayoutParams) params).setMargins(
                                            ((LinearLayout.LayoutParams) params).leftMargin,
                                            ((LinearLayout.LayoutParams) params).topMargin,
                                            GraphicsHelper.dpToPx((int)value),
                                            ((LinearLayout.LayoutParams) params).bottomMargin);
                                }
                                view.setLayoutParams(params);
                                break;
                            }
                            case "marginBottom": {
                                control.setMarginLeft((int)value);
                                ViewGroup.LayoutParams params = view.getLayoutParams();
                                if (params instanceof RelativeLayout.LayoutParams) {
                                    ((RelativeLayout.LayoutParams) params).setMargins(
                                            ((RelativeLayout.LayoutParams) params).leftMargin,
                                            ((RelativeLayout.LayoutParams) params).topMargin,
                                            ((RelativeLayout.LayoutParams) params).rightMargin,
                                            GraphicsHelper.dpToPx((int)value));
                                } else if (params instanceof LinearLayout.LayoutParams) {
                                    ((LinearLayout.LayoutParams) params).setMargins(
                                            ((LinearLayout.LayoutParams) params).leftMargin,
                                            ((LinearLayout.LayoutParams) params).topMargin,
                                            ((LinearLayout.LayoutParams) params).rightMargin,
                                            GraphicsHelper.dpToPx((int)value));
                                }
                                view.setLayoutParams(params);
                                break;
                            }
                            case "paddingLeft": {
                                control.setPaddingLeft((int)value);
                                view.setPadding(
                                        GraphicsHelper.dpToPx((int)value),
                                        view.getPaddingTop(),
                                        view.getPaddingRight(),
                                        view.getPaddingBottom());
                                break;
                            }
                            case "paddingTop": {
                                control.setPaddingTop((int)value);
                                view.setPadding(
                                        view.getPaddingLeft(),
                                        GraphicsHelper.dpToPx((int)value),
                                        view.getPaddingRight(),
                                        view.getPaddingBottom());
                                break;
                            }
                            case "paddingRight": {
                                control.setPaddingRight((int)value);
                                view.setPadding(
                                        view.getPaddingLeft(),
                                        view.getPaddingTop(),
                                        GraphicsHelper.dpToPx((int)value),
                                        view.getPaddingBottom());
                                break;
                            }
                            case "paddingBottom": {
                                control.setPaddingBottom((int)value);
                                view.setPadding(
                                        view.getPaddingLeft(),
                                        view.getPaddingTop(),
                                        view.getPaddingRight(),
                                        GraphicsHelper.dpToPx((int)value));
                                break;
                            }
                            case "rotationX": {
                                control.setRotationX((int)value);
                                view.setRotationX((int)value);
                                break;
                            }
                            case "rotationY": {
                                control.setRotationY((int)value);
                                view.setRotationY((int)value);
                                break;
                            }
                            case "rotation": {
                                control.setRotation((int)value);
                                view.setRotation((int)value);
                                break;
                            }
                        }
                    }
                });
        this.erEngine = new EREngine(
                (mirror, value) -> uiUpdaterEngine.handleMirrorEffect(idTable, mirror, value),
                (anim) -> uiAnimatorEngine.animateUi(idTable, anim));
    }

    private void registerValueAnimator(String controlId, String property) {

    }

    public void buildUi(Controls.Control control) {
        Tuple<View, List<Pair<Controls.Control, View>>
                , CustomHashtable<String, Pair<Controls.Control, View>>> result =
                uiInitiatorEngine.buildViewTree(Controls.PanelCtrl.LayoutType.RELATIVE, control);
        View view = result.getItem1();
        idTable = result.getItem3();
        this.removeAllViews();
        this.addView(view);
    }

    public void buildUi(String json) {
        try {
            this.root = initMapper().readValue(json, Controls.Control.class);
            buildUi(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateUi(String json) {
        try {
            Updates.Update update = initMapper().readValue(json, Updates.Update.class);
            updateUi(update);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateUi(Updates.Update update) {
        uiUpdaterEngine.updateUi(idTable, update);
    }

    public void updateBatchUi(String json) {
        try {
            List<Updates.Update> updates = initMapper().readValue(json, new TypeReference<List<Updates.Update>>(){});
            updateBatchUi(updates);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void updateBatchUi(List<Updates.Update> updates) {
        uiUpdaterEngine.updateBatchUi(idTable, updates);
    }

    public void animateUi(Anims.Anim anim) {
        uiAnimatorEngine.animateUi(idTable, anim);
    }

    public void animateUi(String json) {
        try {
            Anims.Anim anim = initMapper().readValue(json, Anims.Anim.class);
            animateUi(anim);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void animateBatchUi(List<Anims.Anim> anims) {
        uiAnimatorEngine.animateBatchUi(idTable, anims);
    }

    public void animateBatchUi(String json) {
        try {
            List<Anims.Anim> anims = initMapper().readValue(json, new TypeReference<List<Anims.Anim>>(){});
            animateBatchUi(anims);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runCommand(Codes.Code code) {
        this.runCommands(Collections.singletonList(code));
    }

    public void runCommands(List<Codes.Code> codes) {
        this.erEngine.run(codes);
    }

    public void runCommand(String json) {
        try {
            Codes.Code code = initMapper().readValue(json, Codes.Code.class);
            runCommand(code);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void runCommands(String json) {
        try {
            List<Codes.Code> codes = initMapper().readValue(json, new TypeReference<List<Codes.Code>>(){});
            this.runCommands(codes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void modifyMirror(Bindings.Mirror mirror) {
        this.erEngine.modifyMirror(mirror);
    }

    public void modifyMirror(String json) {
        try {
            Bindings.Mirror mirror = initMapper().readValue(json, Bindings.Mirror.class);
            modifyMirror(mirror);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private ObjectMapper initMapper() {
        return new ObjectMapper()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
