package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.Hashtable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.RoomsCreated;

public class ComplexProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private final List<Entities.Room> rooms;
    private Hashtable<Long, Entities.Message> sendingMessages;

    public ComplexProfileAdapter(AppCompatActivity activity, List<Entities.Room> rooms) {
        this.activity = activity;
        this.rooms = rooms;
        this.sendingMessages = new Hashtable<>();
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onMessageReceived(MessageReceived messageReceived) {
        Entities.Message message = messageReceived.getMessage();
        int counter = 0;
        for (Entities.Room room : rooms) {
            if (message.getRoom().getRoomId() == room.getRoomId()) {
                room.setLastAction(message);
                notifyItemChanged(counter);
                rooms.add(0, room);
                rooms.remove(counter + 1);
                notifyItemMoved(counter, 0);
                break;
            }
            counter++;
        }
    }

    @Subscribe
    public void onMessageSending(MessageSending messageSending) {
        Entities.Message message = messageSending.getMessage();
        int counter = 0;
        for (Entities.Room room : rooms) {
            if (message.getRoom().getRoomId() == room.getRoomId()) {
                sendingMessages.put(message.getMessageId(), message.clone());
                room.setLastAction(message);
                notifyItemChanged(counter);
                rooms.add(0, room);
                rooms.remove(counter);
                notifyItemMoved(counter, 0);
                break;
            }
            counter++;
        }
    }

    @Subscribe
    public void onMessageSent(MessageSent messageSent) {
        long localMessageId = messageSent.getLocalMessageId();
        long onlineMessageId = messageSent.getOnlineMessageId();
        Entities.Message message = sendingMessages.remove(localMessageId);
        message.setMessageId(onlineMessageId);
        int counter = 0;
        for (Entities.Room room : rooms) {
            if (message.getRoom().getRoomId() == room.getRoomId()) {
                room.setLastAction(message);
                notifyItemChanged(counter);
                rooms.add(0, room);
                rooms.remove(counter);
                notifyItemMoved(counter, 0);
                break;
            }
            counter++;
        }
    }

    @Subscribe
    public void onRoomCreated(RoomCreated roomCreated) {
        addRoom(roomCreated.getRoom());
    }

    @Subscribe
    public void onRoomsCreated(RoomsCreated roomsCreated) {
        for (Entities.Room room : rooms) {
            onRoomCreated(new RoomCreated(roomsCreated.getComplexId(), room));
        }
    }

    @Subscribe
    public void onRoomRemoved(RoomRemoved roomRemoved) {
        removeRoom(roomRemoved.getRoom());
    }

    @Subscribe
    public void onContactCreated(ContactCreated contactCreated) {
        addRoom(contactCreated.getContact().getComplex().getRooms().get(0));
    }

    private void addRoom(Entities.Room room) {
        boolean exists = false;
        for (Entities.Room r : rooms) {
            if (r.getRoomId() == room.getRoomId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            rooms.add(0, room);
            notifyItemInserted(0);
        }
    }

    private void removeRoom(Entities.Room room) {
        int counter = 0;
        for (Entities.Room r : rooms) {
            if (r.getRoomId() == room.getRoomId()) {
                rooms.remove(counter);
                notifyItemRemoved(counter + 2);
                break;
            }
            counter++;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RoomItem(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.home_room_item, parent, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Entities.Room room = rooms.get(position);
        RoomItem vh = (RoomItem) holder;
        NetworkHelper.loadRoomAvatar(room.getAvatar(), vh.avatarIV);
        vh.nameTV.setText(room.getTitle());
        Entities.Message lastAction = room.getLastAction();
        if (lastAction != null) {
            if (lastAction instanceof Entities.TextMessage)
                vh.lastActionTV.setText(lastAction.getAuthor().getTitle() + " : "
                        + ((Entities.TextMessage) lastAction).getText());
            else if (lastAction instanceof Entities.PhotoMessage)
                vh.lastActionTV.setText(lastAction.getAuthor().getTitle() + " : " + "Photo");
            else if (lastAction instanceof Entities.AudioMessage)
                vh.lastActionTV.setText(lastAction.getAuthor().getTitle() + " : " + "Audio");
            else if (lastAction instanceof Entities.VideoMessage)
                vh.lastActionTV.setText(lastAction.getAuthor().getTitle() + " : " + "Video");
            else if (lastAction instanceof Entities.ServiceMessage)
                vh.lastActionTV.setText("Aseman : "
                        + ((Entities.ServiceMessage) lastAction).getText());
        }
        vh.itemView.setOnClickListener(view ->
                activity.startActivity(new Intent(activity, RoomActivity.class)
                        .putExtra("complex_id", room.getComplex().getComplexId())
                        .putExtra("room_id", room.getRoomId())));
    }

    @Override
    public int getItemCount() {
        return this.rooms.size();
    }

    class RoomItem extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView nameTV;
        TextView lastActionTV;

        RoomItem(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.homeRoomItemAvatar);
            this.nameTV = itemView.findViewById(R.id.homeRoomItemName);
            this.lastActionTV = itemView.findViewById(R.id.homeRoomItemLastAction);
        }
    }
}
