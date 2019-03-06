package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.components.OneClickImageButton;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.InviteHandler;
import kasper.android.pulse.rxbus.notifications.InviteResolved;
import kasper.android.pulse.rxbus.notifications.ShowToast;

public class ComplexesInviteAdapter extends RecyclerView.Adapter<ComplexesInviteAdapter.ComplexHolder> {

    private List<Entities.Invite> invites;

    public ComplexesInviteAdapter(List<Entities.Invite> invites) {
        this.invites =  invites;
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @NonNull
    @Override
    public ComplexHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ComplexHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_complexes_invite, parent, false));
    }

    @Subscribe
    public void onInviteResolved(InviteResolved inviteResolved) {
        int counter = 0;
        for (Entities.Invite invite : invites) {
            if (invite.getInviteId() == inviteResolved.getInvite().getInviteId()) {
                invites.remove(counter);
                notifyItemRemoved(counter);
                break;
            }
            counter++;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ComplexHolder holder, int position) {
        Entities.Invite invite = invites.get(position);
        NetworkHelper.loadComplexAvatar(invite.getUser().getAvatar(), holder.avatarIV);
        holder.titleTV.setText(invite.getUser().getTitle());
        holder.cancel.setOnClickListener(v -> {
            Packet packet = new Packet();
            Entities.Complex complex = new Entities.Complex();
            complex.setComplexId(invite.getComplex().getComplexId());
            packet.setComplex(complex);
            Entities.User user = new Entities.User();
            user.setBaseUserId(invite.getUser().getBaseUserId());
            packet.setUser(user);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(InviteHandler.class).cancelInvite(packet)
                    , new ServerCallback() {
                        @Override
                        public void onRequestSuccess(Packet packet) {
                            DatabaseHelper.notifyInviteResolved(invite);
                            Core.getInstance().bus().post(new InviteResolved(invite));
                        }
                        @Override
                        public void onServerFailure() {
                            Core.getInstance().bus().post(new ShowToast("Invite cancellation failure"));
                        }
                        @Override
                        public void onConnectionFailure() {
                            Core.getInstance().bus().post(new ShowToast("Invite cancellation failure"));
                        }
                    });
        });
    }

    @Override
    public int getItemCount() {
        return invites.size();
    }

    class ComplexHolder extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView titleTV;
        OneClickImageButton cancel;

        ComplexHolder(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.inviteAdapterAvatar);
            this.titleTV = itemView.findViewById(R.id.inviteAdapterTitle);
            this.cancel = itemView.findViewById(R.id.inviteAdapterCancel);
        }
    }
}
