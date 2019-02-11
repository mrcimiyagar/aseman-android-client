package kasper.android.pulse.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.BotStoreBotActivity;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;

public class StoreBotsAdapter extends RecyclerView.Adapter<StoreBotsAdapter.BotVH> {

    private AppCompatActivity activity;
    private List<Entities.Bot> bots;

    public StoreBotsAdapter(AppCompatActivity activity, List<Entities.Bot> bots) {
        this.activity = activity;
        this.bots = bots;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BotVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BotVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_store_bots, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull BotVH holder, int position) {
        final Entities.Bot bot = this.bots.get(position);
        holder.titleTV.setText(bot.getTitle());
        NetworkHelper.loadBotAvatar(bot.getAvatar(), holder.avatarIV);
        holder.itemView.setOnClickListener(view -> activity
                .startActivity(new Intent(activity, BotStoreBotActivity.class)
                .putExtra("bot", bot)));
    }

    @Override
    public int getItemCount() {
        return this.bots.size();
    }

    class BotVH extends RecyclerView.ViewHolder {

        ImageView avatarIV;
        TextView titleTV;

        BotVH(View itemView) {
            super(itemView);
            avatarIV = itemView.findViewById(R.id.storeBotAvatar);
            titleTV = itemView.findViewById(R.id.storeBotTitle);
        }
    }
}
