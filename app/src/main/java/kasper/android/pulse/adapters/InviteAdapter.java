package kasper.android.pulse.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ComplexProfileActivity;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.components.OneClickImageButton;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.InviteHandler;
import kasper.android.pulse.retrofit.MessageHandler;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.InviteCancelled;
import kasper.android.pulse.rxbus.notifications.InviteCreated;
import kasper.android.pulse.rxbus.notifications.MembershipCreated;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.ShowToast;

/**
 * Created by keyhan1376 on 3/7/2018.
 */

public class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.InviteVH> {

    private AppCompatActivity activity;
    private List<Entities.Invite> invites;
    private List<Boolean> enablers;

    public InviteAdapter(AppCompatActivity activity, List<Entities.Invite> invites) {
        this.activity = activity;
        this.invites = invites;
        this.enablers = new ArrayList<>();
        for (int counter = 0; counter < this.invites.size(); counter++) {
            this.enablers.add(true);
        }
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onInviteCreated(InviteCreated inviteCreated) {
        invites.add(inviteCreated.getInvite());
        enablers.add(true);
        notifyItemInserted(invites.size() - 1);
    }

    @Subscribe
    public void onInviteCancelled(InviteCancelled inviteCancelled) {
        int counter = 0;
        for (Entities.Invite invite : invites) {
            if (invite.getInviteId() == inviteCancelled.getInvite().getInviteId()) {
                invites.remove(counter);
                enablers.remove(counter);
                notifyItemRemoved(counter);
                break;
            }
            counter++;
        }
    }

    @NonNull
    @Override
    public InviteVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new InviteVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_invites, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull InviteVH holder, int position) {
        Entities.Invite invite = this.invites.get(position);
        boolean enabled = this.enablers.get(position);
        NetworkHelper.loadComplexAvatar(invite.getComplex().getAvatar(), holder.avatarIV);
        holder.nameTV.setText(invite.getComplex().getTitle());
        if (enabled) {
            holder.accept.enable();
            holder.ignore.enable();
        } else {
            holder.accept.disable();
            holder.ignore.disable();
        }
        holder.itemView.setOnClickListener(v ->
                activity.startActivity(new Intent(activity, ComplexProfileActivity.class)
                .putExtra("complex", invite.getComplex())));
        holder.accept.setOnClickListener(v -> {
            Packet request = new Packet();
            Entities.Complex complex = new Entities.Complex();
            complex.setComplexId(invite.getComplex().getComplexId());
            request.setComplex(complex);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(InviteHandler.class)
                    .acceptInvite(request), new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet result) {
                    DatabaseHelper.notifyUserCreated(result.getMembership().getUser());
                    DatabaseHelper.notifyComplexCreated(result.getMembership().getComplex());
                    DatabaseHelper.notifyComplexSecretCreated(result.getComplexSecret());
                    for (Entities.BaseRoom room : result.getMembership().getComplex().getAllRooms())
                        DatabaseHelper.notifyRoomCreated(room);
                    for (Entities.Bot bot : result.getBots())
                        DatabaseHelper.notifyBotCreated(bot, null);
                    for (Entities.Membership membership : result.getMembership().getComplex().getMembers()) {
                        DatabaseHelper.notifyUserCreated(membership.getUser());
                        DatabaseHelper.notifyMembershipCreated(membership);
                    }
                    DatabaseHelper.notifyMemberAccessCreated(result.getMembership().getMemberAccess());
                    DatabaseHelper.notifyMembershipCreated(result.getMembership());
                    DatabaseHelper.notifyServiceMessageReceived(result.getServiceMessage());
                    DatabaseHelper.notifyInviteResolved(invite);

                    invites.remove(holder.getAdapterPosition());
                    enablers.remove(holder.getAdapterPosition());
                    Core.getInstance().bus().post(new ComplexCreated(result.getMembership().getComplex()));
                    for (Entities.BaseRoom room : result.getMembership().getComplex().getAllRooms()) {
                        room.setComplex(result.getMembership().getComplex());
                        Core.getInstance().bus().post(new RoomCreated(result.getMembership().getComplex().getComplexId(), room));
                    }
                    for (Entities.Membership membership : result.getMembership().getComplex().getMembers()) {
                        membership.setComplex(result.getMembership().getComplex());
                        Core.getInstance().bus().post(new MembershipCreated(membership));
                    }
                    Core.getInstance().bus().post(new MembershipCreated(result.getMembership()));
                    Entities.MessageLocal messageLocal = new Entities.MessageLocal();
                    messageLocal.setMessageId(result.getServiceMessage().getMessageId());
                    messageLocal.setSent(true);
                    Core.getInstance().bus().post(new MessageReceived(true, result.getServiceMessage(), messageLocal));

                    Packet p = new Packet();
                    p.setBaseRooms(new ArrayList<>());
                    for (Entities.BaseRoom room : result.getMembership().getComplex().getAllRooms()) {
                        Entities.Room r = new Entities.Room();
                        r.setRoomId(room.getRoomId());
                        r.setComplexId(room.getComplexId());
                        p.getBaseRooms().add(r);
                    }
                    NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(MessageHandler.class).getLastActions(p)
                            , new ServerCallback() {
                                @Override
                                public void onRequestSuccess(Packet packet) {
                                    for (Entities.BaseRoom room : packet.getBaseRooms()) {
                                        if (room.getLastAction() != null) {
                                            if (room.getLastAction() instanceof Entities.TextMessage)
                                                DatabaseHelper.notifyTextMessageReceived((Entities.TextMessage) room.getLastAction());
                                            else if (room.getLastAction() instanceof Entities.PhotoMessage)
                                                DatabaseHelper.notifyPhotoMessageReceived((Entities.PhotoMessage) room.getLastAction());
                                            else if (room.getLastAction() instanceof Entities.AudioMessage)
                                                DatabaseHelper.notifyAudioMessageReceived((Entities.AudioMessage) room.getLastAction());
                                            else if (room.getLastAction() instanceof Entities.VideoMessage)
                                                DatabaseHelper.notifyVideoMessageReceived((Entities.VideoMessage) room.getLastAction());
                                            else if (room.getLastAction() instanceof Entities.ServiceMessage)
                                                DatabaseHelper.notifyServiceMessageReceived((Entities.ServiceMessage) room.getLastAction());
                                            if (room.getLastAction() != null) {
                                                Entities.MessageLocal messageLocal = new Entities.MessageLocal();
                                                messageLocal.setMessageId(room.getLastAction().getMessageId());
                                                messageLocal.setSent(true);
                                                Core.getInstance().bus().post(new MessageReceived(true, room.getLastAction(), messageLocal));
                                            }
                                        }
                                    }
                                    Core.getInstance().bus().post(new ShowToast("Invite accepted."));
                                    notifyItemRemoved(holder.getAdapterPosition());
                                }
                                @Override
                                public void onServerFailure() {
                                    Core.getInstance().bus().post(new ShowToast("Invite accepted."));
                                    notifyItemRemoved(holder.getAdapterPosition());
                                }
                                @Override
                                public void onConnectionFailure() {
                                    Core.getInstance().bus().post(new ShowToast("Invite accepted."));
                                    notifyItemRemoved(holder.getAdapterPosition());
                                }
                            });
                }
                @Override
                public void onServerFailure() {
                    Core.getInstance().bus().post(new ShowToast("Accepting invite failure"));
                    enablers.set(holder.getAdapterPosition(), true);
                    notifyItemChanged(holder.getAdapterPosition());
                }
                @Override
                public void onConnectionFailure() {
                    Core.getInstance().bus().post(new ShowToast("Accepting invite failure"));
                    enablers.set(holder.getAdapterPosition(), true);
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        });
        holder.ignore.setOnClickListener(v -> {
            Packet packet = new Packet();
            Entities.Complex complex = new Entities.Complex();
            complex.setComplexId(invite.getComplex().getComplexId());
            packet.setComplex(complex);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(InviteHandler.class)
                    .ignoreInvite(packet), new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    DatabaseHelper.notifyInviteResolved(invite);
                    invites.remove(holder.getAdapterPosition());
                    enablers.remove(holder.getAdapterPosition());
                    Core.getInstance().bus().post(new ShowToast("Invite accepted."));
                    notifyItemRemoved(holder.getAdapterPosition());
                }
                @Override
                public void onServerFailure() {
                    Core.getInstance().bus().post(new ShowToast("Accepting invite failure"));
                    enablers.set(holder.getAdapterPosition(), true);
                    notifyItemChanged(holder.getAdapterPosition());
                }
                @Override
                public void onConnectionFailure() {
                    Core.getInstance().bus().post(new ShowToast("Accepting invite failure"));
                    enablers.set(holder.getAdapterPosition(), true);
                    notifyItemChanged(holder.getAdapterPosition());
                }
            });
        });
    }

    @Override
    public int getItemCount() {
        return this.invites.size();
    }

    class InviteVH extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView nameTV;
        OneClickImageButton accept;
        OneClickImageButton ignore;
        private boolean enabled;

        InviteVH(View itemView) {
            super(itemView);
            this.enabled = true;
            this.avatarIV = itemView.findViewById(R.id.inviteAdapterAvatar);
            this.nameTV = itemView.findViewById(R.id.inviteAdapterTitle);
            this.accept = itemView.findViewById(R.id.invitesAdapterAccept);
            this.ignore = itemView.findViewById(R.id.invitesAdapterIgnore);
        }
    }
}
