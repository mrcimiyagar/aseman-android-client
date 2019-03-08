package kasper.android.pulse.callbacks.network;

import kasper.android.pulse.models.network.Packet;

public interface ServerCallback2 {
    void onRequestSuccess(Packet packet);
    void onLogincalError(String errorCode);
    void onServerFailure();
    void onConnectionFailure();
}
