package kasper.android.pulse.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.anadeainc.rxbus.Subscribe;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.middleware.OnComplexSyncListener;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.ui.OnComplexIconClickListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ComplexProfileUpdated;
import kasper.android.pulse.rxbus.notifications.ComplexRemoved;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;

public class ComplexesAdapter extends RecyclerView.Adapter<ComplexesAdapter.ComplexVH> {

    private AppCompatActivity activity;
    private List<Entities.Complex> complexes;
    private OnComplexIconClickListener callback;
    private Runnable addComplexClicked;

    public ComplexesAdapter(AppCompatActivity activity, List<Entities.Complex> complexes, OnComplexIconClickListener callback, Runnable addComplexClicked) {
        this.activity = activity;
        this.complexes = complexes;
        this.callback = callback;
        this.addComplexClicked = addComplexClicked;
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @NonNull
    @Override
    public ComplexVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ComplexVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_complexes, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ComplexVH holder, final int position) {
        if (position == complexes.size()) {
            holder.iconIV.setImageResource(R.drawable.ic_add);
            holder.iconIV.setPadding(GraphicHelper.dpToPx(8), GraphicHelper.dpToPx(8), GraphicHelper.dpToPx(8), GraphicHelper.dpToPx(8));
            holder.itemView.setOnClickListener(view -> addComplexClicked.run());
        } else {
            final Entities.Complex complex = this.complexes.get(position);
            if (complex.getTitle().length() == 0) {
                Entities.Contact contact = DatabaseHelper.getContactByComplexId(complex.getComplexId());
                final Entities.User user = DatabaseHelper.getHumanById(contact.getPeerId());
                if (user != null) {
                    NetworkHelper.loadUserAvatar(user.getAvatar(), holder.iconIV);
                }
                DataSyncer.syncBaseUserWithServer(contact.getPeerId(), new OnBaseUserSyncListener() {
                    @Override
                    public void userSynced(Entities.BaseUser user) {
                        NetworkHelper.loadUserAvatar(user.getAvatar(), holder.iconIV);
                    }
                    @Override
                    public void syncFailed() { }
                });
            } else {
                if (complex.getAvatar() >= 0) {
                    NetworkHelper.loadComplexAvatar(complex.getAvatar(), holder.iconIV);
                }
                DataSyncer.syncComplexWithServer(complex.getComplexId(), new OnComplexSyncListener() {
                    @Override
                    public void complexSynced(Entities.Complex complex) {
                        complexes.set(position, complex);
                        NetworkHelper.loadComplexAvatar(complex.getAvatar(), holder.iconIV);
                    }

                    @Override
                    public void syncFailed() { }
                });
            }
            holder.iconIV.setPadding(0, 0, 0, 0);
            holder.itemView.setOnClickListener(view -> callback.complexSelected(complex));
        }
    }

    @Override
    public int getItemCount() {
        return this.complexes.size() + 1;
    }

    @Subscribe
    public void onComplexCreated(ComplexCreated complexCreated) {
        addComplex(complexCreated.getComplex());
    }

    @Subscribe
    public void onContactCreated(ContactCreated contactCreated) {
        Entities.Complex complex = contactCreated.getContact().getComplex();
        Entities.Room room = complex.getRooms().get(0);
        room.setComplex(complex);
        addComplex(complex);
    }

    @Subscribe
    public void onProfileUpdated(ComplexProfileUpdated profileUpdated) {
        updateComplex(profileUpdated.getComplex());
    }

    @Subscribe
    public void onComplexRemoved(ComplexRemoved complexRemoved) {
        removeComplex(complexRemoved.getComplexId());
    }

    private void addComplex(Entities.Complex complex) {
        boolean exists = false;
        for (Entities.Complex c : complexes) {
            if (c.getComplexId() == complex.getComplexId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            complexes.add(complex);
            notifyItemInserted(complexes.size() - 1);
        }
    }

    private void updateComplex(Entities.Complex complex) {
        int counter = 0;
        for (Entities.Complex c : complexes) {
            if (c.getComplexId() == complex.getComplexId()) {
                complexes.set(counter, complex);
                notifyItemChanged(counter);
                break;
            }
            counter++;
        }
    }

    private void removeComplex(long complexId) {
        int counter = 0;
        boolean found = false;
        for (Entities.Complex complex : complexes) {
            if (complex.getComplexId() == complexId) {
                found = true;
                break;
            }
            counter++;
        }
        if (found) {
            complexes.remove(counter);
            notifyItemRemoved(counter);
        }
    }

    class ComplexVH extends RecyclerView.ViewHolder {

        CircleImageView bgIV;
        ImageView iconIV;

        ComplexVH(View itemView) {
            super(itemView);
            bgIV = itemView.findViewById(R.id.complexBGIV);
            iconIV = itemView.findViewById(R.id.complexIconIV);
        }
    }
}