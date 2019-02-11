package kasper.android.pulseframework.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import kasper.android.pulseframework.interfaces.INotifyValueChange;

public class Codes {

    public static class Code {

    }

    public static class If extends Code {
        private Code condition;
        private List<Code> codes;

        public Code getCondition() {
            return condition;
        }

        public void setCondition(Code condition) {
            this.condition = condition;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class For extends Code {
        private Code condition;
        private Variable counter;
        private Variable step;
        private List<Code> codes;

        public Code getCondition() {
            return condition;
        }

        public void setCondition(Code condition) {
            this.condition = condition;
        }

        public Variable getCounter() {
            return counter;
        }

        public void setCounter(Variable counter) {
            this.counter = counter;
        }

        public Variable getStep() {
            return step;
        }

        public void setStep(Variable step) {
            this.step = step;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class While extends Code {
        private Code condition;
        private List<Code> codes;

        public Code getCondition() {
            return condition;
        }

        public void setCondition(Code condition) {
            this.condition = condition;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class Comparison extends Code {
        private Code item1;
        private Code item2;

        public Code getItem1() {
            return item1;
        }

        public void setItem1(Code item1) {
            this.item1 = item1;
        }

        public Code getItem2() {
            return item2;
        }

        public void setItem2(Code item2) {
            this.item2 = item2;
        }
    }

    public static class EQCompare extends Comparison {

    }

    public static class NECompare extends Comparison {

    }

    public static class GTCompare extends Comparison {

    }

    public static class LTCompare extends Comparison {

    }

    public static class GECompare extends Comparison {

    }

    public static class LECompare extends Comparison {

    }

    public static class Definition extends Code {

        private Variable variable;

        public Variable getVariable() {
            return variable;
        }

        public void setVariable(Variable variable) {
            this.variable = variable;
        }
    }

    public static class Assignment extends Code {
        private Variable variable;
        private Code value;

        public Variable getVariable() {
            return variable;
        }

        public void setVariable(Variable variable) {
            this.variable = variable;
        }

        public Code getValue() {
            return value;
        }

        public void setValue(Code value) {
            this.value = value;
        }
    }

    public static class MathExpSum extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpSubtract extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpMultiply extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpDivide extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpAnd extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class MathExpOr extends Code {

        private Code value1;
        private Code value2;

        public Code getValue1() {
            return value1;
        }

        public void setValue1(Code value1) {
            this.value1 = value1;
        }

        public Code getValue2() {
            return value2;
        }

        public void setValue2(Code value2) {
            this.value2 = value2;
        }
    }

    public static class Increment extends Code {

        private Variable var;

        public Variable getVar() {
            return var;
        }

        public void setVar(Variable var) {
            this.var = var;
        }
    }

    public static class Decrement extends Code {

        private Variable var;

        public Variable getVar() {
            return var;
        }

        public void setVar(Variable var) {
            this.var = var;
        }
    }

    public static class Parenthesis extends Code {
        private Code code;

        public Code getCode() {
            return code;
        }

        public void setCode(Code code) {
            this.code = code;
        }
    }

    public enum DataType {
        @JsonProperty("SHORT") SHORT,
        @JsonProperty("INT") INT,
        @JsonProperty("FLOAT") FLOAT,
        @JsonProperty("LONG") LONG,
        @JsonProperty("DOUBLE") DOUBLE,
        @JsonProperty("BOOL") BOOL,
        @JsonProperty("STRING") STRING
    }

    public static class Variable extends Code {

        private String name;
        private Value value;
        private Hashtable<String, INotifyValueChange> observers;

        public Variable() {
            this.observers = new Hashtable<>();
        }

        void notifyValueChanged(Value value) {
            for (Map.Entry<String, INotifyValueChange> entry : this.observers.entrySet()) {
                entry.getValue().notifyValueChanged(entry.getKey(), value);
            }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
            notifyValueChanged(value);
        }

        public Hashtable<String, INotifyValueChange> getObservers() {
            return observers;
        }

        public void setObservers(Hashtable<String, INotifyValueChange> observers) {
            this.observers = observers;
        }
    }

    public static class Value extends Code implements Comparable<Value> {

        private DataType valueType;
        private Object value;

        public DataType getValueType() {
            return valueType;
        }

        public void setValueType(DataType valueType) {
            this.valueType = valueType;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            if (this.valueType == DataType.SHORT
                    || this.valueType == DataType.INT
                    || this.valueType == DataType.LONG
                    || this.valueType == DataType.FLOAT
                    || this.valueType == DataType.DOUBLE) {

                double raw = Double.valueOf(value + "");
                try {
                    this.value = Short.parseShort(raw + "");
                    this.valueType = DataType.SHORT;
                } catch (Exception ignored) {
                    try {
                        this.value = Integer.parseInt(raw + "");
                        this.valueType = DataType.INT;
                    } catch (Exception ignored2) {
                        try {
                            this.value = Long.parseLong(raw + "");
                            this.valueType = DataType.LONG;
                        } catch (Exception ignored3) {
                            try {
                                this.value = Float.parseFloat(raw + "");
                                this.valueType = DataType.FLOAT;
                            } catch (Exception ignored4) {
                                try {
                                    this.value = Double.parseDouble(raw + "");
                                    this.valueType = DataType.DOUBLE;
                                } catch (Exception ignored5) { }
                            }
                        }
                    }
                }
            } else {
                this.value = value;
            }
        }

        @Override
        public int compareTo(@NonNull  Value v2) {
            if (this.valueType == DataType.SHORT) {
                if (v2.valueType == DataType.SHORT)
                    return (int) Math.signum((short) this.getValue() - (short) v2.getValue());
                else if (v2.valueType == DataType.INT)
                    return (int) Math.signum((short) this.getValue() - (int) v2.getValue());
                else if (v2.valueType == DataType.LONG)
                    return (int) Math.signum((short) this.getValue() - (long) v2.getValue());
                else if (v2.valueType == DataType.FLOAT)
                    return (int) Math.signum((short) this.getValue() - (float) v2.getValue());
                else if (v2.valueType == DataType.DOUBLE)
                    return (int) Math.signum((short) this.getValue() - (double) v2.getValue());
                else if (v2.valueType == DataType.BOOL)
                    return -1;
                else if (v2.valueType == DataType.STRING)
                    return -1;
            } else if (this.valueType == DataType.INT) {
                if (v2.valueType == DataType.SHORT)
                    return (int) Math.signum((int) this.getValue() - (short) v2.getValue());
                else if (v2.valueType == DataType.INT)
                    return (int) Math.signum((int) this.getValue() - (int) v2.getValue());
                else if (v2.valueType == DataType.LONG)
                    return (int) Math.signum((int) this.getValue() - (long) v2.getValue());
                else if (v2.valueType == DataType.FLOAT)
                    return (int) Math.signum((int) this.getValue() - (float) v2.getValue());
                else if (v2.valueType == DataType.DOUBLE)
                    return (int) Math.signum((int) this.getValue() - (double) v2.getValue());
                else if (v2.valueType == DataType.BOOL)
                    return -1;
                else if (v2.valueType == DataType.STRING)
                    return -1;
            } else if (this.valueType == DataType.LONG) {
                if (v2.valueType == DataType.SHORT)
                    return (int) Math.signum((long) this.getValue() - (short) v2.getValue());
                else if (v2.valueType == DataType.INT)
                    return (int) Math.signum((long) this.getValue() - (int) v2.getValue());
                else if (v2.valueType == DataType.LONG)
                    return (int) Math.signum((long) this.getValue() - (long) v2.getValue());
                else if (v2.valueType == DataType.FLOAT)
                    return (int) Math.signum((long) this.getValue() - (float) v2.getValue());
                else if (v2.valueType == DataType.DOUBLE)
                    return (int) Math.signum((long) this.getValue() - (double) v2.getValue());
                else if (v2.valueType == DataType.BOOL)
                    return -1;
                else if (v2.valueType == DataType.STRING)
                    return -1;
            } else if (this.valueType == DataType.FLOAT) {
                if (v2.valueType == DataType.SHORT)
                    return (int) Math.signum((float) this.getValue() - (short) v2.getValue());
                else if (v2.valueType == DataType.INT)
                    return (int) Math.signum((float) this.getValue() - (int) v2.getValue());
                else if (v2.valueType == DataType.LONG)
                    return (int) Math.signum((float) this.getValue() - (long) v2.getValue());
                else if (v2.valueType == DataType.FLOAT)
                    return (int) Math.signum((float) this.getValue() - (float) v2.getValue());
                else if (v2.valueType == DataType.DOUBLE)
                    return (int) Math.signum((float) this.getValue() - (double) v2.getValue());
                else if (v2.valueType == DataType.BOOL)
                    return -1;
                else if (v2.valueType == DataType.STRING)
                    return -1;
            } else if (this.valueType == DataType.DOUBLE) {
                if (v2.valueType == DataType.SHORT)
                    return (int) Math.signum((double) this.getValue() - (short) v2.getValue());
                else if (v2.valueType == DataType.INT)
                    return (int) Math.signum((double) this.getValue() - (int) v2.getValue());
                else if (v2.valueType == DataType.LONG)
                    return (int) Math.signum((double) this.getValue() - (long) v2.getValue());
                else if (v2.valueType == DataType.FLOAT)
                    return (int) Math.signum((double) this.getValue() - (float) v2.getValue());
                else if (v2.valueType == DataType.DOUBLE)
                    return (int) Math.signum((double) this.getValue() - (double) v2.getValue());
                else if (v2.valueType == DataType.BOOL)
                    return -1;
                else if (v2.valueType == DataType.STRING)
                    return -1;
            } else if (this.valueType == DataType.BOOL) {
                if (v2.valueType == DataType.SHORT)
                    return -1;
                else if (v2.valueType == DataType.INT)
                    return -1;
                else if (v2.valueType == DataType.LONG)
                    return -1;
                else if (v2.valueType == DataType.FLOAT)
                    return -1;
                else if (v2.valueType == DataType.DOUBLE)
                    return -1;
                else if (v2.valueType == DataType.BOOL) {
                    if ((boolean) this.value == (boolean) v2.getValue())
                        return 0;
                    else
                         return -1;
                } else if (v2.valueType == DataType.STRING) {
                    return -1;
                }
            } else if (this.valueType == DataType.STRING) {
                if (v2.valueType == DataType.SHORT)
                    return -1;
                else if (v2.valueType == DataType.INT)
                    return -1;
                else if (v2.valueType == DataType.LONG)
                    return -1;
                else if (v2.valueType == DataType.FLOAT)
                    return -1;
                else if (v2.valueType == DataType.DOUBLE)
                    return -1;
                else if (v2.valueType == DataType.BOOL) {
                    return -1;
                } else if (v2.valueType == DataType.STRING) {
                    if (this.value.equals(v2.getValue()))
                        return 0;
                    else
                        return -1;
                }
            }

            return -1;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value) {
                Value v2 = (Value) obj;
                return this.compareTo(v2) == 0;
            }
            return false;
        }

        public boolean greaterThan(Value v2) {
            return this.compareTo(v2) > 0;
        }

        public boolean greaterEqual(Value v2) {
            return this.compareTo(v2) >= 0;
        }

        public boolean lessThan(Value v2) {
            return this.compareTo(v2) < 0;
        }

        public boolean lessEqual(Value v2) {
            return this.compareTo(v2) <= 0;
        }
    }

    public static class Task extends Code {
        private String name;
        private long period;
        private List<Code> codes;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getPeriod() {
            return period;
        }

        public void setPeriod(long period) {
            this.period = period;
        }

        public List<Code> getCodes() {
            return codes;
        }

        public void setCodes(List<Code> codes) {
            this.codes = codes;
        }
    }

    public static class DefineTask extends Code {
        private Task task;

        public Task getTask() {
            return task;
        }

        public void setTask(Task task) {
            this.task = task;
        }
    }

    public static class StartTask extends Code {
        private String taskName;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
    }

    public static class StopTask extends Code {
        private String taskName;

        public String getTaskName() {
            return taskName;
        }

        public void setTaskName(String taskName) {
            this.taskName = taskName;
        }
    }
}
