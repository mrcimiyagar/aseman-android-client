package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ChatActivity;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.middleware.OnRoomSyncListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.RoomsCreated;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private final List<Entities.Room> rooms;
    private Hashtable<Long, Entities.Message> sendingMessages;
    private RecyclerView peopleRV;

    public HomeAdapter(AppCompatActivity activity, List<Entities.Room> rooms) {
        this.activity = activity;
        this.rooms = rooms;
        this.sendingMessages = new Hashtable<>();
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        if (peopleRV != null && peopleRV.getAdapter() != null)
            ((ActiveNowAdapter) peopleRV.getAdapter()).dispose();
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onMessageReceived(MessageReceived messageReceived) {
        Entities.Message message = messageReceived.getMessage();
        int counter = 0;
        for (Entities.Room room : rooms) {
            if (message.getRoom().getRoomId() == room.getRoomId()) {
                room.setLastAction(message);
                notifyItemChanged(counter + 2);
                rooms.add(0, room);
                rooms.remove(counter + 1);
                notifyItemMoved(counter + 2, 2);
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
                notifyItemChanged(counter + 2);
                rooms.add(0, room);
                rooms.remove(counter + 1);
                notifyItemMoved(counter + 2, 2);
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
                notifyItemChanged(counter + 2);
                rooms.add(0, room);
                rooms.remove(counter + 1);
                notifyItemMoved(counter + 2, 2);
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
            notifyItemInserted(2);
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

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getAdapterPosition() == 0) {
            if (peopleRV != null && peopleRV.getAdapter() != null)
                ((ActiveNowAdapter) peopleRV.getAdapter()).dispose();
            peopleRV = null;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new PeopleItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_people_item, parent, false));
        } else if (viewType == 1) {
            return new ConvHeaderItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_conv_header, parent, false));
        } else if (viewType == 2) {
            return new RoomItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_room_item, parent, false));
        } else {
            return new RoomItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.home_room_item, parent, false));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : position == 1 ? 1 : 2;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position == 0) {
            PeopleItem vh = (PeopleItem) holder;
            peopleRV = vh.peopleRV;
            vh.peopleRV.setLayoutManager(new LinearLayoutManager(activity, RecyclerView.HORIZONTAL, false));
            List<Entities.Contact> contacts = DatabaseHelper.getContacts();
            List<Entities.User> users = new ArrayList<>();
            users.add(DatabaseHelper.getMe());
            for (Entities.Contact contact : contacts) {
                users.add(contact.getPeer());
            }
            vh.peopleRV.setAdapter(new ActiveNowAdapter(activity, users));
        } else if (position != 1) {
            Entities.Room room = rooms.get(position - 2);
            RoomItem vh = (RoomItem) holder;
            if (room.getComplex().getMode() == 2 || room.getComplex().getTitle().length() == 0) {
                Entities.Contact contact = DatabaseHelper.getContactByComplexId(room.getComplexId());
                Entities.User user = contact.getPeer();
                DataSyncer.syncBaseUserWithServer(user.getBaseUserId(), new OnBaseUserSyncListener() {
                    @Override
                    public void userSynced(Entities.BaseUser baseUser) {
                        DataSyncer.syncRoomWithServer(room.getComplexId(), room.getRoomId(), new OnRoomSyncListener() {
                            @Override
                            public void roomSynced(Entities.Room room) {
                                try {
                                    vh.nameTV.setText(baseUser.getTitle().split(" ")[0] + " : " + room.getTitle());
                                    NetworkHelper.loadRoomAvatar(baseUser.getAvatar(), vh.avatarIV);
                                } catch (Exception ignored) { }
                            }
                            @Override
                            public void syncFailed() { }
                        });
                    }
                    @Override
                    public void syncFailed() { }
                });
            } else {
                NetworkHelper.loadRoomAvatar(room.getAvatar(), vh.avatarIV);
                vh.nameTV.setText(room.getComplex().getTitle() + " : " + room.getTitle());
                DataSyncer.syncRoomWithServer(room.getComplexId(), room.getRoomId(), new OnRoomSyncListener() {
                    @Override
                    public void roomSynced(Entities.Room room) {
                        try {
                            NetworkHelper.loadRoomAvatar(room.getAvatar(), vh.avatarIV);
                            vh.nameTV.setText(room.getComplex().getTitle() + " : " + room.getTitle());
                        } catch (Exception ignored) { }
                    }
                    @Override
                    public void syncFailed() { }
                });
            }
            Entities.Message lastAction = room.getLastAction();
            if (lastAction != null) {
                if (lastAction instanceof Entities.TextMessage)
                    vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : "
                            + ((Entities.TextMessage) lastAction).getText());
                else if (lastAction instanceof Entities.PhotoMessage)
                    vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : " + "Photo");
                else if (lastAction instanceof Entities.AudioMessage)
                    vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : " + "Audio");
                else if (lastAction instanceof Entities.VideoMessage)
                    vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : " + "Video");
                else if (lastAction instanceof Entities.ServiceMessage)
                    vh.lastActionTV.setText("Aseman : "
                            + ((Entities.ServiceMessage) lastAction).getText());
            } else {
                vh.lastActionTV.setText("");
            }
            vh.itemView.setOnClickListener(view ->
                    activity.startActivity(new Intent(activity, ChatActivity.class)
                            .putExtra("complex_id", room.getComplex().getComplexId())
                            .putExtra("room_id", room.getRoomId())
                            .putExtra("start_file_id", -1L)));
        }
    }

    @Override
    public int getItemCount() {
        return this.rooms.size() + 2;
    }

    class PeopleItem extends RecyclerView.ViewHolder {

        RecyclerView peopleRV;

        PeopleItem(View itemView) {
            super(itemView);
            this.peopleRV = itemView.findViewById(R.id.homePeopleRV);
        }
    }

    class ConvHeaderItem extends RecyclerView.ViewHolder {

        ConvHeaderItem(View itemView) {
            super(itemView);
        }
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
