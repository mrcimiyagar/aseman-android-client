package kasper.android.pulseframework.engines;

import android.animation.ValueAnimator;
import android.os.Handler;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import java.util.Hashtable;
import java.util.List;

import kasper.android.pulseframework.interfaces.IAnimToUpdate;
import kasper.android.pulseframework.interfaces.IFetchVarValue;
import kasper.android.pulseframework.interfaces.IHandleValueAnimator;
import kasper.android.pulseframework.interfaces.IMainThreadRunner;
import kasper.android.pulseframework.locks.Locks;
import kasper.android.pulseframework.models.Anims;
import kasper.android.pulseframework.models.Codes;
import kasper.android.pulseframework.models.Controls;
import kasper.android.pulseframework.models.Updates;
import kasper.android.pulseframework.utils.GraphicsHelper;
import kasper.android.pulseframework.utils.JsonHelper;

public class UiAnimatorEngine {

    private IAnimToUpdate animToUpdate;
    private IMainThreadRunner mainThreadRunner;
    private IFetchVarValue fetchVarValue;
    private IHandleValueAnimator handleValueAnimator;

    public UiAnimatorEngine(IAnimToUpdate animToUpdate, IMainThreadRunner mainThreadRunner, IFetchVarValue fetchVarValue, IHandleValueAnimator handleValueAnimator) {
        this.animToUpdate = animToUpdate;
        this.mainThreadRunner = mainThreadRunner;
        this.fetchVarValue = fetchVarValue;
        this.handleValueAnimator = handleValueAnimator;
    }

    private void animateUiAsync(Controls.Control control, View view, Anims.Anim anim) {
        ValueAnimator valueAnimator = new ValueAnimator();
        Codes.Variable var = anim.getVariable();
        if (anim instanceof Anims.ControlAnimX) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofFloat(GraphicsHelper.pxToDp(view.getX())
                        , ((Anims.ControlAnimX) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(GraphicsHelper.pxToDp(view.getX())
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = Float.valueOf((float)valueAnimator1.getAnimatedValue()).intValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "x", value);
            });
        } else if (anim instanceof Anims.ControlAnimY) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofFloat(GraphicsHelper.pxToDp(view.getY())
                        , ((Anims.ControlAnimY) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(GraphicsHelper.pxToDp(view.getY())
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = Float.valueOf((float)valueAnimator1.getAnimatedValue()).intValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "y", value);
            });
        } else if (anim instanceof Anims.ControlAnimWidth) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofFloat(GraphicsHelper.pxToDp(view.getMeasuredWidth())
                        , ((Anims.ControlAnimWidth) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(GraphicsHelper.pxToDp(view.getMeasuredWidth())
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = Float.valueOf((float)valueAnimator1.getAnimatedValue()).intValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "width", value);
            });
        } else if (anim instanceof Anims.ControlAnimHeight) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(GraphicsHelper.pxToDp(view.getMeasuredHeight())
                        , ((Anims.ControlAnimHeight) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(GraphicsHelper.pxToDp(view.getMeasuredHeight())
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "height", value);
            });
        } else if (anim instanceof Anims.ControlAnimMarginLeft) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getMarginLeft()
                        , ((Anims.ControlAnimMarginLeft) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getMarginLeft()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "marginLeft", value);
            });
        } else if (anim instanceof Anims.ControlAnimMarginRight) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getMarginRight()
                        , ((Anims.ControlAnimMarginRight) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getMarginRight()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "marginRight", value);
            });
        } else if (anim instanceof Anims.ControlAnimMarginTop) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getMarginTop()
                        , ((Anims.ControlAnimMarginTop) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getMarginTop()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "marginTop", value);
            });
        } else if (anim instanceof Anims.ControlAnimMarginBottom) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getMarginBottom()
                        , ((Anims.ControlAnimMarginBottom) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getMarginBottom()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "marginBottom", value);
            });
        } else if (anim instanceof Anims.ControlAnimPaddingLeft) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getPaddingLeft()
                        , ((Anims.ControlAnimPaddingLeft) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getPaddingLeft()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "paddingLeft", value);
            });
        } else if (anim instanceof Anims.ControlAnimPaddingTop) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getPaddingTop()
                        , ((Anims.ControlAnimPaddingTop) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getPaddingTop()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "paddingTop", value);
            });
        } else if (anim instanceof Anims.ControlAnimPaddingRight) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getPaddingRight()
                        , ((Anims.ControlAnimPaddingRight) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getPaddingRight()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "paddingRight", value);
            });
        } else if (anim instanceof Anims.ControlAnimPaddingBottom) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getPaddingBottom()
                        , ((Anims.ControlAnimPaddingBottom) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofFloat(control.getPaddingBottom()
                        , (int)fetchVarValue.fetchVarValue(var.getName()));
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "paddingBottom", value);
            });
        } else if (anim instanceof Anims.ControlAnimRotationX) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getRotationX()
                        , ((Anims.ControlAnimRotationX) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofInt(control.getRotationX()
                        , Float.valueOf((float)fetchVarValue.fetchVarValue(var.getName())).intValue());
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "rotationX", value);
            });
        } else if (anim instanceof Anims.ControlAnimRotationY) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getRotationY()
                        , ((Anims.ControlAnimRotationY) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofInt(control.getRotationY()
                        , Float.valueOf((float)fetchVarValue.fetchVarValue(var.getName())).intValue());
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "rotationY", value);
            });
        } else if (anim instanceof Anims.ControlAnimRotation) {
            if (var == null) {
                valueAnimator = ValueAnimator.ofInt(control.getRotation()
                        , ((Anims.ControlAnimRotation) anim).getFinalValue());
            } else {
                valueAnimator = ValueAnimator.ofInt(control.getRotation()
                        , Float.valueOf((float)fetchVarValue.fetchVarValue(var.getName())).intValue());
            }
            valueAnimator.addUpdateListener(valueAnimator1 -> {
                int value = (int) valueAnimator1.getAnimatedValue();
                handleValueAnimator.handleValueAnimation(control.getId(), "rotation", value);
            });
        }
        valueAnimator.setDuration(anim.getDuration());
        valueAnimator.start();
    }

    public void animateUi(Hashtable<String, Pair<Controls.Control, View>> idTable, Anims.Anim anim) {
        Locks.runInQueue(() -> {
            Pair<Controls.Control, View> pair = idTable.get(anim.getControlId());
            if (pair != null) {
                Controls.Control control = pair.first;
                View view = pair.second;
                if (control != null && view != null)
                    mainThreadRunner.runOnMainThread(() -> animateUiAsync(control, view, anim));
            }
        });
    }

    public void animateBatchUi(Hashtable<String, Pair<Controls.Control, View>> idTable, List<Anims.Anim> anims) {
        for (Anims.Anim anim : anims)
            animateUi(idTable, anim);
    }
}
