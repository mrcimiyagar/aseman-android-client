package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.ui.OnComplexSelectionListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;

public class ComplexSelectorAdapter extends RecyclerView.Adapter<ComplexSelectorAdapter.ComplexHolder> {

    private long userId;
    private List<Entities.Complex> complexes;
    private OnComplexSelectionListener callback;

    public ComplexSelectorAdapter(long userId, List<Entities.Complex> complexes, OnComplexSelectionListener callback) {
        this.userId = userId;
        this.complexes = complexes;
        this.callback = callback;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplexHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ComplexHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_complex_selector, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ComplexHolder holder, int position) {
        Entities.Complex complex = complexes.get(position);
        NetworkHelper.loadComplexAvatar(complex.getAvatar(), holder.avatarIV);
        holder.titleTV.setText(complex.getTitle());
        if (userId > 0) {
            if (DatabaseHelper.doesInviteExist(complex.getComplexId(), userId)) {
                holder.inviteStateTag.setTextColor(Core.getInstance().getResources().getColor(R.color.colorGreen));
                holder.inviteStateTag.setBackgroundResource(R.drawable.tag_background_included);
                holder.inviteStateTag.setText("Invited");
                holder.itemView.setOnClickListener(view -> { });
            } else {
                holder.inviteStateTag.setTextColor(Core.getInstance().getResources().getColor(R.color.colorBlue));
                holder.inviteStateTag.setBackgroundResource(R.drawable.tag_background_action);
                holder.inviteStateTag.setText("Invite");
                holder.itemView.setOnClickListener(view -> callback.complexSelected(complex));
            }
            holder.inviteStateTag.setVisibility(View.VISIBLE);
        } else {
            holder.inviteStateTag.setVisibility(View.GONE);
            holder.itemView.setOnClickListener(view -> callback.complexSelected(complex));
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
