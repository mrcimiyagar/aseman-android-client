package kasper.android.pulse.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.anadeainc.rxbus.Subscribe;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.PulseHandler;
import kasper.android.pulse.rxbus.notifications.BotViewDelivered;
import kasper.android.pulseframework.components.PulseView;
import retrofit2.Call;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class WidgetsAdapter extends RecyclerView.Adapter<WidgetsAdapter.WidgetHolder> {

    private AppCompatActivity activity;
    private long complexId;
    private long roomId;
    private List<Entities.Workership> workers;
    private Hashtable<Long, PulseView> pulseTable;

    public WidgetsAdapter(AppCompatActivity activity, long complexId, long roomId, List<Entities.Workership> workers) {
        this.activity = activity;
        this.complexId = complexId;
        this.roomId = roomId;
        this.workers = workers;
        this.pulseTable = new Hashtable<>();
        Core.getInstance().bus().register(this);
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onBotViewDelivered(BotViewDelivered bvd) {
        if (bvd.getComplexId() == complexId && bvd.getRoomId() == roomId) {
            PulseView pulseView = this.pulseTable.get(bvd.getBotId());
            if (pulseView != null) {
                pulseView.buildUi(bvd.getData());
            }
        }
    }

    @NonNull
    @Override
    public WidgetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        FrameLayout layout = new FrameLayout(activity);
        layout.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, GraphicHelper.dpToPx(200)));
        layout.setBackgroundColor(Color.WHITE);
        return new WidgetHolder(layout);
    }

    @Override
    public void onBindViewHolder(@NonNull WidgetHolder holder, int position) {
        ((FrameLayout)holder.itemView).addView(create(workers.get(position)));
    }

    @Override
    public void onViewRecycled(@NonNull WidgetHolder holder) {
        ((FrameLayout)holder.itemView).removeAllViews();
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return workers.size();
    }

    class WidgetHolder extends RecyclerView.ViewHolder {

        WidgetHolder(View itemView) {
            super(itemView);
        }
    }

    private PulseView create(Entities.Workership ws) {

        if (pulseTable.containsKey(ws.getBotId()))
            return pulseTable.get(ws.getBotId());

        Entities.Bot bot = new Entities.Bot();
        bot.setBaseUserId(ws.getBotId());
        PulseView pulseView = new PulseView(activity);
        pulseView.setup(activity, controlId -> {
            Packet packet = new Packet();
            Entities.Complex c = new Entities.Complex();
            c.setComplexId(this.complexId);
            packet.setComplex(c);
            Entities.Room r = new Entities.Room();
            r.setRoomId(this.roomId);
            packet.setBaseRoom(r);
            packet.setBot(bot);
            packet.setControlId(controlId);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(PulseHandler.class).clickBotView(packet)
                    , new ServerCallback() {
                        @Override
                        public void onRequestSuccess(Packet packet) { }
                        @Override
                        public void onServerFailure() { }
                        @Override
                        public void onConnectionFailure() { }
                    });
        });
        pulseView.setLayoutParams(new RelativeLayout.LayoutParams(ws.getWidth()
                == 0 ? MATCH_PARENT : ws
                .getWidth() == -1 ? RelativeLayout.LayoutParams.WRAP_CONTENT
                : GraphicHelper.dpToPx(ws.getWidth()), ws.getHeight() == 0
                ? MATCH_PARENT : ws.getHeight()
                == -1 ? RelativeLayout.LayoutParams.WRAP_CONTENT : GraphicHelper
                .dpToPx(ws.getHeight())));
        pulseView.setX(GraphicHelper.dpToPx(ws.getPosX()));
        pulseView.setY(GraphicHelper.dpToPx(ws.getPosY()));
        if (ws.getPosX() == -1)
            ((RelativeLayout.LayoutParams) pulseView.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
        pulseTable.put(bot.getBaseUserId(), pulseView);
        PulseHelper.getPulseViewTable().put(bot.getBaseUserId(), pulseView);
        Packet packet2 = new Packet();
        Entities.Complex packComplex = new Entities.Complex();
        packComplex.setComplexId(this.complexId);
        packet2.setComplex(packComplex);
        Entities.Room packRoom = new Entities.Room();
        packRoom.setRoomId(this.roomId);
        packet2.setBaseRoom(packRoom);
        packet2.setBot(bot);
        Toast.makeText(activity, this.complexId + " " + this.roomId, Toast.LENGTH_LONG).show();
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
        pulseView.setTag("PulseView" + bot.getBaseUserId());
        return pulseView;
    }
}
