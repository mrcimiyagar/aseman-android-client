package kasper.android.pulse.adapters;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;

import java.util.Hashtable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ModifyMemberAccessActivity;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import retrofit2.Call;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberVH> {

    private AppCompatActivity activity;
    private List<Entities.Membership> memberships;
    private Hashtable<Long, Entities.SingleRoom> singleRooms;
    private Entities.MemberAccess myMemberAccess;
    private long myId = 0, complexId = 0;
    private Entities.Complex complex;

    public MembersAdapter(AppCompatActivity activity, long myId, long complexId, List<Entities.Membership> memberships, List<Entities.BaseRoom> singleRooms) {
        this.activity = activity;
        this.memberships = memberships;
        this.singleRooms = new Hashtable<>();
        for (Entities.BaseRoom sr : singleRooms) {
            if (((Entities.SingleRoom)sr).getUser1Id() == myId)
                this.singleRooms.put(((Entities.SingleRoom)sr).getUser2Id(), ((Entities.SingleRoom)sr));
            else
                this.singleRooms.put(((Entities.SingleRoom)sr).getUser1Id(), ((Entities.SingleRoom)sr));
        }
        this.myId = myId;
        this.complexId = complexId;
        this.myMemberAccess = DatabaseHelper.getMemberAccessByComplexAndUserId(complexId, myId);
        this.complex = DatabaseHelper.getComplexById(complexId);
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onMemberAccessUpdated(MemberAccessUpdated updated) {
        if (updated.getMemberAccess().getMembership().getUserId() == myId
                && updated.getMemberAccess().getMembership().getComplexId() == complexId) {
            myMemberAccess = updated.getMemberAccess();
            synchronized (memberships) {
                for (int counter = 0; counter < this.memberships.size(); counter++) {
                    notifyItemChanged(counter);
                }
            }
        }
    }

    @Subscribe
    public void onRoomCreated(RoomCreated roomCreated) {
        if (roomCreated.getRoom() instanceof Entities.SingleRoom) {
            Entities.SingleRoom sr = (Entities.SingleRoom) roomCreated.getRoom();
            long peerId = 0;
            if (sr.getUser1Id() == myId) {
                this.singleRooms.put(sr.getUser2Id(), sr);
                peerId = sr.getUser2Id();
            } else {
                this.singleRooms.put(sr.getUser1Id(), sr);
                peerId = sr.getUser1Id();
            }
            synchronized (memberships) {
                for (int counter = 0; counter < memberships.size(); counter++) {
                    if (memberships.get(counter).getUserId() == peerId) {
                        notifyItemChanged(counter);
                        break;
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    public MemberVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MemberVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_members, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MemberVH holder, int position) {
        Entities.Membership membership = this.memberships.get(position);
        NetworkHelper.loadUserAvatar(membership.getUser().getAvatar(), holder.avatarIV);
        holder.titleTV.setText(membership.getUser().getTitle());
        if (myId == membership.getUserId() ||
                (complex.getComplexSecret() != null && complex.getComplexSecret().getAdminId() == membership.getUserId()))
            holder.settingsBTN.setVisibility(View.GONE);
        else {
            if (myMemberAccess.isCanModifyAccess()) {
                holder.settingsBTN.setOnClickListener(v ->
                        activity.startActivity(new Intent(activity, ModifyMemberAccessActivity.class)
                                .putExtra("complex_id", membership.getComplexId())
                                .putExtra("user_id", membership.getUserId())));
                holder.settingsBTN.setVisibility(View.VISIBLE);
            } else
                holder.settingsBTN.setVisibility(View.GONE);
        }
        if (myId == membership.getUserId()) {
            holder.singleRoomBTN.setVisibility(View.GONE);
        } else {
            holder.singleRoomBTN.setVisibility(View.VISIBLE);
            Entities.SingleRoom sr = this.singleRooms.get(membership.getUserId());
            if (sr != null) {
                holder.singleRoomBTN.setColorFilter(
                        Core.getInstance().getResources().getColor(R.color.colorBlue), PorterDuff.Mode.SRC_ATOP);
                holder.singleRoomBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        activity.startActivity(new Intent(activity, RoomActivity.class)
                                .putExtra("complex_id", sr.getComplexId())
                                .putExtra("room_id", sr.getRoomId()));
                    }
                });
            } else {
                holder.singleRoomBTN.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                holder.singleRoomBTN.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Packet p = new Packet();
                        Entities.Complex com = new Entities.Complex();
                        com.setComplexId(complexId);
                        p.setComplex(com);
                        Entities.User u = new Entities.User();
                        u.setBaseUserId(membership.getUserId());
                        p.setUser(u);
                        p.setSingleRoomMode(true);
                        Call<Packet> call = NetworkHelper.getRetrofit().create(RoomHandler.class).createRoom(p);
                        NetworkHelper.requestServer(call, new ServerCallback() {
                            @Override
                            public void onRequestSuccess(Packet packet) {
                                DatabaseHelper.notifyRoomCreated(packet.getBaseRoom());
                                DatabaseHelper.notifyServiceMessageReceived(packet.getServiceMessage());
                                Core.getInstance().bus().post(new RoomCreated(complexId, packet.getBaseRoom()));
                                Entities.MessageLocal messageLocal = new Entities.MessageLocal();
                                messageLocal.setMessageId(packet.getServiceMessage().getMessageId());
                                messageLocal.setSent(true);
                                Core.getInstance().bus().post(new MessageReceived(true, packet.getServiceMessage(), messageLocal));
                                singleRooms.put(membership.getUserId(), (Entities.SingleRoom) packet.getBaseRoom());
                                notifyItemChanged(holder.getAdapterPosition());
                            }

                            @Override
                            public void onServerFailure() {
                                Toast.makeText(activity, "Room creation failure", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onConnectionFailure() {
                                Toast.makeText(activity, "Room creation failure", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return this.memberships.size();
    }

    class MemberVH extends RecyclerView.ViewHolder {
        CircleImageView avatarIV;
        TextView titleTV;
        ImageButton singleRoomBTN;
        ImageButton settingsBTN;

        MemberVH(View itemView) {
            super(itemView);
            avatarIV = itemView.findViewById(R.id.adapterMembersAvatarIV);
            titleTV = itemView.findViewById(R.id.adapterMembersTitleTV);
            singleRoomBTN = itemView.findViewById(R.id.adapterMembersCreateSingleRoomBTN);
            settingsBTN = itemView.findViewById(R.id.adapterMembersModifyAccessBTN);
        }
    }
}
