package kasper.android.pulseframework.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

public class Updates {

    @JsonTypeInfo(
            use = JsonTypeInfo.Id.NAME,
            include = JsonTypeInfo.As.PROPERTY,
            property = "type")
    @JsonSubTypes({
            @JsonSubTypes.Type(value = ControlUpdateWidth.class, name = "ControlUpdateWidth"),
            @JsonSubTypes.Type(value = ControlUpdateHeight.class, name = "ControlUpdateHeight"),
            @JsonSubTypes.Type(value = ControlUpdateX.class, name = "ControlUpdateX"),
            @JsonSubTypes.Type(value = ControlUpdateY.class, name = "ControlUpdateY"),
            @JsonSubTypes.Type(value = ControlUpdateBackColor.class, name = "ControlUpdateBackColor"),
            @JsonSubTypes.Type(value = ControlUpdateBorderColor.class, name = "ControlUpdateBorderColor"),
            @JsonSubTypes.Type(value = ControlUpdateCornerRadius.class, name = "ControlUpdateCornerRadius"),
            @JsonSubTypes.Type(value = ControlUpdateBorderWidth.class, name = "ControlUpdateBorderWidth"),
            @JsonSubTypes.Type(value = ControlUpdateElevation.class, name = "ControlUpdateElevation"),
            @JsonSubTypes.Type(value = ControlUpdatePaddingTop.class, name = "ControlUpdatePaddingTop"),
            @JsonSubTypes.Type(value = ControlUpdatePaddingLeft.class, name = "ControlUpdatePaddingLeft"),
            @JsonSubTypes.Type(value = ControlUpdatePaddingBottom.class, name = "ControlUpdatePaddingBottom"),
            @JsonSubTypes.Type(value = ControlUpdatePaddingRight.class, name = "ControlUpdatePaddingRight"),
            @JsonSubTypes.Type(value = ControlUpdateRotation.class, name = "ControlUpdatePaddingRight"),
            @JsonSubTypes.Type(value = ControlUpdateRotationX.class, name = "ControlUpdateRotationX"),
            @JsonSubTypes.Type(value = ControlUpdateRotationY.class, name = "ControlUpdateRotationY"),
            @JsonSubTypes.Type(value = ControlUpdateMarginBottom.class, name = "ControlUpdateMarginBottom"),
            @JsonSubTypes.Type(value = ControlUpdateMarginLeft.class, name = "ControlUpdateMarginLeft"),
            @JsonSubTypes.Type(value = ControlUpdateMarginTop.class, name = "ControlUpdateMarginTop"),
            @JsonSubTypes.Type(value = ControlUpdateMarginRight.class, name = "ControlUpdateMarginRight"),
            @JsonSubTypes.Type(value = PanelCtrlAddControl.class, name = "PanelCtrlAddControl"),
            @JsonSubTypes.Type(value = TextCtrlUpdateText.class, name = "TextCtrlUpdateText"),
            @JsonSubTypes.Type(value = TextCtrlUpdateTextColor.class, name = "TextCtrlUpdateTextColor"),
            @JsonSubTypes.Type(value = TextCtrlUpdateTextSize.class, name = "TextCtrlUpdateTextSize"),
            @JsonSubTypes.Type(value = TextCtrlUpdateGravityType.class, name = "TextCtrlUpdateGravityType"),
            @JsonSubTypes.Type(value = ImageCtrlUpdateImageUrl.class, name = "ImageCtrlUpdateImageUrl"),
            @JsonSubTypes.Type(value = ImageCtrlUpdateScaleType.class, name = "ImageCtrlUpdateScaleType"),
            @JsonSubTypes.Type(value = InputFieldCtrlUpdateText.class, name = "InputFieldCtrlUpdateText"),
            @JsonSubTypes.Type(value = InputFieldCtrlUpdateTextSize.class, name = "InputFieldCtrlUpdateTextSize"),
            @JsonSubTypes.Type(value = InputFieldCtrlUpdateTextColor.class, name = "InputFieldCtrlUpdateTextColor"),
            @JsonSubTypes.Type(value = InputFieldCtrlUpdateHint.class, name = "InputFieldCtrlUpdateHint"),
            @JsonSubTypes.Type(value = InputFieldCtrlUpdateHintColor.class, name = "InputFieldCtrlUpdateHintColor"),
            @JsonSubTypes.Type(value = InputFieldCtrlUpdateLineColor.class, name = "InputFieldCtrlUpdateLineColor"),
            @JsonSubTypes.Type(value = ButtonCtrlUpdateCaption.class, name = "ControlUpdateWidth"),
            @JsonSubTypes.Type(value = ButtonCtrlUpdateCaptionSize.class, name = "ButtonCtrlUpdateCaptionSize"),
            @JsonSubTypes.Type(value = ButtonCtrlUpdateCaptionColor.class, name = "ButtonCtrlUpdateCaptionColor"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateTrackWidth.class, name = "ProgressCtrlUpdateTrackWidth"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateTrackEnabled.class, name = "ProgressCtrlUpdateTrackEnabled"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateTrackColor.class, name = "ProgressCtrlUpdateTrackColor"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateScaleZoneWidth.class, name = "ProgressCtrlUpdateScaleZoneWidth"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateScaleZonePadding.class, name = "ProgressCtrlUpdateScaleZonePadding"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateScaleZoneLength.class, name = "ProgressCtrlUpdateScaleZoneLength"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateScaleZoneCornerRadius.class, name = "ProgressCtrlUpdateScaleZoneCornerRadius"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateProgressTextVisibility.class, name = "ProgressCtrlUpdateProgressTextVisibility"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateProgressTextSize.class, name = "ProgressCtrlUpdateProgressTextSize"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateProgressTextPaddingBottom.class, name = "ProgressCtrlUpdateProgressTextPaddingBottom"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateProgressTextMoved.class, name = "ProgressCtrlUpdateProgressTextMoved"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateProgressTextColor.class, name = "ProgressCtrlUpdateProgressTextColor"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateProgressCornerRadius.class, name = "ProgressCtrlUpdateProgressCornerRadius"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateGraduatedEnabled.class, name = "ProgressCtrlUpdateGraduatedEnabled"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateFillEnabled.class, name = "ProgressCtrlUpdateFillEnabled"),
            @JsonSubTypes.Type(value = ProgressCtrlUpdateCircleBroken.class, name = "ProgressCtrlUpdateCircleBroken"),
            @JsonSubTypes.Type(value = VideoPlayerCtrlUpdateVideoUrl.class, name = "VideoPlayerCtrlUpdateVideoUrl"),
            @JsonSubTypes.Type(value = VideoPlayerCtrlUpdateProgress.class, name = "VideoPlayerCtrlUpdateProgress"),
            @JsonSubTypes.Type(value = VideoPlayerCtrlUpdatePlaying.class, name = "VideoPlayerCtrlUpdatePlaying"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateLineColor.class, name = "LineChartCtrlUpdateLineColor"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdatePoints.class, name = "LineChartCtrlUpdatePoints"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateLineThickness.class, name = "LineChartCtrlUpdateLineThickness"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateLineSmooth.class, name = "LineChartCtrlUpdateLineSmooth"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateLineEndAt.class, name = "LineChartCtrlUpdateLineEndAt"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateLineDashedIntervals.class, name = "LineChartCtrlUpdateLineDashedIntervals"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateLineBeginAt.class, name = "LineChartCtrlUpdateLineBeginAt"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateLabelsColor.class, name = "LineChartCtrlUpdateLabelsColor"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateFillColor.class, name = "LineChartCtrlUpdateFillColor"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateDotsStrokeThickness.class, name = "LineChartCtrlUpdateDotsStrokeThickness"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateDotsRadius.class, name = "LineChartCtrlUpdateDotsRadius"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateDotsColor.class, name = "LineChartCtrlUpdateDotsColor"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateAxisColor.class, name = "LineChartCtrlUpdateAxisColor"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateGradients.class, name = "LineChartCtrlUpdateGradients"),
            @JsonSubTypes.Type(value = LineChartCtrlUpdateDotsStrokeColor.class, name = "LineChartCtrlUpdateDotsStrokeColor"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdatePoints.class, name = "BarChartCtrlUpdatePoints"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdateAxisColor.class, name = "BarChartCtrlUpdateAxisColor"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdateSetSpacing.class, name = "BarChartCtrlUpdateSetSpacing"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdateRoundCorners.class, name = "BarChartCtrlUpdateRoundCorners"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdateLabelsColor.class, name = "BarChartCtrlUpdateLabelsColor"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdateBarSpacing.class, name = "BarChartCtrlUpdateBarSpacing"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdateBarsColor.class, name = "BarChartCtrlUpdateBarsColor"),
            @JsonSubTypes.Type(value = BarChartCtrlUpdateBarBackgroundColor.class, name = "BarChartCtrlUpdateBarBackgroundColor"),
            @JsonSubTypes.Type(value = ScrollerCtrlUpdatePosition.class, name = "ScrollerCtrlUpdatePosition"),
            @JsonSubTypes.Type(value = CompoundCtrlUpdateCaption.class, name = "CompoundCtrlUpdateCaption"),
            @JsonSubTypes.Type(value = CompoundCtrlUpdateCaptionSize.class, name = "CompoundCtrlUpdateCaptionSize"),
            @JsonSubTypes.Type(value = CompoundCtrlUpdateCaptionColor.class, name = "CompoundCtrlUpdateCaptionColor"),
            @JsonSubTypes.Type(value = CompoundCtrlUpdateTintColor.class, name = "CompoundCtrlUpdateTintColor"),
            @JsonSubTypes.Type(value = CompoundCtrlUpdateChecked.class, name = "CompoundCtrlUpdateChecked"),
            @JsonSubTypes.Type(value = DropDownCtrlAddItem.class, name = "DropDownCtrlAddItem"),
            @JsonSubTypes.Type(value = DropDownCtrlUpdateSelectedPos.class, name = "DropDownCtrlUpdateSelectedPos"),
            @JsonSubTypes.Type(value = RecyclerCtrlUpdateItem.class, name = "RecyclerCtrlUpdateItem"),
            @JsonSubTypes.Type(value = RecyclerCtrlRemoveItem.class, name = "RecyclerCtrlRemoveItem"),
            @JsonSubTypes.Type(value = RecyclerCtrlUpdateItem.class, name = "RecyclerCtrlUpdateItem"),
            @JsonSubTypes.Type(value = RecyclerCtrlUpdateRecyclerConfig.class, name = "RecyclerCtrlUpdateRecyclerConfig")
    })
    public static class Update {
        private String controlId;

