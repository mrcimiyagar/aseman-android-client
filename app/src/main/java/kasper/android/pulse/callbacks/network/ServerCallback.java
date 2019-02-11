package kasper.android.pulse.callbacks.network;

import kasper.android.pulse.models.network.Packet;

public interface ServerCallback {
    void onRequestSuccess(Packet packet);
    void onServerFailure();
    void onConnectionFailure();
}
