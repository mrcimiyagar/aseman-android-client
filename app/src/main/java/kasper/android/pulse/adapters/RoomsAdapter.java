package kasper.android.pulse.adapters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.middleware.OnRoomSyncListener;
import kasper.android.pulse.callbacks.ui.OnRoomIconClickListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.RoomsCreated;

public class RoomsAdapter extends RecyclerView.Adapter<RoomsAdapter.RoomVH> {

    private AppCompatActivity activity;
    private long complexId;
    private List<Entities.BaseRoom> rooms;
    private OnRoomIconClickListener roomOpeningCallback;

    public RoomsAdapter(AppCompatActivity activity, long complexId, List<Entities.BaseRoom> rooms, OnRoomIconClickListener roomOpeningCallback) {
        this.activity = activity;
        this.complexId = complexId;
        this.rooms = rooms;
        this.roomOpeningCallback = roomOpeningCallback;
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onContactCreated(ContactCreated contactCreated) {
        if (complexId == contactCreated.getContact().getComplex().getComplexId()) {
            addRoom(contactCreated.getContact().getComplex().getAllRooms().get(0));
        }
    }

    @Subscribe
    public void onRoomCreated(RoomCreated roomCreated) {
        if (complexId == roomCreated.getComplexId()) {
            addRoom(roomCreated.getRoom());
        }
    }

    @Subscribe
    public void orRoomsCreated(RoomsCreated roomsCreated) {
        for (Entities.Room room : roomsCreated.getRooms()) {
            onRoomCreated(new RoomCreated(roomsCreated.getComplexId(), room));
        }
    }

    @Subscribe
    public void onRoomRemoved(RoomRemoved roomRemoved) {
        if (complexId == roomRemoved.getRoom().getComplexId()) {
            removeRoom(roomRemoved.getRoom());
        }
    }

    @NonNull
    @Override
    public RoomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_rooms, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RoomVH holder, int position) {
        final Entities.BaseRoom room = this.rooms.get(position);
        holder.titleTV.setText(room.getTitle());
        if (room.getAvatar() >= 0) {
            NetworkHelper.loadRoomAvatar(room.getAvatar(), holder.avatarIV);
        }
        final int pos = position;
        DataSyncer.syncRoomWithServer(complexId, room.getRoomId(), new OnRoomSyncListener() {
            @Override
            public void roomSynced(Entities.BaseRoom room) {
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

    private void addRoom(Entities.BaseRoom room) {
        boolean exists = false;
        for (Entities.BaseRoom r : rooms) {
            if (r.getRoomId() == room.getRoomId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            rooms.add(room);
            notifyItemInserted(rooms.size() - 1);
        }
    }

    private void removeRoom(Entities.BaseRoom room) {
        int counter = 0;
        for (Entities.BaseRoom r : rooms) {
            if (r.getRoomId() == room.getRoomId()) {
                rooms.remove(counter);
                notifyItemRemoved(counter);
                break;
            }
            counter++;
        }
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
