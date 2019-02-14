package kasper.android.pulse.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ProfileActivity;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.UserProfileUpdated;

public class ActiveNowAdapter extends RecyclerView.Adapter<ActiveNowAdapter.ActiveItem> {

    private AppCompatActivity activity;
    private List<Entities.User> users;

    public ActiveNowAdapter(AppCompatActivity activity, List<Entities.User> users) {
        this.activity = activity;
        this.users = users;
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onProfileUpdated(UserProfileUpdated profileUpdated) {
        Entities.User user = profileUpdated.getUser();
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
    }

    @Subscribe
    public void onContactCreated(ContactCreated contactCreated) {
        Entities.Contact contact = contactCreated.getContact();
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
        holder.itemView.setOnClickListener(view ->
                activity.startActivity(new Intent(activity, ProfileActivity.class)
                        .putExtra("user-id", user.getBaseUserId())));
        DataSyncer.syncBaseUserWithServer(user.getBaseUserId(), new OnBaseUserSyncListener() {
            @Override
            public void userSynced(Entities.BaseUser baseUser) {
                try {
                    NetworkHelper.loadUserAvatar(baseUser.getAvatar(), holder.avatarIV);
                    holder.titleTV.setText(baseUser.getTitle().split(" ")[0]);
                } catch (Exception ignored) { }
            }
            @Override
            public void syncFailed() { }
        });
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
