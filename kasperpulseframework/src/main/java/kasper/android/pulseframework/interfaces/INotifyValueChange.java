package kasper.android.pulseframework.interfaces;

import kasper.android.pulseframework.models.Codes;

public interface INotifyValueChange {
    void notifyValueChanged(String ctrlName, Codes.Value value);
}
