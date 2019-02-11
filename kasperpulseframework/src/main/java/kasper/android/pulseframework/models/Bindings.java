package kasper.android.pulseframework.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Bindings {

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
}
