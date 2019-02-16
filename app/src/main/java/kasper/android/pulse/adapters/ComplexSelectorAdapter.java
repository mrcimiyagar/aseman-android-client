package kasper.android.pulse.adapters;

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
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;

public class ComplexSelectorAdapter extends RecyclerView.Adapter<ComplexSelectorAdapter.ComplexHolder> {

    private List<Entities.Complex> complexes;
    private OnComplexSelectionListener callback;

    public ComplexSelectorAdapter(List<Entities.Complex> complexes, OnComplexSelectionListener callback) {
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

    @Override
    public void onBindViewHolder(@NonNull ComplexHolder holder, int position) {
        Entities.Complex complex = complexes.get(position);
        NetworkHelper.loadComplexAvatar(complex.getAvatar(), holder.avatarIV);
        holder.titleTV.setText(complex.getTitle());
        holder.itemView.setOnClickListener(view -> callback.complexSelected(complex));
    }

    @Override
    public int getItemCount() {
        return complexes.size();
    }

    class ComplexHolder extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView titleTV;

        ComplexHolder(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.complexSelectorAvatar);
            this.titleTV = itemView.findViewById(R.id.complexSelectorTitle);
        }
    }
}
