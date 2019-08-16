package kasper.android.pulseframework.engines;

import android.util.Log;

import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kasper.android.pulseframework.interfaces.ICodeToAnim;
import kasper.android.pulseframework.interfaces.IFetchVarValue;
import kasper.android.pulseframework.interfaces.IMirrorEffect;
import kasper.android.pulseframework.locks.Locks;
import kasper.android.pulseframework.models.Anims;
import kasper.android.pulseframework.models.Bindings;
import kasper.android.pulseframework.models.Codes;
import kasper.android.pulseframework.models.Exceptions;
import kasper.android.pulseframework.models.Tuple;
import kasper.android.pulseframework.utils.JsonHelper;

public class EREngine {

    public static class TimerHolder {
        private Timer timer;

        Timer getTimer() {
            return timer;
        }

        void setTimer(Timer timer) {
            this.timer = timer;
        }
    }

    private IMirrorEffect mirrorEffect;
    private ICodeToAnim codeToAnimation;
    private Hashtable<String, Codes.Variable> variableTable;

    public Hashtable<String, Codes.Variable> getVariableTable() {
        return variableTable;
    }

    private Hashtable<String, Tuple<Codes.Task, TimerHolder, Boolean>> taskTable;

    public EREngine(IMirrorEffect mirrorEffect, ICodeToAnim codeToAnimation) {
        this.mirrorEffect = mirrorEffect;
        this.codeToAnimation = codeToAnimation;
        this.variableTable = new Hashtable<>();
        this.taskTable = new Hashtable<>();
    }

    public void modifyMirror(Bindings.Mirror mirror) {
        try {
            Codes.Variable variable = variableTable.get(mirror.getVarName());
            if (variable != null) {
                if (mirror.getAction() == Bindings.Mirror.ActionType.BIND)
                    variable.getObservers().put(
                            mirror.getCtrlName(),
                            (ctrlName, value) -> mirrorEffect.mirrorEffect(mirror, value.getValue()));
                else if (mirror.getAction() == Bindings.Mirror.ActionType.UNBIND)
                    variable.getObservers().remove(mirror.getCtrlName());
            } else {
                throw new Exceptions.ELangException();
            }
        } catch (Exceptions.ELangException exception) {
            exception.printStackTrace();
        }
    }

    public void run(List<Codes.Code> codes) {
        Locks.runInQueue(() -> {
            try {
                runCommands(codes);
            } catch (Exceptions.ELangException ex) {
                ex.printStackTrace();
            }
        });
    }

