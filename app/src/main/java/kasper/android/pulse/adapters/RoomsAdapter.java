package kasper.android.pulse.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.middleware.OnRoomSyncListener;
import kasper.android.pulse.callbacks.ui.OnRoomIconClickListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomVH> {

    private AppCompatActivity activity;
    private long complexId;
    private List<Entities.Room> rooms;
    private OnRoomIconClickListener roomOpeningCallback;

    public RoomsAdapter(AppCompatActivity activity, long complexId, List<Entities.Room> rooms, OnRoomIconClickListener roomOpeningCallback) {
        this.activity = activity;
        this.complexId = complexId;
        this.rooms = rooms;
        this.roomOpeningCallback = roomOpeningCallback;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_rooms, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RoomVH holder, int position) {
        final Entities.Room room = this.rooms.get(position);
        holder.titleTV.setText(room.getTitle());
        if (room.getAvatar() >= 0) {
            NetworkHelper.loadRoomAvatar(room.getAvatar(), holder.avatarIV);
        }
        final int pos = position;
        DataSyncer.syncRoomWithServer(complexId, room.getRoomId(), new OnRoomSyncListener() {
            @Override
            public void roomSynced(Entities.Room room) {
                rooms.set(pos, room);
                holder.titleTV.setText(room.getTitle());
                NetworkHelper.loadRoomAvatar(room.getAvatar(), holder.avatarIV);
            }
            @Override
            public void syncFailed() { }
        });
        holder.itemView.setOnClickListener(view -> roomOpeningCallback.roomSelected(room));
    }

    @Override
    public int getItemCount() {
        return this.rooms.size();
    }

    public void addRoom(Entities.Room room) {
        GraphicHelper.runOnUiThread(() -> {
            boolean exists = false;
            for (Entities.Room r : rooms) {
                if (r.getRoomId() == room.getRoomId()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                rooms.add(room);
                notifyItemInserted(rooms.size() - 1);
            }
        });
    }

    public void removeRoom(Entities.Room room) {
        GraphicHelper.runOnUiThread(() -> {
            int counter = 0;
            for (Entities.Room r : rooms) {
                if (r.getRoomId() == room.getRoomId()) {
                    final int index = counter;
                    rooms.remove(index);
                    GraphicHelper.runOnUiThread(() -> notifyItemRemoved(index));
                    break;
                }
                counter++;
            }
        });
    }

    class RoomVH extends RecyclerView.ViewHolder {

        private ImageView avatarIV;
        private TextView titleTV;

        RoomVH(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.roomAvatar);
            this.titleTV = itemView.findViewById(R.id.roomTitle);
        }
    }
}
