package kasper.android.pulse.adapters;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityOptionsCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import com.anadeainc.rxbus.Subscribe;

import java.util.Hashtable;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.middleware.OnComplexSyncListener;
import kasper.android.pulse.callbacks.network.PreviewPulseListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.MyDragShadowBuilder;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.PulseHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.rxbus.notifications.BotPicked;
import kasper.android.pulse.rxbus.notifications.BotViewDelivered;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ComplexProfileUpdated;
import kasper.android.pulse.rxbus.notifications.ComplexRemoved;
import kasper.android.pulseframework.components.PulseView;
import kasper.android.pulseframework.interfaces.IClickNotifier;
import kasper.android.pulseframework.utils.GraphicsHelper;

public class BotPickerAdapter extends RecyclerView.Adapter<BotPickerAdapter.BpVh> {

    private AppCompatActivity activity;
    private View.OnDragListener dragListener;
    private long complexId;
    private long roomId;
    private List<Entities.Bot> bots;
    private Hashtable<Long, PulseView> pulseViews;

    public BotPickerAdapter(AppCompatActivity activity, View.OnDragListener dragListener, long complexId, long roomId, List<Entities.Bot> bots) {
        this.activity = activity;
        this.dragListener = dragListener;
        this.bots = bots;
        this.complexId = complexId;
        this.roomId = roomId;
        this.pulseViews = new Hashtable<>();
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onBotViewDelivered(BotViewDelivered botViewDelivered) {
        if (botViewDelivered.getComplexId() == 0 && botViewDelivered.getRoomId() == 0) {
            PulseView pulseView = pulseViews.get(botViewDelivered.getBotId());
            if (pulseView != null) pulseView.buildUi(botViewDelivered.getData());
        }
    }

    @NonNull
    @Override
    public BpVh onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BpVh(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bot_picker, parent, false), viewType);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull final BpVh holder, final int position) {
        Entities.Bot bot = bots.get(position);
        holder.titleTV.setText(bot.getTitle());
        holder.descTV.setText(bot.getDescription());
        Packet packet = new Packet();
        Entities.Complex c = new Entities.Complex();
        c.setComplexId(complexId);
        packet.setComplex(c);
        Entities.BaseRoom r = new Entities.BaseRoom();
        r.setRoomId(roomId);
        packet.setBaseRoom(r);
        Entities.Bot b = new Entities.Bot();
        b.setBaseUserId(bot.getBaseUserId());
        packet.setBot(b);
        Entities.Workership w = new Entities.Workership();
        w.setWidth(150);
        w.setHeight(150);
        packet.setWorkership(w);
        holder.preview.setup(activity, (controlId) -> {});
        this.pulseViews.put(bot.getBaseUserId(), holder.preview);
        holder.preview.setTag("PulseView" + bot.getBaseUserId());
        holder.preview.setOnLongClickListener(v -> {
            Toast.makeText(activity, "hello", Toast.LENGTH_SHORT).show();
            ClipData.Item item = new ClipData.Item(holder.preview.getTag().toString());
            ClipData dragData = new ClipData(
                    holder.preview.getTag().toString(),
                    new String[] { ClipDescription.MIMETYPE_TEXT_PLAIN },
                    item);
            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(holder.preview, true);
            holder.preview.startDrag(dragData,
                    myShadow,
                    holder.preview,
                    0
            );
            Core.getInstance().bus().post(new BotPicked(bot, holder.preview.getRoot(), 0, 0, 0, 0));
            return true;
        });
        NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(PulseHandler.class).requestBotPreview(packet), new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) { }
            @Override
            public void onServerFailure() { }
            @Override
            public void onConnectionFailure() { }
        });
    }

    @Override
    public void onViewRecycled(@NonNull BpVh holder) {
        super.onViewRecycled(holder);
        this.pulseViews.remove(holder.botId);
    }

    @Override
    public int getItemCount() {
        return this.bots.size();
    }

    class BpVh extends RecyclerView.ViewHolder {

        long botId;
        PulseView preview;
        TextView titleTV;
        TextView descTV;
        LinearLayout parent;
        LinearLayout parentParent;

        BpVh(View itemView, long botId) {
            super(itemView);
            this.botId = botId;
            preview = itemView.findViewById(R.id.botPickerPreview);
            titleTV = itemView.findViewById(R.id.botPickerTitle);
            descTV = itemView.findViewById(R.id.botPickerDesc);
            parent = itemView.findViewById(R.id.pulseViewParent);
            parentParent = itemView.findViewById(R.id.pulseViewParentParent);
        }
    }
}