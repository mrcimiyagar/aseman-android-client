package kasper.android.pulse.adapters;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.BotProfileActivity;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.entities.Entities;

public class BotsSimpleAdapter extends RecyclerView.Adapter<BotsSimpleAdapter.BotVH> {

    private AppCompatActivity activity;
    private List<Entities.Bot> bots;

    public BotsSimpleAdapter(AppCompatActivity activity, List<Entities.Bot> bots) {
        this.activity = activity;
        this.bots = bots;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BotVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BotVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_bots, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final BotVH holder, int position) {
        final Entities.Bot bot = this.bots.get(position);
        holder.titleTV.setText(bot.getTitle());
        NetworkHelper.loadBotAvatar(bot.getAvatar(), holder.avatarIV);
        holder.itemView.setOnClickListener(view ->
                activity.startActivity(new Intent(activity, BotProfileActivity.class)
                        .putExtra("bot_id", bot.getBaseUserId())
                        .putExtra("token", bot.getSessions().get(0).getSessionId() + " " +
                                bot.getBotSecret().getToken())));
    }

    @Override
    public int getItemCount() {
        return this.bots.size();
    }

    class BotVH extends RecyclerView.ViewHolder {

        TextView titleTV;
        CircleImageView avatarIV;

        BotVH(View itemView) {
            super(itemView);
            titleTV = itemView.findViewById(R.id.botCardTitle);
            avatarIV = itemView.findViewById(R.id.botCardAvatar);
        }
    }
}
