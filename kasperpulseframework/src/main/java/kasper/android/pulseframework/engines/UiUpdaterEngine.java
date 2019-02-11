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
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.LineSet;
import com.db.chart.view.BaseBarChartView;
import com.db.chart.view.LineChartView;
import com.moos.library.CircleProgressView;
import com.moos.library.HorizontalProgressView;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import androidx.cardview.widget.CardView;
import androidx.core.widget.CompoundButtonCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kasper.android.pulseframework.adapters.DropDownAdapter;
import kasper.android.pulseframework.adapters.RecyclerAdapter;
import kasper.android.pulseframework.interfaces.IMainThreadRunner;
import kasper.android.pulseframework.locks.Locks;
import kasper.android.pulseframework.models.Bindings;
import kasper.android.pulseframework.models.Controls;
import kasper.android.pulseframework.models.Data;
import kasper.android.pulseframework.models.Exceptions;
import kasper.android.pulseframework.models.Tuple;
import kasper.android.pulseframework.models.Updates;
import kasper.android.pulseframework.utils.FieldValidator;
import kasper.android.pulseframework.utils.GraphicsHelper;
import tcking.github.com.giraffeplayer2.VideoView;

public class UiUpdaterEngine {

    private static final String TAG = "KasperPulseFramework";

    private Context context;
    private IMainThreadRunner mainThreadRunner;

    public UiUpdaterEngine(Context context, IMainThreadRunner mainThreadRunner) {
        this.context = context;
        this.mainThreadRunner = mainThreadRunner;
    }