        public String getControlId() {
            return controlId;
        }

        public void setControlId(String controlId) {
            this.controlId = controlId;
        }
    }

    public static class IntUpdate extends Update {
        private int value;

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    public static class StringUpdate extends Update {
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    public static class FloatUpdate extends Update {
        private float value;

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    public static class BoolUpdate extends Update {
        private boolean value;

        public boolean isValue() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    public static class ControlUpdateWidth extends IntUpdate {

    }

    public static class ControlUpdateHeight extends IntUpdate {

    }

    public static class ControlUpdateX extends IntUpdate {

    }

    public static class ControlUpdateY extends IntUpdate {

    }

    public static class ControlUpdateBackColor extends StringUpdate {

    }

    public static class ControlUpdateBorderColor extends StringUpdate {

    }

    public static class ControlUpdateCornerRadius extends IntUpdate {

    }

    public static class ControlUpdateBorderWidth extends IntUpdate {

    }

    public static class ControlUpdateRotationX extends IntUpdate {

    }

    public static class ControlUpdateRotationY extends IntUpdate {

    }

    public static class ControlUpdateRotation extends IntUpdate {

    }

    public static class ControlUpdateMarginLeft extends IntUpdate {

    }

    public static class ControlUpdateMarginTop extends IntUpdate {

    }

    public static class ControlUpdateMarginRight extends IntUpdate {

    }

    public static class ControlUpdateMarginBottom extends IntUpdate {

    }

    public static class ControlUpdatePaddingLeft extends IntUpdate {

    }

    public static class ControlUpdatePaddingTop extends IntUpdate {

    }

    public static class ControlUpdatePaddingRight extends IntUpdate {

    }

    public static class ControlUpdatePaddingBottom extends IntUpdate {

    }

    public static class ControlUpdateElevation extends IntUpdate {

    }

    public static class PanelCtrlAddControl extends Update {
        private Controls.Control value;

        public Controls.Control getValue() {
            return value;
        }

        public void setValue(Controls.Control value) {
            this.value = value;
        }
    }

    public static class TextCtrlUpdateText extends StringUpdate {

    }

    public static class TextCtrlUpdateTextSize extends IntUpdate {

    }

    public static class TextCtrlUpdateTextColor extends StringUpdate {

    }

    public static class TextCtrlUpdateGravityType extends Update {
        private Controls.TextCtrl.GravityType value;

        public Controls.TextCtrl.GravityType getValue() {
            return value;
        }

        public void setValue(Controls.TextCtrl.GravityType value) {
            this.value = value;
        }
    }

    public static class ImageCtrlUpdateImageUrl extends StringUpdate {

    }

    public static class ImageCtrlUpdateScaleType extends Update {
        private Controls.ImageCtrl.ImageScaleType value;

        public Controls.ImageCtrl.ImageScaleType getValue() {
            return value;
        }

        public void setValue(Controls.ImageCtrl.ImageScaleType value) {
            this.value = value;
        }
    }

    public static class InputFieldCtrlUpdateHint extends StringUpdate {

    }

    public static class InputFieldCtrlUpdateHintColor extends StringUpdate {

    }

    public static class InputFieldCtrlUpdateText extends StringUpdate {

    }

    public static class InputFieldCtrlUpdateTextColor extends StringUpdate {

    }

    public static class InputFieldCtrlUpdateTextSize extends IntUpdate {

    }

    public static class InputFieldCtrlUpdateLineColor extends StringUpdate {

    }

    public static class ButtonCtrlUpdateCaption extends StringUpdate {

    }

    public static class ButtonCtrlUpdateCaptionSize extends IntUpdate {

    }

    public static class ButtonCtrlUpdateCaptionColor extends StringUpdate {

    }

    public static class ProgressCtrlUpdateCircleBroken extends BoolUpdate {

    }

    public static class ProgressCtrlUpdateTrackWidth extends IntUpdate {

    }

    public static class ProgressCtrlUpdateFillEnabled extends BoolUpdate {

    }

    public static class ProgressCtrlUpdateProgressTextSize extends IntUpdate {

    }

    public static class ProgressCtrlUpdateProgressTextColor extends StringUpdate {

    }

    public static class ProgressCtrlUpdateTrackEnabled extends BoolUpdate {

    }

    public static class ProgressCtrlUpdateTrackColor extends StringUpdate {

    }

    public static class ProgressCtrlUpdateProgressTextVisibility extends BoolUpdate {

    }

    public static class ProgressCtrlUpdateGraduatedEnabled extends BoolUpdate {

    }

    public static class ProgressCtrlUpdateScaleZoneWidth extends IntUpdate {

    }

    public static class ProgressCtrlUpdateScaleZoneLength extends IntUpdate {

    }

    public static class ProgressCtrlUpdateScaleZonePadding extends IntUpdate {

    }

    public static class ProgressCtrlUpdateScaleZoneCornerRadius extends IntUpdate {

    }

    public static class ProgressCtrlUpdateProgressCornerRadius extends IntUpdate {

    }

    public static class ProgressCtrlUpdateProgressTextPaddingBottom extends IntUpdate {

    }

    public static class ProgressCtrlUpdateProgressTextMoved extends BoolUpdate {

    }

    public static class VideoPlayerCtrlUpdateVideoUrl extends StringUpdate {

    }

    public static class VideoPlayerCtrlUpdateProgress extends IntUpdate {

    }

    public static class VideoPlayerCtrlUpdatePlaying extends BoolUpdate {

    }

    public static class LineChartCtrlUpdatePoints extends Update {
        private List<Data.Point> value;

        public List<Data.Point> getValue() {
            return value;
        }

        public void setValue(List<Data.Point> value) {
            this.value = value;
        }
    }

    public static class LineChartCtrlUpdateDotsColor extends StringUpdate {

    }

    public static class LineChartCtrlUpdateDotsRadius extends FloatUpdate {

    }

    public static class LineChartCtrlUpdateDotsStrokeThickness extends FloatUpdate {

    }

    public static class LineChartCtrlUpdateDotsStrokeColor extends StringUpdate {

    }

    public static class LineChartCtrlUpdateLineDashedIntervals extends Update {
        private List<Data.FloatValue> value;

        public List<Data.FloatValue> getValue() {
            return value;
        }

        public void setValue(List<Data.FloatValue> value) {
            this.value = value;
        }
    }

    public static class LineChartCtrlUpdateLineSmooth extends BoolUpdate {

    }

    public static class LineChartCtrlUpdateLineThickness extends FloatUpdate {

    }

    public static class LineChartCtrlUpdateLineColor extends StringUpdate {

    }

    public static class LineChartCtrlUpdateLineBeginAt extends IntUpdate {

    }

    public static class LineChartCtrlUpdateLineEndAt extends IntUpdate {

    }

    public static class LineChartCtrlUpdateFillColor extends StringUpdate {

    }

    public static class LineChartCtrlUpdateGradients extends Update {
        private List<Data.StringValue> value;
        private List<Data.FloatValue> value2;

        public List<Data.StringValue> getValue() {
            return value;
        }

        public void setValue(List<Data.StringValue> value) {
            this.value = value;
        }

        public List<Data.FloatValue> getValue2() {
            return value2;
        }

        public void setValue2(List<Data.FloatValue> value2) {
            this.value2 = value2;
        }
    }

    public static class LineChartCtrlUpdateAxisColor extends StringUpdate {

    }

    public static class LineChartCtrlUpdateLabelsColor extends StringUpdate {

    }

    public static class BarChartCtrlUpdatePoints extends Update {
        private List<Data.Point> value;
        private List<Data.StringValue> value2;

        public List<Data.Point> getValue() {
            return value;
        }

        public void setValue(List<Data.Point> value) {
            this.value = value;
        }

        public List<Data.StringValue> getValue2() {
            return value2;
        }

        public void setValue2(List<Data.StringValue> value2) {
            this.value2 = value2;
        }
    }

    public static class BarChartCtrlUpdateBarSpacing extends IntUpdate {

    }

    public static class BarChartCtrlUpdateSetSpacing extends IntUpdate {

    }

    public static class BarChartCtrlUpdateBarBackgroundColor extends StringUpdate {

    }

    public static class BarChartCtrlUpdateRoundCorners extends IntUpdate {

    }

    public static class BarChartCtrlUpdateBarsColor extends StringUpdate {

    }

    public static class BarChartCtrlUpdateAxisColor extends StringUpdate {

    }

    public static class BarChartCtrlUpdateLabelsColor extends StringUpdate {

    }

    public static class ScrollerCtrlUpdatePosition extends IntUpdate {

    }

    public static class CompoundCtrlUpdateCaption extends StringUpdate {

    }

    public static class CompoundCtrlUpdateCaptionSize extends IntUpdate {

    }

    public static class CompoundCtrlUpdateCaptionColor extends StringUpdate {

    }

    public static class CompoundCtrlUpdateTintColor extends StringUpdate {

    }

    public static class CompoundCtrlUpdateChecked extends BoolUpdate {

    }

    public static class DropDownCtrlAddItem extends Update {
        private Controls.Control value;

        public Controls.Control getValue() {
            return value;
        }

        public void setValue(Controls.Control value) {
            this.value = value;
        }
    }

    public static class DropDownCtrlUpdateSelectedPos extends IntUpdate {

    }

    public static class RecyclerCtrlAddItem extends IntUpdate {
        private Controls.Control value2;

        public Controls.Control getValue2() {
            return value2;
        }

        public void setValue2(Controls.Control value) {
            this.value2 = value;
        }
    }

    public static class RecyclerCtrlUpdateItem extends IntUpdate {

    }

    public static class RecyclerCtrlRemoveItem extends IntUpdate {

    }

    public static class RecyclerCtrlUpdateRecyclerConfig extends IntUpdate {
        private Controls.RecyclerCtrl.RecyclerLayoutType value2;
        private Controls.RecyclerCtrl.RecyclerOrientation value3;

        public Controls.RecyclerCtrl.RecyclerLayoutType getValue2() {
            return value2;
        }

        public void setValue2(Controls.RecyclerCtrl.RecyclerLayoutType value2) {
            this.value2 = value2;
        }

        public Controls.RecyclerCtrl.RecyclerOrientation getValue3() {
            return value3;
        }

        public void setValue3(Controls.RecyclerCtrl.RecyclerOrientation value3) {
            this.value3 = value3;
        }
    }

    public static class SeekBarCtrlUpdateThumbColor extends StringUpdate {

    }

    public static class SeekBarCtrlUpdateThumbSize extends IntUpdate {

    }

    public static class SeekBarCtrlUpdateTrackColor extends StringUpdate {

    }

    public static class SeekBarCtrlUpdateTrackThickness extends IntUpdate {

    }

    public static class SeekBarCtrlUpdateProgress extends IntUpdate {

    }
}
