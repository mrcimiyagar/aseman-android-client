package kasper.android.pulseframework.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

public class Bindings {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = MirrorToX.class, name = "MirrorToX"),
            @JsonSubTypes.Type(value = MirrorToY.class, name = "MirrorToY"),
            @JsonSubTypes.Type(value = MirrorToWidth.class, name = "MirrorToWidth"),
            @JsonSubTypes.Type(value = MirrorToHeight.class, name = "MirrorToHeight"),
            @JsonSubTypes.Type(value = MirrorToMarginLeft.class, name = "MirrorToMarginLeft"),
            @JsonSubTypes.Type(value = MirrorToMarginTop.class, name = "MirrorToMarginTop"),
            @JsonSubTypes.Type(value = MirrorToMarginRight.class, name = "MirrorToMarginRight"),
            @JsonSubTypes.Type(value = MirrorToMarginBottom.class, name = "MirrorToMarginBottom"),
            @JsonSubTypes.Type(value = MirrorToPaddingLeft.class, name = "MirrorToPaddingLeft"),
            @JsonSubTypes.Type(value = MirrorToPaddingTop.class, name = "MirrorToPaddingTop"),
            @JsonSubTypes.Type(value = MirrorToPaddingRight.class, name = "MirrorToPaddingRight"),
            @JsonSubTypes.Type(value = MirrorToPaddingBottom.class, name = "MirrorToPaddingBottom"),
            @JsonSubTypes.Type(value = MirrorToBorderColor.class, name = "MirrorToBorderColor"),
            @JsonSubTypes.Type(value = MirrorToBorderWidth.class, name = "MirrorToBorderWidth"),
            @JsonSubTypes.Type(value = MirrorToBackColor.class, name = "MirrorToBackColor"),
            @JsonSubTypes.Type(value = MirrorToRotationX.class, name = "MirrorToRotationX"),
            @JsonSubTypes.Type(value = MirrorToRotationY.class, name = "MirrorToRotationY"),
            @JsonSubTypes.Type(value = MirrorToRotation.class, name = "MirrorToRotation"),
            @JsonSubTypes.Type(value = MirrorToElevation.class, name = "MirrorToElevation"),
            @JsonSubTypes.Type(value = MirrorToTextColor.class, name = "MirrorToTextColor")
    })
    public static class Mirror {

        public enum ActionType {
            @JsonProperty("BIND") BIND,
            @JsonProperty("UNBIND") UNBIND
        }

        private String varName;
        private String ctrlName;
        private Mirror.ActionType action;

        public String getVarName() {
            return varName;
        }

        public void setVarName(String varName) {
            this.varName = varName;
        }

        public String getCtrlName() {
            return ctrlName;
        }

        public void setCtrlName(String ctrlName) {
            this.ctrlName = ctrlName;
        }

        public ActionType getAction() {
            return action;
        }

        public void setAction(Mirror.ActionType action) {
            this.action = action;
        }
    }

    public static class MirrorToX extends Mirror {

    }

    public static class MirrorToY extends Mirror {

    }

    public static class MirrorToWidth extends Mirror {

    }

    public static class MirrorToHeight extends Mirror {

    }

    public static class MirrorToMarginLeft extends Mirror {

    }

    public static class MirrorToMarginTop extends Mirror {

    }

    public static class MirrorToMarginRight extends Mirror {

    }

    public static class MirrorToMarginBottom extends Mirror {

    }

    public static class MirrorToPaddingLeft extends Mirror {

    }

    public static class MirrorToPaddingTop extends Mirror {

    }

    public static class MirrorToPaddingRight extends Mirror {

    }

    public static class MirrorToPaddingBottom extends Mirror {

    }

    public static class MirrorToBorderColor extends Mirror {

    }

    public static class MirrorToBorderWidth extends Mirror {

    }

    public static class MirrorToBackColor extends Mirror {

    }

    public static class MirrorToRotationX extends Mirror {

    }

    public static class MirrorToRotationY extends Mirror {

    }

    public static class MirrorToRotation extends Mirror {

    }

    public static class MirrorToElevation extends Mirror {

    }

    public static class MirrorToTextColor extends Mirror {

    }
}