    public void handleMirrorEffect(Hashtable<String, Pair<Controls.Control, View>> idTable
            , Bindings.Mirror mirror, Object value) {
        Updates.Update update = null;
        if (mirror instanceof Bindings.MirrorToX) {
            update = new Updates.ControlUpdateX();
            ((Updates.ControlUpdateX) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToY) {
            update = new Updates.ControlUpdateY();
            ((Updates.ControlUpdateY) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToWidth) {
            update = new Updates.ControlUpdateWidth();
            ((Updates.ControlUpdateWidth) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToHeight) {
            update = new Updates.ControlUpdateHeight();
            ((Updates.ControlUpdateHeight) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToBackColor) {
            update = new Updates.ControlUpdateBackColor();
            ((Updates.ControlUpdateBackColor) update).setValue(value + "");
        } else if (mirror instanceof Bindings.MirrorToBorderColor) {
            update = new Updates.ControlUpdateBorderColor();
            ((Updates.ControlUpdateBorderColor) update).setValue(value + "");
        } else if (mirror instanceof Bindings.MirrorToBorderWidth) {
            update = new Updates.ControlUpdateBorderWidth();
            ((Updates.ControlUpdateBorderWidth) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToMarginLeft) {
            update = new Updates.ControlUpdateMarginLeft();
            ((Updates.ControlUpdateMarginLeft) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToMarginRight) {
            update = new Updates.ControlUpdateMarginRight();
            ((Updates.ControlUpdateMarginRight) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToMarginTop) {
            update = new Updates.ControlUpdateMarginTop();
            ((Updates.ControlUpdateMarginTop) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToMarginBottom) {
            update = new Updates.ControlUpdateMarginBottom();
            ((Updates.ControlUpdateMarginBottom) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToPaddingLeft) {
            update = new Updates.ControlUpdatePaddingLeft();
            ((Updates.ControlUpdatePaddingLeft) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToPaddingTop) {
            update = new Updates.ControlUpdatePaddingTop();
            ((Updates.ControlUpdatePaddingTop) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToPaddingRight) {
            update = new Updates.ControlUpdatePaddingRight();
            ((Updates.ControlUpdatePaddingRight) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToPaddingBottom) {
            update = new Updates.ControlUpdatePaddingBottom();
            ((Updates.ControlUpdatePaddingBottom) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToElevation) {
            update = new Updates.ControlUpdateElevation();
            ((Updates.ControlUpdateElevation) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToRotationX) {
            update = new Updates.ControlUpdateRotationX();
            ((Updates.ControlUpdateRotationX) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToRotationY) {
            update = new Updates.ControlUpdateRotationY();
            ((Updates.ControlUpdateRotationY) update).setValue(convertDoubleToInt(value));
        } else if (mirror instanceof Bindings.MirrorToRotation) {
            update = new Updates.ControlUpdateRotation();
            ((Updates.ControlUpdateRotation) update).setValue(convertDoubleToInt(value + ""));
        }
        if (update != null) {
            update.setControlId(mirror.getCtrlName());
            updateUi(idTable, update);
        } else {
            new Exceptions.ELangException().printStackTrace();
        }
    }

    private int convertDoubleToInt(Object value) {
        String valueStr = value + "";
        if (valueStr.endsWith(".0"))
            valueStr = valueStr.substring(0, valueStr.length() - 2);
        return Integer.valueOf(valueStr);
    }

    @SuppressLint("RtlHardcoded")
    private void updateUiAsync(Controls.Control control, View view
            , Hashtable<String, Pair<Controls.Control, View>> idTable, Updates.Update update) {
        if (view != null) {
            if (update instanceof Updates.ControlUpdateWidth) {
                control.setWidth(((Updates.ControlUpdateWidth) update).getValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (((Updates.ControlUpdateWidth) update).getValue() != Controls.Control.MATCH_PARENT
                        && ((Updates.ControlUpdateWidth) update).getValue() != Controls.Control.WRAP_CONTENT)
                    params.width = GraphicsHelper.dpToPx(((Updates.ControlUpdateWidth) update).getValue());
                else
                    params.width = ((Updates.ControlUpdateWidth) update).getValue();
                view.setLayoutParams(params);
            } else if (update instanceof Updates.ControlUpdateHeight) {
                control.setWidth(((Updates.ControlUpdateHeight) update).getValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (((Updates.ControlUpdateHeight) update).getValue() != Controls.Control.MATCH_PARENT
                        && ((Updates.ControlUpdateHeight) update).getValue() != Controls.Control.WRAP_CONTENT)
                    params.height = GraphicsHelper.dpToPx(((Updates.ControlUpdateHeight) update).getValue());
                else
                    params.height = ((Updates.ControlUpdateHeight) update).getValue();
                view.setLayoutParams(params);
            } else if (update instanceof Updates.ControlUpdateX) {
                control.setX(((Updates.ControlUpdateX) update).getValue());
                if (((Updates.ControlUpdateX) update).getValue() != Controls.Control.CENTER)
                    view.setX(GraphicsHelper.dpToPx(((Updates.ControlUpdateX) update).getValue()));
                else
                    view.setX(((Updates.ControlUpdateX) update).getValue());
            } else if (update instanceof Updates.ControlUpdateY) {
                control.setY(((Updates.ControlUpdateY) update).getValue());
                if (((Updates.ControlUpdateY) update).getValue() != Controls.Control.CENTER)
                    view.setY(GraphicsHelper.dpToPx(((Updates.ControlUpdateY) update).getValue()));
                else
                    view.setY(((Updates.ControlUpdateY) update).getValue());
            } else if (update instanceof Updates.ControlUpdateBackColor) {
                control.setBackColor(((Updates.ControlUpdateBackColor) update).getValue());
                View target = getOrigin(view);
                target.setBackgroundColor(Color.parseColor(
                        ((Updates.ControlUpdateBackColor) update).getValue()));
            }
            if (update instanceof Updates.ControlUpdateBorderColor) {
                control.setBorderColor(((Updates.ControlUpdateBorderColor) update).getValue());
                ((CardView) view).setCardBackgroundColor(Color.parseColor(
                        ((Updates.ControlUpdateBorderColor) update).getValue()));
            } else if (update instanceof Updates.ControlUpdateBorderWidth) {
                control.setBorderWidth(((Updates.ControlUpdateBorderWidth) update).getValue());
                CardView.LayoutParams params = (CardView.LayoutParams)
                        ((ViewGroup) view).getChildAt(0).getLayoutParams();
                int bw = GraphicsHelper.dpToPx(((Updates.ControlUpdateBorderWidth) update).getValue());
                params.setMargins(bw, bw, bw, bw);
                ((ViewGroup) view).getChildAt(0).setLayoutParams(params);
            } else if (update instanceof Updates.ControlUpdateRotationX) {
                control.setRotationX(((Updates.ControlUpdateRotationX) update).getValue());
                view.setRotationX(((Updates.ControlUpdateRotationX) update).getValue());
            } else if (update instanceof Updates.ControlUpdateRotationY) {
                control.setRotationY(((Updates.ControlUpdateRotationY) update).getValue());
                view.setRotationY(((Updates.ControlUpdateRotationY) update).getValue());
            } else if (update instanceof Updates.ControlUpdateRotation) {
                control.setRotation(((Updates.ControlUpdateRotation) update).getValue());
                view.setRotation(((Updates.ControlUpdateRotation) update).getValue());
            } else if (update instanceof Updates.ControlUpdateCornerRadius) {
                control.setCornerRadius(((Updates.ControlUpdateCornerRadius) update).getValue());
                ((CardView) view).setRadius(GraphicsHelper.dpToPx(
                        ((Updates.ControlUpdateCornerRadius) update).getValue()));
                ((CardView) ((ViewGroup) view).getChildAt(0)).setRadius(GraphicsHelper.dpToPx(
                        ((Updates.ControlUpdateCornerRadius) update).getValue()));
            } else if (update instanceof Updates.ControlUpdateMarginLeft) {
                control.setMarginLeft(((Updates.ControlUpdateMarginLeft) update).getValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params instanceof RelativeLayout.LayoutParams) {
                    ((RelativeLayout.LayoutParams) params).setMargins(
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginLeft) update).getValue()),
                            ((RelativeLayout.LayoutParams) params).topMargin,
                            ((RelativeLayout.LayoutParams) params).rightMargin,
                            ((RelativeLayout.LayoutParams) params).bottomMargin);
                } else if (params instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) params).setMargins(
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginLeft) update).getValue()),
                            ((LinearLayout.LayoutParams) params).topMargin,
                            ((LinearLayout.LayoutParams) params).rightMargin,
                            ((LinearLayout.LayoutParams) params).bottomMargin);
                }
                view.setLayoutParams(params);
            } else if (update instanceof Updates.ControlUpdateMarginTop) {
                control.setMarginLeft(((Updates.ControlUpdateMarginTop) update).getValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params instanceof RelativeLayout.LayoutParams) {
                    ((RelativeLayout.LayoutParams) params).setMargins(
                            ((RelativeLayout.LayoutParams) params).leftMargin,
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginTop) update).getValue()),
                            ((RelativeLayout.LayoutParams) params).rightMargin,
                            ((RelativeLayout.LayoutParams) params).bottomMargin);
                } else if (params instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) params).setMargins(
                            ((LinearLayout.LayoutParams) params).leftMargin,
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginTop) update).getValue()),
                            ((LinearLayout.LayoutParams) params).rightMargin,
                            ((LinearLayout.LayoutParams) params).bottomMargin);
                }
                view.setLayoutParams(params);
            } else if (update instanceof Updates.ControlUpdateMarginRight) {
                control.setMarginLeft(((Updates.ControlUpdateMarginRight) update).getValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params instanceof RelativeLayout.LayoutParams) {
                    ((RelativeLayout.LayoutParams) params).setMargins(
                            ((RelativeLayout.LayoutParams) params).leftMargin,
                            ((RelativeLayout.LayoutParams) params).topMargin,
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginRight) update).getValue()),
                            ((RelativeLayout.LayoutParams) params).bottomMargin);
                } else if (params instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) params).setMargins(
                            ((LinearLayout.LayoutParams) params).leftMargin,
                            ((LinearLayout.LayoutParams) params).topMargin,
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginRight) update).getValue()),
                            ((LinearLayout.LayoutParams) params).bottomMargin);
                }
                view.setLayoutParams(params);
            } else if (update instanceof Updates.ControlUpdateMarginBottom) {
                control.setMarginLeft(((Updates.ControlUpdateMarginBottom) update).getValue());
                ViewGroup.LayoutParams params = view.getLayoutParams();
                if (params instanceof RelativeLayout.LayoutParams) {
                    ((RelativeLayout.LayoutParams) params).setMargins(
                            ((RelativeLayout.LayoutParams) params).leftMargin,
                            ((RelativeLayout.LayoutParams) params).topMargin,
                            ((RelativeLayout.LayoutParams) params).rightMargin,
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginBottom) update).getValue()));
                } else if (params instanceof LinearLayout.LayoutParams) {
                    ((LinearLayout.LayoutParams) params).setMargins(
                            ((LinearLayout.LayoutParams) params).leftMargin,
                            ((LinearLayout.LayoutParams) params).topMargin,
                            ((LinearLayout.LayoutParams) params).rightMargin,
                            GraphicsHelper.dpToPx(
                                    ((Updates.ControlUpdateMarginBottom) update).getValue()));
                }
                view.setLayoutParams(params);
            } else if (update instanceof Updates.ControlUpdatePaddingLeft) {
                control.setPaddingLeft(((Updates.ControlUpdatePaddingLeft) update).getValue());
                view.setPadding(
                        GraphicsHelper.dpToPx(
                                ((Updates.ControlUpdatePaddingLeft) update).getValue()),
                        view.getPaddingTop(),
                        view.getPaddingRight(),
                        view.getPaddingBottom());
            } else if (update instanceof Updates.ControlUpdatePaddingTop) {
                control.setPaddingLeft(((Updates.ControlUpdatePaddingTop) update).getValue());
                view.setPadding(
                        view.getPaddingLeft(),
                        GraphicsHelper.dpToPx(
                                ((Updates.ControlUpdatePaddingTop) update).getValue()),
                        view.getPaddingRight(),
                        view.getPaddingBottom());
            } else if (update instanceof Updates.ControlUpdatePaddingRight) {
                control.setPaddingLeft(((Updates.ControlUpdatePaddingRight) update).getValue());
                view.setPadding(
                        view.getPaddingLeft(),
                        view.getPaddingTop(),
                        GraphicsHelper.dpToPx(
                                ((Updates.ControlUpdatePaddingRight) update).getValue()),
                        view.getPaddingBottom());
            } else if (update instanceof Updates.ControlUpdatePaddingBottom) {
                control.setPaddingLeft(((Updates.ControlUpdatePaddingBottom) update).getValue());
                view.setPadding(
                        view.getPaddingLeft(),
                        view.getPaddingTop(),
                        view.getPaddingRight(),
                        GraphicsHelper.dpToPx(
                                ((Updates.ControlUpdatePaddingBottom) update).getValue()));
            } else if (update instanceof Updates.ControlUpdateElevation) {
                control.setElevation(((Updates.ControlUpdateElevation) update).getValue());
                view.setElevation(GraphicsHelper.dpToPx(
                        ((Updates.ControlUpdateElevation) update).getValue()));
            } else if (update instanceof Updates.PanelCtrlAddControl) {
                if (control instanceof Controls.PanelCtrl) {
                    ((Controls.PanelCtrl) control).getControls().add(
                            ((Updates.PanelCtrlAddControl) update).getValue());
                    Tuple<View, List<Pair<Controls.Control, View>>
                            , Hashtable<String, Pair<Controls.Control, View>>> buildResult =
                            new UiInitiatorEngine(context, mainThreadRunner).buildViewTree(
                                    ((Controls.PanelCtrl) control).getLayoutType(),
                                    ((Updates.PanelCtrlAddControl) update).getValue());
                    idTable.putAll(buildResult.getItem3());
                    ((ViewGroup) view).addView(buildResult.getItem1());
                }
            } else if (update instanceof Updates.TextCtrlUpdateText) {
                if (control instanceof Controls.TextCtrl) {
                    ((Controls.TextCtrl) control).setText(
                            ((Updates.TextCtrlUpdateText) update).getValue());
                    view = getOrigin(view);
                    ((TextView) view).setText(((Updates.TextCtrlUpdateText) update).getValue());
                }
            } else if (update instanceof Updates.TextCtrlUpdateTextSize) {
                if (control instanceof Controls.TextCtrl) {
                    ((Controls.TextCtrl) control).setTextSize(
                            ((Updates.TextCtrlUpdateTextSize) update).getValue());
                    view = getOrigin(view);
                    ((TextView) view).setTextSize(((Updates.TextCtrlUpdateTextSize) update).getValue());
                }
            } else if (update instanceof Updates.TextCtrlUpdateTextColor) {
                if (control instanceof Controls.TextCtrl) {
                    ((Controls.TextCtrl) control).setTextColor(
                            ((Updates.TextCtrlUpdateTextColor) update).getValue());
                    view = getOrigin(view);
                    ((TextView) view).setTextColor(Color.parseColor(
                            ((Updates.TextCtrlUpdateTextColor) update).getValue()));
                }
            } else if (update instanceof Updates.TextCtrlUpdateGravityType) {
                if (control instanceof Controls.TextCtrl) {
                    ((Controls.TextCtrl) control).setGravityType(
                            ((Updates.TextCtrlUpdateGravityType) update).getValue());
                    view = getOrigin(view);
                    Updates.TextCtrlUpdateGravityType updateGravityType =
                            (Updates.TextCtrlUpdateGravityType) update;
                    if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.LEFT) {
                        ((TextView) view).setGravity(Gravity.LEFT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.TOP) {
                        ((TextView) view).setGravity(Gravity.TOP);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.RIGHT) {
                        ((TextView) view).setGravity(Gravity.RIGHT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.BOTTOM) {
                        ((TextView) view).setGravity(Gravity.BOTTOM);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.TOP_LEFT) {
                        ((TextView) view).setGravity(Gravity.TOP | Gravity.LEFT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.TOP_RIGHT) {
                        ((TextView) view).setGravity(Gravity.TOP | Gravity.RIGHT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.BOTTOM_LEFT) {
                        ((TextView) view).setGravity(Gravity.BOTTOM | Gravity.LEFT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.BOTTOM_RIGHT) {
                        ((TextView) view).setGravity(Gravity.BOTTOM | Gravity.RIGHT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.LEFT_CENTER_VERTICAL) {
                        ((TextView) view).setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.RIGHT_CENTER_VERTICAL) {
                        ((TextView) view).setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.TOP_CENTER_HORIZONTAL) {
                        ((TextView) view).setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.BOTTOM_CENTER_HORIZONTAL) {
                        ((TextView) view).setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);
                    } else if (updateGravityType.getValue() == Controls.TextCtrl.GravityType.CENTER) {
                        ((TextView) view).setGravity(Gravity.CENTER);
                    }
                }
            } else if (update instanceof Updates.ImageCtrlUpdateImageUrl) {
                if (control instanceof Controls.ImageCtrl) {
                    ((Controls.ImageCtrl) control).setImageUrl(
                            ((Updates.ImageCtrlUpdateImageUrl) update).getValue());
                    view = getOrigin(view);
                    Glide.with(context)
                            .load(((Updates.ImageCtrlUpdateImageUrl) update).getValue())
                            .into((ImageView) view);
                }
            } else if (update instanceof Updates.ImageCtrlUpdateScaleType) {
                if (control instanceof Controls.ImageCtrl) {
                    ((Controls.ImageCtrl) control).setScaleType(
                            ((Updates.ImageCtrlUpdateScaleType) update).getValue());
                    view = getOrigin(view);
                    ((ImageView) view).setScaleType(ImageView.ScaleType.valueOf(
                            ((Updates.ImageCtrlUpdateScaleType) update).getValue().toString()));
                }
            } else if (update instanceof Updates.InputFieldCtrlUpdateHint) {
                if (control instanceof Controls.InputFieldCtrl) {
                    ((Controls.InputFieldCtrl) control).setHint(
                            ((Updates.InputFieldCtrlUpdateHint) update).getValue());
                    view = getOrigin(view);
                    ((EditText) view).setHint(((Updates.InputFieldCtrlUpdateHint) update).getValue());
                }
            } else if (update instanceof Updates.InputFieldCtrlUpdateHintColor) {
                if (control instanceof Controls.InputFieldCtrl) {
                    ((Controls.InputFieldCtrl) control).setHintColor(
                            ((Updates.InputFieldCtrlUpdateHintColor) update).getValue());
                    view = getOrigin(view);
                    ((EditText) view).setHintTextColor(Color.parseColor(
                            ((Updates.InputFieldCtrlUpdateHintColor) update).getValue()));
                }
            } else if (update instanceof Updates.InputFieldCtrlUpdateText) {
                if (control instanceof Controls.InputFieldCtrl) {
                    ((Controls.InputFieldCtrl) control).setText(
                            ((Updates.InputFieldCtrlUpdateText) update).getValue());
                    view = getOrigin(view);
                    ((EditText) view).setText(
                            ((Updates.InputFieldCtrlUpdateText) update).getValue());
                }
            } else if (update instanceof Updates.InputFieldCtrlUpdateTextColor) {
                if (control instanceof Controls.InputFieldCtrl) {
                    ((Controls.InputFieldCtrl) control).setTextColor(
                            ((Updates.InputFieldCtrlUpdateTextColor) update).getValue());
                    view = getOrigin(view);
                    ((EditText) view).setTextColor(Color.parseColor(
                            ((Updates.InputFieldCtrlUpdateTextColor) update).getValue()));
                }
            } else if (update instanceof Updates.InputFieldCtrlUpdateTextSize) {
                if (control instanceof Controls.InputFieldCtrl) {
                    ((Controls.InputFieldCtrl) control).setTextSize(
                            ((Updates.InputFieldCtrlUpdateTextSize) update).getValue());
                    view = getOrigin(view);
                    ((EditText) view).setTextSize(
                            ((Updates.InputFieldCtrlUpdateTextSize) update).getValue());
                }
            } else if (update instanceof Updates.InputFieldCtrlUpdateLineColor) {
                if (control instanceof Controls.InputFieldCtrl) {
                    ((Controls.InputFieldCtrl) control).setLineColor(
                            ((Updates.InputFieldCtrlUpdateLineColor) update).getValue());
                    view = getOrigin(view);
                    view.getBackground().setColorFilter(Color.parseColor(
                            ((Updates.InputFieldCtrlUpdateLineColor) update).getValue())
                            , PorterDuff.Mode.SRC_ATOP);
                }
            } else if (update instanceof Updates.ButtonCtrlUpdateCaption) {
                if (control instanceof Controls.ButtonCtrl) {
                    ((Controls.ButtonCtrl) control).setCaption(
                            ((Updates.ButtonCtrlUpdateCaption) update).getValue());
                    view = getOrigin(view);
                    ((Button) view).setText(((Updates.ButtonCtrlUpdateCaption) update).getValue());
                }
            } else if (update instanceof Updates.ButtonCtrlUpdateCaptionSize) {
                if (control instanceof Controls.ButtonCtrl) {
                    ((Controls.ButtonCtrl) control).setCaptionSize(
                            ((Updates.ButtonCtrlUpdateCaptionSize) update).getValue());
                    view = getOrigin(view);
                    ((Button) view).setTextSize(((Updates.ButtonCtrlUpdateCaptionSize) update).getValue());
                }
            } else if (update instanceof Updates.ButtonCtrlUpdateCaptionColor) {
                if (control instanceof Controls.ButtonCtrl) {
                    ((Controls.ButtonCtrl) control).setCaptionColor(
                            ((Updates.ButtonCtrlUpdateCaptionColor) update).getValue());
                    view = getOrigin(view);
                    ((Button) view).setTextColor(Color.parseColor(
                            ((Updates.ButtonCtrlUpdateCaptionColor) update).getValue()));
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateCircleBroken) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setCircleBroken(
                            ((Updates.ProgressCtrlUpdateCircleBroken) update).isValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setCircleBroken(
                                ((Updates.ProgressCtrlUpdateCircleBroken) update).isValue());
                    } else if (view instanceof HorizontalProgressView) {
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateFillEnabled) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setFillEnabled(
                            ((Updates.ProgressCtrlUpdateFillEnabled) update).isValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setFillEnabled(
                                ((Updates.ProgressCtrlUpdateFillEnabled) update).isValue());
                    } else if (view instanceof HorizontalProgressView) {
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateGraduatedEnabled) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setGraduatedEnabled(
                            ((Updates.ProgressCtrlUpdateGraduatedEnabled) update).isValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setGraduatedEnabled(
                                ((Updates.ProgressCtrlUpdateGraduatedEnabled) update).isValue());
                    } else if (view instanceof HorizontalProgressView) {
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateProgressCornerRadius) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setProgressCornerRadius(
                            ((Updates.ProgressCtrlUpdateProgressCornerRadius) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        Log.d(TAG, "Illegal Update");
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setProgressCornerRadius(
                                ((Updates.ProgressCtrlUpdateProgressCornerRadius) update).getValue());
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateProgressTextColor) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setProgressTextColor(
                            ((Updates.ProgressCtrlUpdateProgressTextColor) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setProgressTextColor(Color.parseColor(
                                ((Updates.ProgressCtrlUpdateProgressTextColor) update).getValue()));
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setProgressTextColor(Color.parseColor(
                                ((Updates.ProgressCtrlUpdateProgressTextColor) update).getValue()));
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateProgressTextMoved) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setProgressTextMoved(
                            ((Updates.ProgressCtrlUpdateProgressTextMoved) update).isValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        Log.d(TAG, "Illegal Update");
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setProgressTextMoved(
                                ((Updates.ProgressCtrlUpdateProgressTextMoved) update).isValue());
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateProgressTextPaddingBottom) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setProgressTextPaddingBottom(
                            ((Updates.ProgressCtrlUpdateProgressTextPaddingBottom) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        Log.d(TAG, "Illegal Update");
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setProgressTextPaddingBottom(
                                ((Updates.ProgressCtrlUpdateProgressTextPaddingBottom) update).getValue());
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateProgressTextSize) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setProgressTextSize(
                            ((Updates.ProgressCtrlUpdateProgressTextSize) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setProgressTextSize(
                                ((Updates.ProgressCtrlUpdateProgressTextSize) update).getValue());
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setProgressTextSize(
                                ((Updates.ProgressCtrlUpdateProgressTextSize) update).getValue());
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateProgressTextVisibility) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setProgressTextVisibility(
                            ((Updates.ProgressCtrlUpdateProgressTextVisibility) update).isValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setProgressTextVisibility(
                                ((Updates.ProgressCtrlUpdateProgressTextVisibility) update).isValue());
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setProgressTextVisibility(
                                ((Updates.ProgressCtrlUpdateProgressTextVisibility) update).isValue());
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateScaleZoneCornerRadius) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setScaleZoneCornerRadius(
                            ((Updates.ProgressCtrlUpdateScaleZoneCornerRadius) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setScaleZoneCornerRadius(
                                ((Updates.ProgressCtrlUpdateScaleZoneCornerRadius) update).getValue());
                    } else if (view instanceof HorizontalProgressView) {
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateScaleZoneLength) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setScaleZoneLength(
                            ((Updates.ProgressCtrlUpdateScaleZoneLength) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setScaleZoneLength(
                                ((Updates.ProgressCtrlUpdateScaleZoneLength) update).getValue());
                    } else if (view instanceof HorizontalProgressView) {
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateScaleZonePadding) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setScaleZonePadding(
                            ((Updates.ProgressCtrlUpdateScaleZonePadding) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setScaleZonePadding(
                                ((Updates.ProgressCtrlUpdateScaleZonePadding) update).getValue());
                    } else if (view instanceof HorizontalProgressView) {
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateScaleZoneWidth) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setScaleZoneWidth(
                            ((Updates.ProgressCtrlUpdateScaleZoneWidth) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setScaleZoneWidth(
                                ((Updates.ProgressCtrlUpdateScaleZoneWidth) update).getValue());
                    } else if (view instanceof HorizontalProgressView) {
                        Log.d(TAG, "Illegal Update");
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateTrackColor) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setTrackColor(
                            ((Updates.ProgressCtrlUpdateTrackColor) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setTrackColor(Color.parseColor(
                                ((Updates.ProgressCtrlUpdateTrackColor) update).getValue()));
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setTrackColor(Color.parseColor(
                                ((Updates.ProgressCtrlUpdateTrackColor) update).getValue()));
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateTrackEnabled) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setTrackEnabled(
                            ((Updates.ProgressCtrlUpdateTrackEnabled) update).isValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setTrackEnabled(
                                ((Updates.ProgressCtrlUpdateTrackEnabled) update).isValue());
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setTrackEnabled(
                                ((Updates.ProgressCtrlUpdateTrackEnabled) update).isValue());
                    }
                }
            } else if (update instanceof Updates.ProgressCtrlUpdateTrackWidth) {
                if (control instanceof Controls.ProgressCtrl) {
                    ((Controls.ProgressCtrl) control).setTrackWidth(
                            ((Updates.ProgressCtrlUpdateTrackWidth) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CircleProgressView) {
                        ((CircleProgressView) view).setTrackWidth(
                                ((Updates.ProgressCtrlUpdateTrackWidth) update).getValue());
                    } else if (view instanceof HorizontalProgressView) {
                        ((HorizontalProgressView) view).setTrackWidth(
                                ((Updates.ProgressCtrlUpdateTrackWidth) update).getValue());
                    }
                }
            } else if (update instanceof Updates.VideoPlayerCtrlUpdateVideoUrl) {
                if (control instanceof Controls.VideoPlayerCtrl) {
                    ((Controls.VideoPlayerCtrl) control).setVideoUrl(
                            ((Updates.VideoPlayerCtrlUpdateVideoUrl) update).getValue());
                    view = getOrigin(view);
                    ((VideoView) view).getPlayer().stop();
                    ((VideoView) view).setVideoPath(
                            Uri.parse(((Updates.VideoPlayerCtrlUpdateVideoUrl) update)
                                    .getValue()).toString());
                    if (((Controls.VideoPlayerCtrl) control).isPlaying())
                        ((VideoView) view).getPlayer().start();
                }
            } else if (update instanceof Updates.VideoPlayerCtrlUpdateProgress) {
                if (control instanceof Controls.VideoPlayerCtrl) {
                    ((Controls.VideoPlayerCtrl) control).setProgress(
                            ((Updates.VideoPlayerCtrlUpdateProgress) update).getValue());
                    view = getOrigin(view);
                    ((VideoView) view).getPlayer().seekTo(
                            ((Updates.VideoPlayerCtrlUpdateProgress) update).getValue());
                }
            } else if (update instanceof Updates.VideoPlayerCtrlUpdatePlaying) {
                if (control instanceof Controls.VideoPlayerCtrl) {
                    ((Controls.VideoPlayerCtrl) control).setPlaying(
                            ((Updates.VideoPlayerCtrlUpdatePlaying) update).isValue());
                    view = getOrigin(view);
                    if (((Updates.VideoPlayerCtrlUpdatePlaying) update).isValue())
                        ((VideoView) view).getPlayer().start();
                    else
                        ((VideoView) view).getPlayer().stop();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateAxisColor) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setAxisColor(
                            ((Updates.LineChartCtrlUpdateAxisColor) update).getValue());
                    view = getOrigin(view);
                    ((LineChartView) view).setAxisColor(Color.parseColor(
                            ((Updates.LineChartCtrlUpdateAxisColor) update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateDotsColor) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setDotsColor(
                            ((Updates.LineChartCtrlUpdateDotsColor) update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setDotsColor(
                            Color.parseColor(((Updates.LineChartCtrlUpdateDotsColor) update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateDotsRadius) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setDotsRadius(
                            ((Updates.LineChartCtrlUpdateDotsRadius) update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setDotsRadius(
                            GraphicsHelper.dpToPx(((Updates.LineChartCtrlUpdateDotsRadius)
                                    update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateDotsStrokeColor) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setDotsStrokeColor(
                            ((Updates.LineChartCtrlUpdateDotsStrokeColor) update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setDotsStrokeColor(
                            Color.parseColor(((Updates.LineChartCtrlUpdateDotsStrokeColor)
                                    update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateDotsStrokeThickness) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setDotsStrokeThickness(
                            ((Updates.LineChartCtrlUpdateDotsStrokeThickness)
                                    update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setDotsStrokeThickness(
                            GraphicsHelper.dpToPx(((Updates.LineChartCtrlUpdateDotsStrokeThickness)
                                    update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateFillColor) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setFillColor(
                            ((Updates.LineChartCtrlUpdateFillColor) update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setFill(
                            Color.parseColor(((Updates.LineChartCtrlUpdateFillColor) update)
                                    .getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateGradients) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setGradientColors(
                            ((Updates.LineChartCtrlUpdateGradients) update).getValue());
                    ((Controls.LineChartCtrl) control).setGradientValues(
                            ((Updates.LineChartCtrlUpdateGradients) update).getValue2());
                    view = getOrigin(view);
                    List<Data.StringValue> gradientColorsList =
                            ((Updates.LineChartCtrlUpdateGradients) update).getValue();
                    List<Data.FloatValue> gradientValuesList =
                            ((Updates.LineChartCtrlUpdateGradients) update).getValue2();
                    int[] gradientColors = new int[gradientColorsList.size()];
                    float[] gradientValues = new float[gradientValuesList.size()];
                    int counter = 0;
                    Iterator<Data.StringValue> colorIterator = gradientColorsList.iterator();
                    Iterator<Data.FloatValue> valueIterator = gradientValuesList.iterator();
                    while (colorIterator.hasNext() && valueIterator.hasNext()) {
                        Data.StringValue color = colorIterator.next();
                        Data.FloatValue value = valueIterator.next();
                        gradientColors[counter] = Color.parseColor(color.getValue());
                        gradientValues[counter] = value.getValue();
                        counter++;
                    }
                    LineSet lineSet = (LineSet) ((LineChartView) view).getData().get(0);
                    if (gradientColors.length > 0)
                        lineSet.setGradientFill(gradientColors, gradientValues);
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateLabelsColor) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setLabelsColor(
                            ((Updates.LineChartCtrlUpdateLabelsColor) update).getValue());
                    view = getOrigin(view);
                    ((LineChartView) view).setLabelsColor(Color.parseColor(
                            ((Updates.LineChartCtrlUpdateLabelsColor) update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateLineBeginAt) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setLineBeginAt(
                            ((Updates.LineChartCtrlUpdateLineBeginAt) update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).beginAt(
                            ((Updates.LineChartCtrlUpdateLineBeginAt) update).getValue());
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateLineColor) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setLineColor(
                            ((Updates.LineChartCtrlUpdateLineColor) update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setColor(Color.parseColor(
                            ((Updates.LineChartCtrlUpdateLineColor) update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateLineDashedIntervals) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setLineDashedIntervals(
                            ((Updates.LineChartCtrlUpdateLineDashedIntervals) update).getValue());
                    view = getOrigin(view);
                    List<Data.FloatValue> dashedIntervalsList =
                            ((Updates.LineChartCtrlUpdateLineDashedIntervals) update).getValue();
                    float[] lineDashedIntervals = new float[dashedIntervalsList.size()];
                    int counter = 0;
                    for (Data.FloatValue floatValue : dashedIntervalsList) {
                        lineDashedIntervals[counter] = floatValue.getValue();
                        counter++;
                    }
                    ((LineSet) ((LineChartView) view).getData().get(0)).setDashed(lineDashedIntervals);
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateLineEndAt) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setLineEndAt(
                            ((Updates.LineChartCtrlUpdateLineEndAt) update).getValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).endAt(
                            ((Updates.LineChartCtrlUpdateLineEndAt) update).getValue());
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateLineSmooth) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setLineSmooth(
                            ((Updates.LineChartCtrlUpdateLineSmooth) update).isValue());
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setSmooth(
                            ((Updates.LineChartCtrlUpdateLineSmooth) update).isValue());
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdateLineThickness) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setLineThickness(
                            GraphicsHelper.dpToPx(((Updates.LineChartCtrlUpdateLineThickness)
                                    update).getValue()));
                    view = getOrigin(view);
                    ((LineSet) ((LineChartView) view).getData().get(0)).setThickness(
                            GraphicsHelper.dpToPx(((Updates.LineChartCtrlUpdateLineThickness)
                                    update).getValue()));
                    ((LineChartView) view).notifyDataUpdate();
                    ((LineChartView) view).show();
                }
            } else if (update instanceof Updates.LineChartCtrlUpdatePoints) {
                if (control instanceof Controls.LineChartCtrl) {
                    ((Controls.LineChartCtrl) control).setPoints(
                            ((Updates.LineChartCtrlUpdatePoints) update).getValue());
                    view = getOrigin(view);
                    LineChartView lineChartView = (LineChartView) view;
                    Controls.LineChartCtrl chartEl = (Controls.LineChartCtrl) control;
                    LineSet dataset = new UiInitiatorEngine(context, mainThreadRunner)
                            .initLineChartView(chartEl);
                    lineChartView.getData().remove(0);
                    lineChartView.addData(dataset);
                    lineChartView.notifyDataUpdate();
                    lineChartView.show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdateAxisColor) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setAxisColor(
                            ((Updates.BarChartCtrlUpdateAxisColor) update).getValue());
                    view = getOrigin(view);
                    ((BaseBarChartView) view).setAxisColor(Color.parseColor(
                            ((Updates.BarChartCtrlUpdateAxisColor) update).getValue()));
                    ((BaseBarChartView) view).notifyDataUpdate();
                    ((BaseBarChartView) view).show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdateBarBackgroundColor) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setBarBackgroundColor(
                            ((Updates.BarChartCtrlUpdateBarBackgroundColor) update).getValue());
                    view = getOrigin(view);
                    ((BaseBarChartView) view).setBarBackgroundColor(Color.parseColor(
                            ((Updates.BarChartCtrlUpdateBarBackgroundColor) update).getValue()));
                    ((BaseBarChartView) view).notifyDataUpdate();
                    ((BaseBarChartView) view).show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdateBarsColor) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setBarsColor(
                            ((Updates.BarChartCtrlUpdateBarsColor) update).getValue());
                    view = getOrigin(view);
                    ((BarSet) ((BaseBarChartView) view).getData().get(0)).setColor(Color.parseColor(
                            ((Updates.BarChartCtrlUpdateBarsColor) update).getValue()));
                    ((BaseBarChartView) view).notifyDataUpdate();
                    ((BaseBarChartView) view).show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdateBarSpacing) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setBarSpacing(
                            ((Updates.BarChartCtrlUpdateBarSpacing) update).getValue());
                    view = getOrigin(view);
                    ((BaseBarChartView) view).setBarSpacing(GraphicsHelper.dpToPx(
                            ((Updates.BarChartCtrlUpdateBarSpacing) update).getValue()));
                    ((BaseBarChartView) view).notifyDataUpdate();
                    ((BaseBarChartView) view).show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdateLabelsColor) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setLabelsColor(
                            ((Updates.BarChartCtrlUpdateLabelsColor) update).getValue());
                    view = getOrigin(view);
                    ((BaseBarChartView) view).setLabelsColor(Color.parseColor(
                            ((Updates.BarChartCtrlUpdateLabelsColor) update).getValue()));
                    ((BaseBarChartView) view).notifyDataUpdate();
                    ((BaseBarChartView) view).show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdateRoundCorners) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setRoundCorners(
                            ((Updates.BarChartCtrlUpdateRoundCorners) update).getValue());
                    view = getOrigin(view);
                    ((BaseBarChartView) view).setRoundCorners(GraphicsHelper.dpToPx(
                            ((Updates.BarChartCtrlUpdateRoundCorners) update).getValue()));
                    ((BaseBarChartView) view).notifyDataUpdate();
                    ((BaseBarChartView) view).show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdateSetSpacing) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setSetSpacing(
                            ((Updates.BarChartCtrlUpdateSetSpacing) update).getValue());
                    view = getOrigin(view);
                    ((BaseBarChartView) view).setSetSpacing(GraphicsHelper.dpToPx(
                            ((Updates.BarChartCtrlUpdateSetSpacing) update).getValue()));
                    ((BaseBarChartView) view).notifyDataUpdate();
                    ((BaseBarChartView) view).show();
                }
            } else if (update instanceof Updates.BarChartCtrlUpdatePoints) {
                if (control instanceof Controls.BarChartCtrl) {
                    ((Controls.BarChartCtrl) control).setPoints(
                            ((Updates.BarChartCtrlUpdatePoints) update).getValue());
                    ((Controls.BarChartCtrl) control).setBarColors(
                            ((Updates.BarChartCtrlUpdatePoints) update).getValue2());
                    view = getOrigin(view);
                    Controls.BarChartCtrl chartEl = (Controls.BarChartCtrl) control;
                    BaseBarChartView barChartView = (BaseBarChartView) view;
                    BarSet dataset = new BarSet();
                    Iterator<Data.Point> pointIterator = chartEl.getPoints().iterator();
                    Iterator<Data.StringValue> barColorIterator = chartEl.getBarColors().iterator();
                    while (pointIterator.hasNext() && barColorIterator.hasNext()) {
                        Data.Point point = pointIterator.next();
                        Data.StringValue barColor = barColorIterator.next();
                        Bar bar = new Bar(point.getLabel(), point.getValue());
                        bar.setColor(Color.parseColor(barColor.getValue()));
                        dataset.addBar(bar);
                    }
                    barChartView.getData().remove(0);
                    barChartView.addData(dataset);
                    barChartView.notifyDataUpdate();
                    barChartView.show();
                }
            } else if (update instanceof Updates.ScrollerCtrlUpdatePosition) {
                if (control instanceof Controls.ScrollerCtrl) {
                    ((Controls.ScrollerCtrl) control).setPosition(
                            ((Updates.ScrollerCtrlUpdatePosition) update).getValue());
                    view = getOrigin(view);
                    view.scrollTo(0, GraphicsHelper.dpToPx(
                            ((Updates.ScrollerCtrlUpdatePosition) update).getValue()));
                }
            } else if (update instanceof Updates.CompoundCtrlUpdateCaption) {
                if (control instanceof Controls.CheckCtrl) {
                    ((Controls.CheckCtrl) control).setCaption(
                            ((Updates.CompoundCtrlUpdateCaption) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CheckBox)
                        ((CheckBox) view).setText(((Updates.CompoundCtrlUpdateCaption) update).getValue());
                    else if (view instanceof RadioButton)
                        ((RadioButton) view).setText(((Updates.CompoundCtrlUpdateCaption) update).getValue());
                }
            } else if (update instanceof Updates.CompoundCtrlUpdateCaptionSize) {
                if (control instanceof Controls.CheckCtrl) {
                    ((Controls.CheckCtrl) control).setCaptionSize(
                            ((Updates.CompoundCtrlUpdateCaptionSize) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CheckBox)
                        ((CheckBox) view).setTextSize(TypedValue.COMPLEX_UNIT_SP,
                                ((Updates.CompoundCtrlUpdateCaptionSize) update).getValue());
                    else if (view instanceof RadioButton)
                        ((RadioButton) view).setTextSize(TypedValue.COMPLEX_UNIT_SP,
                                ((Updates.CompoundCtrlUpdateCaptionSize) update).getValue());
                }
            } else if (update instanceof Updates.CompoundCtrlUpdateCaptionColor) {
                if (control instanceof Controls.CheckCtrl) {
                    ((Controls.CheckCtrl) control).setCaptionColor(
                            ((Updates.CompoundCtrlUpdateCaptionColor) update).getValue());
                    view = getOrigin(view);
                    if (view instanceof CheckBox)
                        ((CheckBox) view).setTextColor(Color.parseColor(
                                ((Updates.CompoundCtrlUpdateCaptionColor) update).getValue()));
                    else if (view instanceof RadioButton)
                        ((RadioButton) view).setTextColor(Color.parseColor(
                                ((Updates.CompoundCtrlUpdateCaptionColor) update).getValue()));
                }
            } else if (update instanceof Updates.CompoundCtrlUpdateTintColor) {
                if (control instanceof Controls.CheckCtrl) {
                    ((Controls.CheckCtrl) control).setTintColor(
                            ((Updates.CompoundCtrlUpdateTintColor) update).getValue());
                    view = getOrigin(view);
                    int color = Color.parseColor(
                            ((Updates.CompoundCtrlUpdateTintColor) update).getValue());

                    if (view instanceof CheckBox) {
                        ColorStateList colorStateList = new ColorStateList(
                                new int[][]{
                                        new int[]{-android.R.attr.state_checked}, // unchecked
                                        new int[]{android.R.attr.state_checked}, // checked
                                },
                                new int[]{color, color}
                        );
                        CompoundButtonCompat.setButtonTintList(((CheckBox) view), colorStateList);
                    } else if (view instanceof RadioButton) {
                        ColorStateList colorStateList = new ColorStateList(
                                new int[][]{
                                        new int[]{-android.R.attr.state_checked}, // unchecked
                                        new int[]{android.R.attr.state_checked}, // checked
                                },
                                new int[]{color, color}
                        );
                        CompoundButtonCompat.setButtonTintList(((RadioButton) view), colorStateList);
                    }
                }
            } else if (update instanceof Updates.CompoundCtrlUpdateChecked) {
                if (control instanceof Controls.CheckCtrl) {
                    ((Controls.CheckCtrl) control).setChecked(
                            ((Updates.CompoundCtrlUpdateChecked) update).isValue());
                    view = getOrigin(view);
                    if (view instanceof CheckBox)
                        ((CheckBox) view).setChecked(((Updates.CompoundCtrlUpdateChecked) update).isValue());
                    else if (view instanceof RadioButton)
                        ((RadioButton) view).setChecked(((Updates.CompoundCtrlUpdateChecked) update).isValue());
                }
            } else if (update instanceof Updates.DropDownCtrlAddItem) {
                if (control instanceof Controls.DropDownCtrl) {
                    ((Controls.DropDownCtrl) control).getItems().add(
                            ((Updates.DropDownCtrlAddItem) update).getValue());
                    view = getOrigin(view);
                    ((DropDownAdapter) ((Spinner) view).getAdapter()).addItem(
                            ((Updates.DropDownCtrlAddItem) update).getValue());
                }
            } else if (update instanceof Updates.DropDownCtrlUpdateSelectedPos) {
                if (control instanceof Controls.DropDownCtrl) {
                    ((Controls.DropDownCtrl) control).setSelectedPos(
                            ((Updates.DropDownCtrlUpdateSelectedPos) update).getValue());
                    view = getOrigin(view);
                    ((Spinner) view).setSelection(
                            ((Updates.DropDownCtrlUpdateSelectedPos) update).getValue());
                }
            } else if (update instanceof Updates.RecyclerCtrlAddItem) {
                if (control instanceof Controls.RecyclerCtrl) {
                    ((Controls.RecyclerCtrl) control).getItems().add(
                            ((Updates.RecyclerCtrlAddItem) update).getValue(),
                            ((Updates.RecyclerCtrlAddItem) update).getValue2());
                    view = getOrigin(view);
                    ((RecyclerAdapter) Objects.requireNonNull(
                            ((RecyclerView) view).getAdapter())).addItem(
                            ((Updates.RecyclerCtrlAddItem) update).getValue(),
                            ((Updates.RecyclerCtrlAddItem) update).getValue2());
                }
            } else if (update instanceof Updates.RecyclerCtrlUpdateItem) {
                if (control instanceof Controls.RecyclerCtrl) {
                    view = getOrigin(view);
                    Objects.requireNonNull(((RecyclerView) view).getAdapter()).notifyItemChanged(
                            ((Updates.RecyclerCtrlUpdateItem) update).getValue());
                }
            } else if (update instanceof Updates.RecyclerCtrlRemoveItem) {
                if (control instanceof Controls.RecyclerCtrl) {
                    ((Controls.RecyclerCtrl) control).getItems().remove(
                            ((Updates.RecyclerCtrlRemoveItem) update).getValue());
                    view = getOrigin(view);
                    ((RecyclerAdapter) Objects.requireNonNull(
                            ((RecyclerView) view).getAdapter())).removeItem(
                            ((Updates.RecyclerCtrlRemoveItem) update).getValue());
                }
            } else if (update instanceof Updates.RecyclerCtrlUpdateRecyclerConfig) {
                if (control instanceof Controls.RecyclerCtrl) {
                    ((Controls.RecyclerCtrl) control).setGridSpanCount(
                            ((Updates.RecyclerCtrlUpdateRecyclerConfig) update).getValue());
                    ((Controls.RecyclerCtrl) control).setRecyclerType(
                            ((Updates.RecyclerCtrlUpdateRecyclerConfig) update).getValue2());
                    ((Controls.RecyclerCtrl) control).setOrientation(
                            ((Updates.RecyclerCtrlUpdateRecyclerConfig) update).getValue3());
                    view = getOrigin(view);
                    Controls.RecyclerCtrl recyclerCtrl = (Controls.RecyclerCtrl) control;
                    RecyclerView recyclerView = (RecyclerView) view;
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
                    Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
                }
            } else if (update instanceof Updates.SeekBarCtrlUpdateProgress) {
                if (control instanceof Controls.SeekBarCtrl) {
                    ((Controls.SeekBarCtrl) control).setProgress(
                            ((Updates.SeekBarCtrlUpdateProgress) update).getValue());
                    view = getOrigin(view);
                    ((SeekBar) view).setProgress(
                            ((Updates.SeekBarCtrlUpdateProgress) update).getValue());
                }
            } else if (update instanceof Updates.SeekBarCtrlUpdateThumbColor) {
                if (control instanceof Controls.SeekBarCtrl) {
                    ((Controls.SeekBarCtrl) control).setThumbColor(
                            ((Updates.SeekBarCtrlUpdateThumbColor) update).getValue());
                    view = getOrigin(view);
                    ((SeekBar) view).getThumb().setColorFilter(Color.parseColor(
                            ((Updates.SeekBarCtrlUpdateThumbColor) update).getValue())
                            , PorterDuff.Mode.SRC_ATOP);
                }
            } else if (update instanceof Updates.SeekBarCtrlUpdateThumbSize) {
                if (control instanceof Controls.SeekBarCtrl) {
                    ((Controls.SeekBarCtrl) control).setThumbSize(
                            ((Updates.SeekBarCtrlUpdateThumbSize) update).getValue());
                    view = getOrigin(view);
                    ShapeDrawable th = new ShapeDrawable(new OvalShape());
                    th.setIntrinsicWidth(GraphicsHelper.dpToPx(
                            ((Controls.SeekBarCtrl)control).getThumbSize()));
                    th.setIntrinsicHeight(GraphicsHelper.dpToPx(
                            ((Controls.SeekBarCtrl)control).getThumbSize()));
                    if (!FieldValidator.isFieldEmpty(((Controls.SeekBarCtrl)control).getThumbColor()))
                        th.setColorFilter(Color.parseColor(((Controls.SeekBarCtrl)control)
                                .getThumbColor()), PorterDuff.Mode.SRC_OVER);
                    else
                        th.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_OVER);
                    ((SeekBar) view).setThumb(th);
                }
            } else if (update instanceof Updates.SeekBarCtrlUpdateTrackColor) {
                if (control instanceof Controls.SeekBarCtrl) {
                    ((Controls.SeekBarCtrl) control).setTrackColor(
                            ((Updates.SeekBarCtrlUpdateTrackColor) update).getValue());
                    view = getOrigin(view);
                    ((SeekBar) view).getProgressDrawable().setColorFilter(Color.parseColor(
                            ((Updates.SeekBarCtrlUpdateTrackColor) update).getValue())
                            , PorterDuff.Mode.MULTIPLY);
                    if (((SeekBar) view).getProgressDrawable() instanceof ShapeDrawable)
                        ((ShapeDrawable) ((SeekBar) view).getProgressDrawable())
                                .getPaint().setColor(Color.parseColor(
                                ((Updates.SeekBarCtrlUpdateTrackColor) update).getValue()));
                }
            } else if (update instanceof Updates.SeekBarCtrlUpdateTrackThickness) {
                if (control instanceof Controls.SeekBarCtrl) {
                    ((Controls.SeekBarCtrl) control).setTrackThickness(
                            ((Updates.SeekBarCtrlUpdateTrackThickness) update).getValue());
                    view = getOrigin(view);
                    ShapeDrawable shapeDrawable = new ShapeDrawable();
                    shapeDrawable.setShape(new RectShape());
                    shapeDrawable.setIntrinsicWidth(GraphicsHelper.dpToPx(
                            control.getWidth()));
                    shapeDrawable.setIntrinsicHeight(GraphicsHelper.dpToPx(
                            ((Controls.SeekBarCtrl) control).getTrackThickness()));
                    ((SeekBar) view).setProgressDrawable(shapeDrawable);
                    if (!FieldValidator.isFieldEmpty(((Controls.SeekBarCtrl) control).getTrackColor()))
                        shapeDrawable.getPaint().setColor(Color.parseColor(
                                ((Controls.SeekBarCtrl) control).getTrackColor()));
                    else
                        shapeDrawable.getPaint().setColor(Color.WHITE);
                }
            }
        }
    }

    public void updateUi(Hashtable<String, Pair<Controls.Control, View>> idTable, Updates.Update update) {
        Pair<Controls.Control, View> pair = idTable.get(update.getControlId());
        if (pair != null) {
            Locks.runSafeOnIdTable(() -> {
                Controls.Control control = pair.first;
                View view = pair.second;
                mainThreadRunner.runOnMainThread(() -> updateUiAsync(control, view, idTable, update));
            });
        }
    }

    public void updateBatchUi(Hashtable<String, Pair<Controls.Control, View>> idTable, List<Updates.Update> updates) {
        for (Updates.Update update : updates) {
            updateUi(idTable, update);
        }
    }
    
    private View getOrigin(View view) {
        return ((ViewGroup)((ViewGroup) view)
                .getChildAt(0)).getChildAt(0);
    }
}