    private Codes.Code runCommands(List<Codes.Code> codes) throws Exceptions.ELangException {
        for (Codes.Code code : codes) {
            if (code instanceof Codes.GetTime) {
                Codes.GetTime getTime = (Codes.GetTime) code;
                Calendar rightNow = Calendar.getInstance();
                if (getTime.getDetail() == Codes.GetTime.TimeDetail.SECOND) {
                    int value = rightNow.get(Calendar.SECOND);
                    Codes.Value result = new Codes.Value();
                    result.setValueType(Codes.DataType.INT);
                    result.setValue(value);
                    return result;
                } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.MINUTE) {
                    int value = rightNow.get(Calendar.MINUTE);
                    Codes.Value result = new Codes.Value();
                    result.setValueType(Codes.DataType.INT);
                    result.setValue(value);
                    return result;
                } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.HOUR12) {
                    int value = rightNow.get(Calendar.HOUR);
                    Codes.Value result = new Codes.Value();
                    result.setValueType(Codes.DataType.INT);
                    result.setValue(value);
                    return result;
                } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.HOUR24) {
                    int value = rightNow.get(Calendar.HOUR_OF_DAY);
                    Codes.Value result = new Codes.Value();
                    result.setValueType(Codes.DataType.INT);
                    result.setValue(value);
                    return result;
                } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.FULL12) {
                    int value1 = rightNow.get(Calendar.SECOND);
                    int value2 = rightNow.get(Calendar.MINUTE);
                    int value3 = rightNow.get(Calendar.HOUR);
                    Codes.Value result = new Codes.Value();
                    result.setValueType(Codes.DataType.STRING);
                    result.setValue(value3 + ":" + value2 + ":" + value1);
                    return result;
                } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.FULL24) {
                    int value1 = rightNow.get(Calendar.SECOND);
                    int value2 = rightNow.get(Calendar.MINUTE);
                    int value3 = rightNow.get(Calendar.HOUR_OF_DAY);
                    Codes.Value result = new Codes.Value();
                    result.setValueType(Codes.DataType.STRING);
                    result.setValue(value3 + ":" + value2 + ":" + value1);
                    return result;
                }
            } else if (code instanceof Codes.PerformAnim) {
                Codes.PerformAnim performAnim = (Codes.PerformAnim) code;
                Anims.Anim anim = performAnim.getAnim();
                this.codeToAnimation.performAnimation(anim);
            } else if (code instanceof Codes.DefineTask) {
                Codes.Task task = ((Codes.DefineTask) code).getTask();
                if (!taskTable.containsKey(task.getName())) {
                    TimerHolder timerHolder = new TimerHolder();
                    timerHolder.setTimer(new Timer());
                    taskTable.put(task.getName(), new Tuple<>(task, timerHolder, false));
                }
            } else if (code instanceof Codes.StartTask) {
                Tuple<Codes.Task, TimerHolder, Boolean> tuple = taskTable.get(((Codes.StartTask) code).getTaskName());
                if (tuple != null) {
                    if (!tuple.getItem3()) {
                        Codes.Task task = tuple.getItem1();
                        Timer timer = tuple.getItem2().getTimer();
                        taskTable.remove(task.getName());
                        taskTable.put(task.getName(), new Tuple<>(task, tuple.getItem2(), true));
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    runCommands(task.getCodes());
                                } catch (Exceptions.ELangException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }, 1, task.getPeriod());
                    }
                } else {
                    throw new Exceptions.ELangException();
                }
            } else if (code instanceof Codes.StopTask) {
                Tuple<Codes.Task, TimerHolder, Boolean> tuple = taskTable.get(((Codes.StopTask) code).getTaskName());
                if (tuple != null) {
                    taskTable.remove(((Codes.StopTask) code).getTaskName());
                    taskTable.put(((Codes.StopTask) code).getTaskName(), new Tuple<>(tuple.getItem1(), tuple.getItem2(), false));
                    Timer timer = tuple.getItem2().getTimer();
                    timer.cancel();
                    timer.purge();
                    tuple.getItem2().setTimer(new Timer());
                }
            } if (code instanceof Codes.If) {
                Codes.Value conditionValue = resolveObjectTree(((Codes.If) code).getCondition());
                if (conditionValue != null) {
                    if (conditionValue.getValueType() == Codes.DataType.BOOL) {
                        if ((boolean) conditionValue.getValue()) {
                            runCommands(((Codes.If) code).getIfCodes());
                        } else {
                            runCommands(((Codes.If) code).getElseCodes());
                        }
                    } else {
                        throw new Exceptions.ELangException();
                    }
                } else {
                    throw new Exceptions.ELangException();
                }
            } else if (code instanceof Codes.ModifyMirror) {
               modifyMirror(((Codes.ModifyMirror) code).getMirror());
            } else if (code instanceof Codes.For) {
                Codes.Variable counterVar = ((Codes.For) code).getCounter();
                variableTable.put(counterVar.getName(), counterVar);
                Codes.Value conditionValue = resolveObjectTree(code);
                if (conditionValue != null) {
                    if (conditionValue.getValueType() == Codes.DataType.BOOL) {
                        boolean condition = (boolean) conditionValue.getValue();
                        if (counterVar.getValue().getValueType() == Codes.DataType.SHORT) {
                            short step = (short) ((Codes.For) code).getStep().getValue().getValue();
                            for (/* no need for extra counter var*/; condition; counterVar.getValue()
                                    .setValue((short) counterVar.getValue().getValue() + step)) {
                                runCommands(((Codes.For) code).getCodes());
                                conditionValue = resolveObjectTree(((Codes.For) code).getCondition());
                                if (conditionValue != null) {
                                    if (conditionValue.getValueType() == Codes.DataType.BOOL)
                                        condition = (boolean) conditionValue.getValue();
                                    else {
                                        throw new Exceptions.ELangException();
                                    }
                                } else {
                                    throw new Exceptions.ELangException();
                                }
                            }
                        } else if (counterVar.getValue().getValueType() == Codes.DataType.INT) {
                            int step = (int) ((Codes.For) code).getStep().getValue().getValue();
                            for (/* no need for extra counter var*/; condition; counterVar.getValue()
                                    .setValue((int) counterVar.getValue().getValue() + step)) {
                                runCommands(((Codes.For) code).getCodes());
                                conditionValue = resolveObjectTree(((Codes.For) code).getCondition());
                                if (conditionValue != null) {
                                    if (conditionValue.getValueType() == Codes.DataType.BOOL)
                                        condition = (boolean) conditionValue.getValue();
                                    else {
                                        throw new Exceptions.ELangException();
                                    }
                                } else {
                                    throw new Exceptions.ELangException();
                                }
                            }
                        } else if (counterVar.getValue().getValueType() == Codes.DataType.FLOAT) {
                            float step = (float) ((Codes.For) code).getStep().getValue().getValue();
                            for (/* no need for extra counter var*/; condition; counterVar.getValue()
                                    .setValue((float) counterVar.getValue().getValue() + step)) {
                                runCommands(((Codes.For) code).getCodes());
                                conditionValue = resolveObjectTree(((Codes.For) code).getCondition());
                                if (conditionValue != null) {
                                    if (conditionValue.getValueType() == Codes.DataType.BOOL)
                                        condition = (boolean) conditionValue.getValue();
                                    else {
                                        throw new Exceptions.ELangException();
                                    }
                                } else {
                                    throw new Exceptions.ELangException();
                                }
                            }
                        } else if (counterVar.getValue().getValueType() == Codes.DataType.LONG) {
                            long step = (long) ((Codes.For) code).getStep().getValue().getValue();
                            for (/* no need for extra counter var*/; condition; counterVar.getValue()
                                    .setValue((long) counterVar.getValue().getValue() + step)) {
                                runCommands(((Codes.For) code).getCodes());
                                conditionValue = resolveObjectTree(((Codes.For) code).getCondition());
                                if (conditionValue != null) {
                                    if (conditionValue.getValueType() == Codes.DataType.BOOL)
                                        condition = (boolean) conditionValue.getValue();
                                    else {
                                        throw new Exceptions.ELangException();
                                    }
                                } else {
                                    throw new Exceptions.ELangException();
                                }
                            }
                        } else if (counterVar.getValue().getValueType() == Codes.DataType.DOUBLE) {
                            double step = (double) ((Codes.For) code).getStep().getValue().getValue();
                            for (/* no need for extra counter var*/; condition; counterVar.getValue()
                                    .setValue((double) counterVar.getValue().getValue() + step)) {
                                runCommands(((Codes.For) code).getCodes());
                                conditionValue = resolveObjectTree(((Codes.For) code).getCondition());
                                if (conditionValue != null) {
                                    if (conditionValue.getValueType() == Codes.DataType.BOOL)
                                        condition = (boolean) conditionValue.getValue();
                                    else {
                                        throw new Exceptions.ELangException();
                                    }
                                } else {
                                    throw new Exceptions.ELangException();
                                }
                            }
                        }
                    } else {
                        throw new Exceptions.ELangException();
                    }
                } else {
                    throw new Exceptions.ELangException();
                }
            } else if (code instanceof Codes.While) {
                Codes.Value conditionValue = resolveObjectTree(((Codes.While) code).getCondition());
                if (conditionValue != null) {
                    if (conditionValue.getValueType() == Codes.DataType.BOOL) {
                        boolean condition = (boolean) conditionValue.getValue();
                        while (condition) {
                            runCommands(((Codes.While) code).getCodes());
                            conditionValue = resolveObjectTree(((Codes.While) code).getCondition());
                            if (conditionValue != null) {
                                if (conditionValue.getValueType() == Codes.DataType.BOOL)
                                    condition = (boolean) conditionValue.getValue();
                                else {
                                    throw new Exceptions.ELangException();
                                }
                            } else {
                                throw new Exceptions.ELangException();
                            }
                        }
                    }
                } else {
                    throw new Exceptions.ELangException();
                }
            } else if (code instanceof Codes.Increment) {
                Codes.Variable variable = variableTable.get(
                        ((Codes.Increment) code).getVar().getName());
                if (variable != null) {
                    if (variable.getValue().getValueType() == Codes.DataType.SHORT)
                        variable.getValue().setValue((double) variable.getValue().getValue() + 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.INT)
                        variable.getValue().setValue((double) variable.getValue().getValue() + 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.FLOAT)
                        variable.getValue().setValue((double) variable.getValue().getValue() + 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.LONG)
                        variable.getValue().setValue((double) variable.getValue().getValue() + 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.DOUBLE)
                        variable.getValue().setValue((double) variable.getValue().getValue() + 1);
                } else {
                    throw new Exceptions.ELangException();
                }
            } else if (code instanceof Codes.Decrement) {
                Codes.Variable variable = variableTable.get(
                        ((Codes.Decrement) code).getVar().getName());
                if (variable != null) {
                    if (variable.getValue().getValueType() == Codes.DataType.SHORT)
                        variable.getValue().setValue((double) variable.getValue().getValue() - 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.INT)
                        variable.getValue().setValue((double) variable.getValue().getValue() - 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.FLOAT)
                        variable.getValue().setValue((double) variable.getValue().getValue() - 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.LONG)
                        variable.getValue().setValue((double) variable.getValue().getValue() - 1);
                    else if (variable.getValue().getValueType() == Codes.DataType.DOUBLE)
                        variable.getValue().setValue((double) variable.getValue().getValue() - 1);
                } else {
                    throw new Exceptions.ELangException();
                }
            } else if (code instanceof Codes.Definition) {
                Codes.Variable variable = ((Codes.Definition) code).getVariable();
                variableTable.put(variable.getName(), variable);
            } else if (code instanceof Codes.Assignment) {
                Codes.Variable variable = variableTable.get(
                        ((Codes.Assignment) code).getVariable().getName());
                if (variable != null) {
                    Codes.Value value = resolveObjectTree(((Codes.Assignment) code).getValue());
                    variable.setValue(value);
                } else {
                    throw new Exceptions.ELangException();
                }
            }
        }
        return new Codes.Code();
    }

    private Codes.Value resolveObjectTree(Codes.Code code) throws Exceptions.ELangException {
        if (code instanceof Codes.GetTime) {
            Codes.GetTime getTime = (Codes.GetTime) code;
            Calendar rightNow = Calendar.getInstance();
            if (getTime.getDetail() == Codes.GetTime.TimeDetail.SECOND) {
                int value = rightNow.get(Calendar.SECOND);
                Codes.Value result = new Codes.Value();
                result.setValueType(Codes.DataType.INT);
                result.setValue(value);
                return result;
            } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.MINUTE) {
                int value = rightNow.get(Calendar.MINUTE);
                Codes.Value result = new Codes.Value();
                result.setValueType(Codes.DataType.INT);
                result.setValue(value);
                return result;
            } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.HOUR12) {
                int value = rightNow.get(Calendar.HOUR);
                Codes.Value result = new Codes.Value();
                result.setValueType(Codes.DataType.INT);
                result.setValue(value);
                return result;
            } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.HOUR24) {
                int value = rightNow.get(Calendar.HOUR_OF_DAY);
                Codes.Value result = new Codes.Value();
                result.setValueType(Codes.DataType.INT);
                result.setValue(value);
                return result;
            } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.FULL12) {
                int value1 = rightNow.get(Calendar.SECOND);
                int value2 = rightNow.get(Calendar.MINUTE);
                int value3 = rightNow.get(Calendar.HOUR);
                Codes.Value result = new Codes.Value();
                result.setValueType(Codes.DataType.STRING);
                result.setValue(value3 + ":" + value2 + ":" + value1);
                return result;
            } else if (getTime.getDetail() == Codes.GetTime.TimeDetail.FULL24) {
                int value1 = rightNow.get(Calendar.SECOND);
                int value2 = rightNow.get(Calendar.MINUTE);
                int value3 = rightNow.get(Calendar.HOUR_OF_DAY);
                Codes.Value result = new Codes.Value();
                result.setValueType(Codes.DataType.STRING);
                result.setValue(value3 + ":" + value2 + ":" + value1);
                return result;
            }
        } else if (code instanceof Codes.EQCompare) {
            Codes.EQCompare compare = (Codes.EQCompare) code;
            Codes.Value value1 = resolveObjectTree(compare.getItem1());
            Codes.Value value2 = resolveObjectTree(compare.getItem2());
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.BOOL);
            if (value1 != null && value2 != null) {
                if (value1.getValueType() == value2.getValueType())
                    value.setValue(value1.equals(value2));
                else
                    throw new Exceptions.ELangException();
            } else {
                throw new Exceptions.ELangException();
            }
            return value;
        } else if (code instanceof Codes.NECompare) {
            Codes.NECompare compare = (Codes.NECompare) code;
            Codes.Value value1 = resolveObjectTree(compare.getItem1());
            Codes.Value value2 = resolveObjectTree(compare.getItem2());
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.BOOL);
            if (value1 != null && value2 != null) {
                if (value1.getValueType() == value2.getValueType())
                    value.setValue(!(value1.equals(value2)));
                else
                    throw new Exceptions.ELangException();
            } else {
                throw new Exceptions.ELangException();
            }
            return value;
        } else if (code instanceof Codes.GTCompare) {
            Codes.GTCompare compare = (Codes.GTCompare) code;
            Codes.Value value1 = resolveObjectTree(compare.getItem1());
            Codes.Value value2 = resolveObjectTree(compare.getItem2());
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.BOOL);
            if (value1 != null && value2 != null) {
                if (value1.getValueType() == value2.getValueType())
                    value.setValue(value1.greaterThan(value2));
                else
                    throw new Exceptions.ELangException();
            } else {
                throw new Exceptions.ELangException();
            }
            return value;
        } else if (code instanceof Codes.GECompare) {
            Codes.GECompare compare = (Codes.GECompare) code;
            Codes.Value value1 = resolveObjectTree(compare.getItem1());
            Codes.Value value2 = resolveObjectTree(compare.getItem2());
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.BOOL);
            if (value1 != null && value2 != null) {
                if (value1.getValueType() == value2.getValueType())
                    value.setValue(value1.greaterEqual(value2));
                else
                    throw new Exceptions.ELangException();
            } else {
                throw new Exceptions.ELangException();
            }
            return value;
        } else if (code instanceof Codes.LTCompare) {
            Codes.LTCompare compare = (Codes.LTCompare) code;
            Codes.Value value1 = resolveObjectTree(compare.getItem1());
            Codes.Value value2 = resolveObjectTree(compare.getItem2());
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.BOOL);
            if (value1 != null && value2 != null) {
                if (value1.getValueType() == value2.getValueType())
                    value.setValue(value1.lessThan(value2));
                else
                    throw new Exceptions.ELangException();
            } else {
                throw new Exceptions.ELangException();
            }
            return value;
        } else if (code instanceof Codes.LECompare) {
            Codes.LECompare compare = (Codes.LECompare) code;
            Codes.Value value1 = resolveObjectTree(compare.getItem1());
            Codes.Value value2 = resolveObjectTree(compare.getItem2());
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.BOOL);
            if (value1 != null && value2 != null) {
                if (value1.getValueType() == value2.getValueType())
                    value.setValue(value1.lessEqual(value2));
                else
                    throw new Exceptions.ELangException();
            } else {
                throw new Exceptions.ELangException();
            }
            return value;
        } else if (code instanceof Codes.MathExpSum) {
            Codes.Value v1 = resolveObjectTree(((Codes.MathExpSum) code).getValue1());
            Codes.Value v2 = resolveObjectTree(((Codes.MathExpSum) code).getValue2());
            if (v1 != null) {
                return resolveMathExpression(v1, v2, code);
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.MathExpSubtract) {
            Codes.Value v1 = resolveObjectTree(((Codes.MathExpSubtract) code).getValue1());
            Codes.Value v2 = resolveObjectTree(((Codes.MathExpSubtract) code).getValue2());
            if (v1 != null && v2 != null) {
                return resolveMathExpression(v1, v2, code);
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.MathExpMultiply) {
            Codes.Value v1 = resolveObjectTree(((Codes.MathExpMultiply) code).getValue1());
            Codes.Value v2 = resolveObjectTree(((Codes.MathExpMultiply) code).getValue2());
            if (v1 != null && v2 != null) {
                return resolveMathExpression(v1, v2, code);
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.MathExpDivide) {
            Codes.Value v1 = resolveObjectTree(((Codes.MathExpDivide) code).getValue1());
            Codes.Value v2 = resolveObjectTree(((Codes.MathExpDivide) code).getValue2());
            if (v1 != null && v2 != null) {
                return resolveMathExpression(v1, v2, code);
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.MathExpAnd) {
            Codes.Value v1 = resolveObjectTree(((Codes.MathExpAnd) code).getValue1());
            Codes.Value v2 = resolveObjectTree(((Codes.MathExpAnd) code).getValue2());
            if (v1 != null && v2 != null) {
                return resolveMathExpression(v1, v2, code);
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.MathExpOr) {
            Codes.Value v1 = resolveObjectTree(((Codes.MathExpOr) code).getValue1());
            Codes.Value v2 = resolveObjectTree(((Codes.MathExpOr) code).getValue2());
            if (v1 != null && v2 != null) {
                return resolveMathExpression(v1, v2, code);
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.MathExpNot) {
            Codes.Value v = resolveObjectTree(((Codes.MathExpNot) code).getCode());
            if (v != null) {
                if (v.getValueType() == Codes.DataType.BOOL) {
                    v.setValue(!((boolean) v.getValue()));
                    return v;
                } else {
                    throw new Exceptions.ELangException();
                }
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.Variable) {
            Codes.Variable variable = (Codes.Variable) code;
            Codes.Variable target = variableTable.get(variable.getName());
            if (target != null) {
                return target.getValue();
            } else {
                throw new Exceptions.ELangException();
            }
        } else if (code instanceof Codes.Value) {
            return (Codes.Value) code;
        }
        return null;
    }

    private Codes.Value resolveMathExpression(Codes.Value value1Raw, Codes.Value value2Raw, Object opObj) throws Exceptions.ELangException {
        if (value1Raw.getValue() instanceof String || value2Raw.getValue() instanceof String) {
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.STRING);
            value.setValue(value1Raw.getValue().toString() + value2Raw.getValue().toString());
            return value;
        } else if (value1Raw.getValueType() == Codes.DataType.BOOL &&
                value2Raw.getValueType() == Codes.DataType.BOOL) {
            Codes.Value value = new Codes.Value();
            value.setValueType(Codes.DataType.BOOL);
            if (opObj instanceof Codes.MathExpAnd)
                value.setValue(((boolean) value1Raw.getValue()) && ((boolean) value2Raw.getValue()));
            else if (opObj instanceof Codes.MathExpOr)
                value.setValue(((boolean) value1Raw.getValue()) || ((boolean) value2Raw.getValue()));
            else
                throw new Exceptions.ELangException();
            return value;
        } else if (value1Raw.getValueType() == Codes.DataType.SHORT) {
            short value1 = convertDoubleFormShortToShort(value1Raw.getValue());
            if (value2Raw.getValueType() == Codes.DataType.SHORT) {
                short value2 = convertDoubleFormShortToShort(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.SHORT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue(value1 & value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue(value1 | value2);
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.DOUBLE) {
                double value2 = Double.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + value2);
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - value2);
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * value2);
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / value2);
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value1Raw.getValueType() == Codes.DataType.LONG) {
                long value2 = convertDoubleFormLongToLong(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.LONG);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue((long)value1 & value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue((long)value1 | value2);
                return value;
            } else if (value1Raw.getValueType() == Codes.DataType.INT) {
                int value2 = convertDoubleFormIntToInt(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.INT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue((int)value1 & value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue((int)value1 | value2);
                return value;
            } else if (value1Raw.getValueType() == Codes.DataType.FLOAT) {
                float value2 = Float.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.FLOAT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue((double) value1 + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue((double) value1 - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue((double) value1 * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue((double) value1 / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            }
        } else if (value1Raw.getValueType() == Codes.DataType.DOUBLE) {
            double value1 = Double.valueOf(value1Raw.getValue() + "");
            if (value2Raw.getValueType() == Codes.DataType.SHORT) {
                short value2 = convertDoubleFormShortToShort(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(value1 + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(value1 - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(value1 * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(value1 / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.DOUBLE) {
                double value2 = Double.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(value1 + value2);
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(value1 - value2);
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(value1 * value2);
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(value1 / value2);
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value1Raw.getValueType() == Codes.DataType.LONG) {
                long value2 = convertDoubleFormLongToLong(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(value1 + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(value1 - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(value1 * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(value1 / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value1Raw.getValueType() == Codes.DataType.INT) {
                int value2 = convertDoubleFormIntToInt(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(value1 + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(value1 - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(value1 * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(value1 / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value1Raw.getValueType() == Codes.DataType.FLOAT) {
                float value2 = Float.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(value1 + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(value1 - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(value1 * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(value1 / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            }
        } else if (value1Raw.getValueType() == Codes.DataType.LONG) {
            long value1 = convertDoubleFormLongToLong(value1Raw.getValue());
            if (value2Raw.getValueType() == Codes.DataType.SHORT) {
                short value2 = convertDoubleFormShortToShort(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.LONG);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue(value1 & (long)value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue(value1 | (long)value2);
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.DOUBLE) {
                double value2 = Double.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue((double) value1 + value2);
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue((double) value1 - value2);
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue((double) value1 * value2);
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue((double) value1 / value2);
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.LONG) {
                long value2 = convertDoubleFormLongToLong(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.LONG);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue(value1 & value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue(value1 | value2);
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.INT) {
                int value2 = convertDoubleFormIntToInt(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.LONG);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue(value1 & (long)value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue(value1 | (long)value2);
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.FLOAT) {
                float value2 = Float.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            }
        } else if (value1Raw.getValueType() == Codes.DataType.INT) {
            int value1 = convertDoubleFormIntToInt(value1Raw.getValue());
            if (value2Raw.getValueType() == Codes.DataType.SHORT) {
                short value2 = convertDoubleFormShortToShort(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.INT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue(value1 & (int)value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue(value1 | (int)value2);
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.DOUBLE) {
                double value2 = Double.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + value2);
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - value2);
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * value2);
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / value2);
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.LONG) {
                long value2 = convertDoubleFormLongToLong(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.LONG);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue((long)value1 & value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue((long)value1 | value2);
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.INT) {
                int value2 = convertDoubleFormIntToInt(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.INT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd)
                    value.setValue(value1 & value2);
                else if (opObj instanceof Codes.MathExpOr)
                    value.setValue(value1 | value2);
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.FLOAT) {
                float value2 = Float.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.FLOAT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            }
        } else if (value1Raw.getValueType() == Codes.DataType.FLOAT) {
            float value1 = Float.valueOf(value1Raw.getValue() + "");
            if (value2Raw.getValueType() == Codes.DataType.SHORT) {
                short value2 = convertDoubleFormShortToShort(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.FLOAT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.DOUBLE) {
                double value2 = Double.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + value2);
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - value2);
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * value2);
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / value2);
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.LONG) {
                long value2 = convertDoubleFormLongToLong(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.DOUBLE);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.INT) {
                int value2 = convertDoubleFormIntToInt(value2Raw.getValue());
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.FLOAT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            } else if (value2Raw.getValueType() == Codes.DataType.FLOAT) {
                float value2 = Float.valueOf(value2Raw.getValue() + "");
                Codes.Value value = new Codes.Value();
                value.setValueType(Codes.DataType.FLOAT);
                if (opObj instanceof Codes.MathExpSum)
                    value.setValue(Double.valueOf(value1 + "") + Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpSubtract)
                    value.setValue(Double.valueOf(value1 + "") - Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpMultiply)
                    value.setValue(Double.valueOf(value1 + "") * Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpDivide)
                    value.setValue(Double.valueOf(value1 + "") / Double.valueOf(value2 + ""));
                else if (opObj instanceof Codes.MathExpAnd) {
                    throw new Exceptions.ELangException();
                } else if (opObj instanceof Codes.MathExpOr) {
                    throw new Exceptions.ELangException();
                }
                return value;
            }
        } else {
            throw new Exceptions.ELangException();
        }
        return null;
    }

    private int convertDoubleFormIntToInt(Object value) {
        String valueStr = value + "";
        if (valueStr.endsWith(".0"))
            valueStr = valueStr.substring(0, valueStr.length() - 2);
        return Integer.valueOf(valueStr);
    }

    private long convertDoubleFormLongToLong(Object value) {
        String valueStr = value + "";
        if (valueStr.endsWith(".0"))
            valueStr = valueStr.substring(0, valueStr.length() - 2);
        return Long.valueOf(valueStr);
    }

    private short convertDoubleFormShortToShort(Object value) {
        String valueStr = value + "";
        if (valueStr.endsWith(".0"))
            valueStr = valueStr.substring(0, valueStr.length() - 2);
        return Short.valueOf(valueStr);
    }
}
