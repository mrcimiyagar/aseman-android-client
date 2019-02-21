package kasper.android.pulse.adapters;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.ComplexProfileActivity;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;

public class ComplexesSearchAdapter extends RecyclerView.Adapter<ComplexesSearchAdapter.ComplexVH> {

    private AppCompatActivity activity;
    private List<Entities.Complex> complexes;

    public ComplexesSearchAdapter(AppCompatActivity activity, List<Entities.Complex> complexes) {
        this.activity = activity;
        this.complexes = complexes;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ComplexVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ComplexVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_complexes_search, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ComplexVH holder, final int position) {
        final Entities.Complex complex = this.complexes.get(position);
        holder.nameTV.setText(complex.getTitle());
        NetworkHelper.loadComplexAvatar(complex.getAvatar(), holder.avatarIV);
        holder.itemView.setOnClickListener(view -> {
            if (DatabaseHelper.isComplexInDatabase(complex.getComplexId())) {
                activity.startActivity(new Intent(activity, ComplexProfileActivity.class)
                        .putExtra("complex", complex));
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.complexes.size();
    }

    class ComplexVH extends RecyclerView.ViewHolder {

        ImageView avatarIV;
        TextView nameTV;

        ComplexVH(View itemView) {
            super(itemView);
            avatarIV = itemView.findViewById(R.id.complexesSearchAvatar);
            nameTV = itemView.findViewById(R.id.complexesSearchName);
        }
    }
}