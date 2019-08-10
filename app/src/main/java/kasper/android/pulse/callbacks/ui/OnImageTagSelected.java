package kasper.android.pulse.callbacks.ui;

import java.util.List;

import kasper.android.pulse.models.extras.YoloBoundingBox;

public interface OnImageTagSelected {
    void imageTagSelected(List<YoloBoundingBox> boxes);
}
