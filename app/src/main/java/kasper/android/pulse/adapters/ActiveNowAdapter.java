package kasper.android.pulse.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ProfileActivity;
import kasper.android.pulse.callbacks.ui.ProfileListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;

public class ActiveNowAdapter extends RecyclerView.Adapter<ActiveNowAdapter.ActiveItem> {

    private AppCompatActivity activity;
    private List<Entities.User> users;

    public ActiveNowAdapter(AppCompatActivity activity, List<Entities.User> users) {
        this.activity = activity;
        this.users = users;
        this.notifyDataSetChanged();
        GraphicHelper.addProfileListener(new ProfileListener() {
            @Override
            public void profileUpdated(Entities.User user) {
                GraphicHelper.runOnUiThread(() -> {
                    int counter = 0;
                    for (Entities.User u : users) {
                        if (u.getBaseUserId() == user.getBaseUserId()) {
                            u.setTitle(user.getTitle());
                            u.setAvatar(user.getAvatar());
                            notifyItemChanged(counter);
                            break;
                        }
                        counter++;
                    }
                });
            }

            @Override
            public void profileUpdated(Entities.Complex complex) {

            }

            @Override
            public void profileUpdated(Entities.Room room) {

            }

            @Override
            public void profileUpdated(Entities.Bot bot) {

            }
        });
    }

    public void addContact(Entities.Contact contact) {
        this.users.add(1, contact.getPeer());
        this.notifyItemInserted(1);
    }

    @NonNull
    @Override
    public ActiveItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ActiveItem(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.active_now_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ActiveItem holder, int position) {
        Entities.User user = users.get(position);
        NetworkHelper.loadUserAvatar(user.getAvatar(), holder.avatarIV);
        holder.titleTV.setText(user.getTitle());
        holder.itemView.setOnClickListener(view ->
                activity.startActivity(new Intent(activity, ProfileActivity.class)
                        .putExtra("user-id", user.getBaseUserId())));
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    class ActiveItem extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView titleTV;

        ActiveItem(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.activeNowAvatar);
            this.titleTV = itemView.findViewById(R.id.activeNowTitle);
        }
    }
}
