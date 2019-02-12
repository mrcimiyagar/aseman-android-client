package kasper.android.pulse.adapters;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.WorkershipDataActivity;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.rxbus.notifications.WorkerAdded;
import kasper.android.pulse.rxbus.notifications.WorkerRemoved;
import retrofit2.Call;

public class BotsFullAdapter extends RecyclerView.Adapter<BotsFullAdapter.BotVH> {

    private AppCompatActivity activity;
    private long complexId;
    private long roomId;
    private List<Entities.Bot> bots;
    private List<Entities.Workership> existingBots;

    public BotsFullAdapter(AppCompatActivity activity, long complexId, long roomId
            , List<Entities.Bot> bots, List<Entities.Workership> existingBots) {
        this.activity = activity;
        this.complexId = complexId;
        this.roomId = roomId;
        this.bots = bots;
        this.existingBots = existingBots;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BotVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BotVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bots_full, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final BotVH holder, int position) {
        final Entities.Bot bot = this.bots.get(position);
        holder.titleTV.setText(bot.getTitle());
        NetworkHelper.loadBotAvatar(bot.getAvatar(), holder.avatarIV);
        boolean contains = false;
        for (Entities.Workership aBot : existingBots) {
            if (aBot.getBotId() == bot.getBaseUserId()) {
                contains = true;
                break;
            }
        }
        if (contains) {
            holder.settingsContainer.setVisibility(View.VISIBLE);
            holder.settingsBtn.setOnClickListener(view -> {
                Entities.Workership ws = null;
                for (Entities.Workership workership : existingBots) {
                    if (workership.getBotId() == bot.getBaseUserId()) {
                        ws = workership;
                        break;
                    }
                }
                if (ws != null)
                    activity.startActivity(new Intent(activity, WorkershipDataActivity.class)
                            .putExtra("bot_id", bot.getBaseUserId())
                            .putExtra("complex_id", complexId)
                            .putExtra("room_id", roomId)
                            .putExtra("pos_x", ws.getPosX())
                            .putExtra("pos_y", ws.getPosY())
                            .putExtra("width", ws.getWidth())
                            .putExtra("height", ws.getHeight()));
            });
            holder.addBtn.setImageResource(R.drawable.minus);
            holder.addBtn.setOnClickListener(view -> {
                Packet packet = new Packet();
                packet.setBot(bot);
                Entities.Complex complex = new Entities.Complex();
                complex.setComplexId(complexId);
                packet.setComplex(complex);
                Entities.Room room = new Entities.Room();
                room.setRoomId(roomId);
                packet.setRoom(room);
                RobotHandler robotHandler = NetworkHelper.getRetrofit().create(RobotHandler.class);
                Call<Packet> call = robotHandler.removeBotFromRoom(packet);
                NetworkHelper.requestServer(call, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        for (int counter = 0; counter < existingBots.size(); counter++) {
                            if (bot.getBaseUserId() == existingBots.get(counter).getBotId()) {
                                Entities.Workership ws = existingBots.remove(counter);
                                Core.getInstance().bus().post(new WorkerRemoved(ws));
                                break;
                            }
                        }
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                    @Override
                    public void onServerFailure() {
                        Toast.makeText(activity, "Bot removing failure", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionFailure() {
                        Toast.makeText(activity, "Bot removing failure", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        } else {
            holder.settingsContainer.setVisibility(View.GONE);
            holder.addBtn.setImageResource(R.drawable.ic_add);
            holder.addBtn.setOnClickListener(view -> {
                Packet packet = new Packet();
                packet.setBot(bot);
                Entities.Complex complex = new Entities.Complex();
                complex.setComplexId(complexId);
                packet.setComplex(complex);
                Entities.Room room = new Entities.Room();
                room.setRoomId(roomId);
                packet.setRoom(room);
                final Entities.Workership workership = new Entities.Workership();
                workership.setPosX(0);
                workership.setPosY(0);
                workership.setWidth(150);
                workership.setHeight(150);
                packet.setWorkership(workership);
                RobotHandler robotHandler = NetworkHelper.getRetrofit().create(RobotHandler.class);
                Call<Packet> call = robotHandler.addBotToRoom(packet);
                NetworkHelper.requestServer(call, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        Entities.Workership ws = packet.getWorkership();
                        existingBots.add(ws);
                        Core.getInstance().bus().post(new WorkerAdded(ws));
                        notifyItemChanged(holder.getAdapterPosition());
                    }

                    @Override
                    public void onServerFailure() {
                        Toast.makeText(activity, "Bot adding failure", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionFailure() {
                        Toast.makeText(activity, "Bot adding failure", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
    }

    @Override
    public int getItemCount() {
        return this.bots.size();
    }

    class BotVH extends RecyclerView.ViewHolder {

        TextView titleTV;
        CircleImageView avatarIV;
        ImageButton addBtn;
        CardView settingsContainer;
        ImageButton settingsBtn;

        BotVH(View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.botCardTitle);
            avatarIV = itemView.findViewById(R.id.botCardAvatar);
            addBtn = itemView.findViewById(R.id.botsFullAddBtn);
            settingsContainer = itemView.findViewById(R.id.botsFullSettingsContainer);
            settingsBtn = itemView.findViewById(R.id.botsFullSettingsBtn);
        }
    }
}
