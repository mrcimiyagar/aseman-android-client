package kasper.android.pulse.adapters;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.UserHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by keyhan1376 on 3/7/2018.
 */

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.InviteVH> {

    AppCompatActivity activity;
    List<Entities.Invite> invites;

    public InviteAdapter(AppCompatActivity activity, List<Entities.Invite> invites) {
        this.activity = activity;
        this.invites = invites;
        this.notifyDataSetChanged();
    }

    @Override
    public InviteVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new InviteVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_invites, parent, false));
    }

    @Override
    public void onBindViewHolder(final InviteVH holder, int position) {
        Entities.Invite invite = this.invites.get(position);
        DataSyncer.syncBaseUserWithServer(invite.getUserId(), new OnBaseUserSyncListener() {
            @Override
            public void userSynced(Entities.BaseUser baseUser) {
                invite.setUser((Entities.User) baseUser);
                notifyItemChanged(holder.getAdapterPosition());
            }

            @Override
            public void syncFailed() { }
        });
    }

    @Override
    public int getItemCount() {
        return this.invites.size();
    }

    class InviteVH extends RecyclerView.ViewHolder {

        TextView nameTV;

        InviteVH(View itemView) {
            super(itemView);
            this.nameTV = itemView.findViewById(R.id.adapter_invites_name_text_view);
        }
    }
}
