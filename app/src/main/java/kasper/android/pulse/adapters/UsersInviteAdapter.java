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
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.InviteHandler;
import kasper.android.pulse.rxbus.notifications.InviteResolved;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import retrofit2.Call;

public class UsersInviteAdapter extends RecyclerView.Adapter<UsersInviteAdapter.ComplexHolder> {

    private long userId;
    private List<Entities.Complex> complexes;

    public UsersInviteAdapter(long userId, List<Entities.Complex> complexes) {
        this.userId = userId;
        this.complexes = complexes;
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
                .inflate(R.layout.adapter_users_invite, parent, false));
    }

    @Subscribe
    public void onInviteResolved(InviteResolved inviteResolved) {
        int counter = 0;
        for (Entities.Complex complex : complexes) {
            if (complex.getComplexId() == inviteResolved.getInvite().getComplex().getComplexId()) {
                notifyItemChanged(counter);
                break;
            }
            counter++;
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ComplexHolder holder, int position) {
        Entities.Complex complex = complexes.get(position);
        NetworkHelper.loadComplexAvatar(complex.getAvatar(), holder.avatarIV);
        holder.titleTV.setText(complex.getTitle());
        Entities.Invite invite = DatabaseHelper.getInviteByComplexAndUserId(userId, complex.getComplexId());
        if (invite != null) {
            holder.inviteStateTag.setTextColor(Core.getInstance().getResources().getColor(android.R.color.holo_red_light));
            holder.inviteStateTag.setBackgroundResource(R.drawable.tag_background_cancel);
            holder.inviteStateTag.setText("Cancel Invite");
            holder.itemView.setOnClickListener(view -> {
                Packet packet = new Packet();
                Entities.Complex c = new Entities.Complex();
                c.setComplexId(complex.getComplexId());
                packet.setComplex(c);
                Entities.User user = new Entities.User();
                user.setBaseUserId(userId);
                packet.setUser(user);
                Call<Packet> call = NetworkHelper.getRetrofit().create(InviteHandler.class).cancelInvite(packet);
                NetworkHelper.requestServer(call, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        DatabaseHelper.notifyInviteResolved(invite);
                        notifyItemChanged(holder.getAdapterPosition());
                        Core.getInstance().bus().post(new ShowToast("Invite cancelled."));
                    }
                    @Override
                    public void onServerFailure() {
                        DatabaseHelper.notifyInviteResolved(invite);
                        notifyItemChanged(holder.getAdapterPosition());
                        Core.getInstance().bus().post(new ShowToast("Invite cancelled."));
                    }
                    @Override
                    public void onConnectionFailure() {
                        DatabaseHelper.notifyInviteResolved(invite);
                        notifyItemChanged(holder.getAdapterPosition());
                        Core.getInstance().bus().post(new ShowToast("Invite cancelled."));
                    }
                });
            });
        } else {
            if (!DatabaseHelper.isUserMemberOfComplex(complex.getComplexId(), userId)) {
                holder.inviteStateTag.setTextColor(Core.getInstance().getResources().getColor(R.color.colorBlue));
                holder.inviteStateTag.setBackgroundResource(R.drawable.tag_background_action);
                holder.inviteStateTag.setText("Send Invite");
                holder.itemView.setOnClickListener(view -> {
                    Packet packet = new Packet();
                    Entities.Complex c = new Entities.Complex();
                    c.setComplexId(complex.getComplexId());
                    packet.setComplex(c);
                    Entities.User user = new Entities.User();
                    user.setBaseUserId(userId);
                    packet.setUser(user);
                    Call<Packet> call = NetworkHelper.getRetrofit().create(InviteHandler.class).createInvite(packet);
                    NetworkHelper.requestServer(call, new ServerCallback() {
                        @Override
                        public void onRequestSuccess(Packet packet) {
                            DatabaseHelper.notifyInviteSent(packet.getInvite());
                            notifyItemChanged(holder.getAdapterPosition());
                            Core.getInstance().bus().post(new ShowToast("Invite sent."));
                        }

                        @Override
                        public void onServerFailure() {
                            Core.getInstance().bus().post(new ShowToast("Invite Sending failure"));
                        }

                        @Override
                        public void onConnectionFailure() {
                            Core.getInstance().bus().post(new ShowToast("Invite Sending failure"));
                        }
                    });
                });
            } else {
                holder.inviteStateTag.setTextColor(Core.getInstance().getResources().getColor(R.color.colorGreen));
                holder.inviteStateTag.setBackgroundResource(R.drawable.tag_background_included);
                holder.inviteStateTag.setText("Already Member");
                holder.itemView.setOnClickListener(view -> { });
            }
        }
    }

    @Override
    public int getItemCount() {
        return complexes.size();
    }

    class ComplexHolder extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView titleTV;
        TextView inviteStateTag;

        ComplexHolder(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.complexSelectorAvatar);
            this.titleTV = itemView.findViewById(R.id.complexSelectorTitle);
            this.inviteStateTag = itemView.findViewById(R.id.inviteStateTag);
        }
    }
}
