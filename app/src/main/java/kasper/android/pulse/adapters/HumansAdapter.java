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
import kasper.android.pulse.activities.ProfileActivity;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;

/**
 * Created by keyhan1376 on 3/9/2018.
 */

public class HumansAdapter extends RecyclerView.Adapter<HumansAdapter.HumanVH> {

    private AppCompatActivity activity;
    private List<Entities.User> users;

    public HumansAdapter(AppCompatActivity activity, List<Entities.User> users) {
        this.activity = activity;
        this.users = users;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HumanVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HumanVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_humans, parent, false));
    }

    @Override
    public void onBindViewHolder(HumanVH holder, int position) {
        final Entities.User user = this.users.get(position);
        holder.nameTV.setText(user.getTitle());
        GlideApp.with(activity).load(NetworkHelper
                .createFileLink(user.getAvatar())).into(holder.avatarIV);
        holder.itemView.setOnClickListener(v -> {
            DatabaseHelper.notifyUserCreated(user);
            activity.startActivity(new Intent(activity, ProfileActivity.class)
                    .putExtra("user-id", user.getBaseUserId()));
        });
    }

    @Override
    public int getItemCount() {
        return this.users.size();
    }

    class HumanVH extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView nameTV;

        HumanVH(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.adapter_humans_avatar_image_view);
            this.nameTV = itemView.findViewById(R.id.adapter_humans_name_text_view);
        }
    }
}
