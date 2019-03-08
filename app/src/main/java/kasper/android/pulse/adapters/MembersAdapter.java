package kasper.android.pulse.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ModifyMemberAccessActivity;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.MemberVH> {

    private AppCompatActivity activity;
    private List<Entities.Membership> memberships;
    private Entities.MemberAccess myMemberAccess;
    private long myId = 0, complexId = 0;
    private Entities.Complex complex;

    public MembersAdapter(AppCompatActivity activity, long myId, long complexId, List<Entities.Membership> memberships) {
        this.activity = activity;
        this.memberships = memberships;
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
            for (int counter = 0; counter < this.memberships.size(); counter++) {
                notifyItemChanged(counter);
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
    }

    @Override
    public int getItemCount() {
        return this.memberships.size();
    }

    class MemberVH extends RecyclerView.ViewHolder {
        CircleImageView avatarIV;
        TextView titleTV;
        ImageButton settingsBTN;

        MemberVH(View itemView) {
            super(itemView);
            avatarIV = itemView.findViewById(R.id.adapterMembersAvatarIV);
            titleTV = itemView.findViewById(R.id.adapterMembersTitleTV);
            settingsBTN = itemView.findViewById(R.id.adapterMembersModifyAccessBTN);
        }
    }
}
