package kasper.android.pulseframework.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class Anims {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ControlAnimX.class, name = "ControlAnimX"),
            @JsonSubTypes.Type(value = ControlAnimY.class, name = "ControlAnimY"),
            @JsonSubTypes.Type(value = ControlAnimWidth.class, name = "ControlAnimWidth"),
            @JsonSubTypes.Type(value = ControlAnimHeight.class, name = "ControlAnimHeight"),
            @JsonSubTypes.Type(value = ControlAnimMarginLeft.class, name = "ControlAnimMarginLeft"),
            @JsonSubTypes.Type(value = ControlAnimMarginTop.class, name = "ControlAnimMarginTop"),
            @JsonSubTypes.Type(value = ControlAnimMarginRight.class, name = "ControlAnimMarginRight"),
            @JsonSubTypes.Type(value = ControlAnimMarginBottom.class, name = "ControlAnimMarginBottom"),
            @JsonSubTypes.Type(value = ControlAnimPaddingLeft.class, name = "ControlAnimPaddingLeft"),
            @JsonSubTypes.Type(value = ControlAnimPaddingTop.class, name = "ControlAnimPaddingTop"),
            @JsonSubTypes.Type(value = ControlAnimPaddingRight.class, name = "ControlAnimPaddingRight"),
            @JsonSubTypes.Type(value = ControlAnimPaddingBottom.class, name = "ControlAnimPaddingBottom"),
            @JsonSubTypes.Type(value = ControlAnimRotationX.class, name = "ControlAnimRotationX"),
            @JsonSubTypes.Type(value = ControlAnimRotationY.class, name = "ControlAnimRotationY"),
            @JsonSubTypes.Type(value = ControlAnimRotation.class, name = "ControlAnimRotation"),
    })
    public static class Anim {
        private String controlId;
        private long duration;
        private Codes.Variable variable;

        public String getControlId() {
            return controlId;
        }

        public void setControlId(String controlId) {
            this.controlId = controlId;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public void setVariable(Codes.Variable var) {
            this.variable = var;
        }

        public Codes.Variable getVariable() {
            return variable;
        }
    }

    public static class IntAnim extends Anim {
        private int finalValue;

        public int getFinalValue() {
            return finalValue;
        }

        public void setFinalValue(int finalValue) {
            this.finalValue = finalValue;
        }
    }

    public static class ControlAnimX extends IntAnim {

    }

    public static class ControlAnimY extends IntAnim {

    }

    public static class ControlAnimWidth extends IntAnim {

    }

    public static class ControlAnimHeight extends IntAnim {

    }

    public static class ControlAnimMarginLeft extends IntAnim {

    }

    public static class ControlAnimMarginRight extends IntAnim {

    }

    public static class ControlAnimMarginTop extends IntAnim {

    }

    public static class ControlAnimMarginBottom extends IntAnim {

    }

    public static class ControlAnimPaddingLeft extends IntAnim {

    }

    public static class ControlAnimPaddingTop extends IntAnim {

    }

    public static class ControlAnimPaddingBottom extends IntAnim {

    }

    public static class ControlAnimPaddingRight extends IntAnim {

    }

    public static class ControlAnimRotation extends IntAnim {

    }

    public static class ControlAnimRotationX extends IntAnim {

    }

    public static class ControlAnimRotationY extends IntAnim {

    }
}
