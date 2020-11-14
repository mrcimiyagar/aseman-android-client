package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.PulseHandler;
import kasper.android.pulse.rxbus.notifications.BotViewAnimated;
import kasper.android.pulse.rxbus.notifications.BotViewDelivered;
import kasper.android.pulse.rxbus.notifications.BotViewRanCommands;
import kasper.android.pulse.rxbus.notifications.BotViewUpdated;
import kasper.android.pulseframework.components.PulseView;
import kasper.android.pulseframework.interfaces.IClickNotifier;
import retrofit2.Call;

public class BotWindowActivity extends BaseActivity {

    private long complexId, roomId, botId;

    PulseView pulseView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_window);

        Core.getInstance().bus().register(this);

        if (this.getIntent().getExtras() != null) {
            if (this.getIntent().getExtras().containsKey("complex-id"))
                complexId = this.getIntent().getExtras().getLong("complex-id");
            if (this.getIntent().getExtras().containsKey("room-id"))
                roomId = this.getIntent().getExtras().getLong("room-id");
            if (this.getIntent().getExtras().containsKey("bot-id"))
                botId = this.getIntent().getExtras().getLong("bot-id");
        }

        pulseView = findViewById(R.id.pulseView);
        pulseView.setup(this, controlId -> {

        });

        Packet packet2 = new Packet();
        Entities.Complex packComplex = new Entities.Complex();
        packComplex.setComplexId(complexId);
        packet2.setComplex(packComplex);
        Entities.Room packRoom = new Entities.Room();
        packRoom.setRoomId(roomId);
        packet2.setBaseRoom(packRoom);
        Entities.Bot b = new Entities.Bot();
        b.setBaseUserId(botId);
        packet2.setBot(b);
        packet2.setBotWindowMode(true);
        Entities.Workership w = new Entities.Workership();
        w.setWidth((int)(GraphicHelper.getScreenWidth() / GraphicHelper.getDensity()));
        w.setHeight((int)(GraphicHelper.getScreenHeight() / GraphicHelper.getDensity()));
        packet2.setWorkership(w);
        Call<Packet> call2 = NetworkHelper.getRetrofit()
                .create(PulseHandler.class).requestBotView(packet2);
        NetworkHelper.requestServer(call2, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) { }
            @Override
            public void onServerFailure() { }
            @Override
            public void onConnectionFailure() { }
        });
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onBotViewDelivered(BotViewDelivered botViewDelivered) {
        if (complexId == botViewDelivered.getComplexId() && roomId == botViewDelivered.getRoomId() && botViewDelivered.isBotWindowMode()) {
            if (botViewDelivered.getBotId() == botId) {
                pulseView.buildUi(botViewDelivered.getData());
            }
        }
    }

    @Subscribe
    public void onBotViewUpdated(BotViewUpdated botViewUpdated) {
        if (complexId == botViewUpdated.getComplexId() && roomId == botViewUpdated.getRoomId() && botViewUpdated.isBotWindowMode()) {
            if (botViewUpdated.getBotId() == botId) {
                if (botViewUpdated.isBatchData())
                    pulseView.updateBatchUi(botViewUpdated.getUpdateData());
                else
                    pulseView.updateUi(botViewUpdated.getUpdateData());
            }
        }
    }

    @Subscribe
    public void onBotViewAnimated(BotViewAnimated botViewAnimated) {
        if (complexId == botViewAnimated.getComplexId() && roomId == botViewAnimated.getRoomId() && botViewAnimated.isBotWindowMode()) {
            if (botViewAnimated.getBotId() == botId) {
                if (botViewAnimated.isBatchData())
                    pulseView.animateBatchUi(botViewAnimated.getAnimData());
                else
                    pulseView.animateUi(botViewAnimated.getAnimData());
            }
        }
    }

    @Subscribe
    public void onBotViewRanCommands(BotViewRanCommands botViewRanCommands) {
        if (complexId == botViewRanCommands.getComplexId() && roomId == botViewRanCommands.getRoomId() && botViewRanCommands.isBotWindowMode()) {
            if (botViewRanCommands.getBotId() == botId) {
                if (botViewRanCommands.isBatchData())
                    pulseView.runCommands(botViewRanCommands.getCommandsData());
                else
                    pulseView.runCommand(botViewRanCommands.getCommandsData());
            }
        }
    }
}
