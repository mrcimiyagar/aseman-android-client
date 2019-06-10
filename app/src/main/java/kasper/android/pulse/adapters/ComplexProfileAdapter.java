package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;

import java.util.Hashtable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class ComplexProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private long myId;
    private final List<Entities.BaseRoom> rooms;
    private Hashtable<Long, Entities.Message> sendingMessages;

    public ComplexProfileAdapter(AppCompatActivity activity, long myId, List<Entities.BaseRoom> rooms) {
        this.activity = activity;
        this.myId = myId;
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
        if (messageReceived.isBottom()) {
            Entities.Message message = messageReceived.getMessage();
            int counter = 0;
            for (Entities.BaseRoom room : rooms) {
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
    }

    @Subscribe
    public void onMessageSending(MessageSending messageSending) {
        Entities.Message message = messageSending.getMessage();
        int counter = 0;
        for (Entities.BaseRoom room : rooms) {
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
        for (Entities.BaseRoom room : rooms) {
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
        for (Entities.BaseRoom room : rooms) {
            onRoomCreated(new RoomCreated(roomsCreated.getComplexId(), room));
        }
    }

    @Subscribe
    public void onRoomRemoved(RoomRemoved roomRemoved) {
        removeRoom(roomRemoved.getRoom());
    }

    @Subscribe
    public void onContactCreated(ContactCreated contactCreated) {
        addRoom(contactCreated.getContact().getComplex().getAllRooms().get(0));
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
            rooms.add(0, room);
            notifyItemInserted(0);
        }
    }

    private void removeRoom(Entities.BaseRoom room) {
        int counter = 0;
        for (Entities.BaseRoom r : rooms) {
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
        Entities.BaseRoom room = rooms.get(position);
        ComplexProfileAdapter.RoomItem vh = (ComplexProfileAdapter.RoomItem) holder;
        if (room.getComplex().getMode() == 2 || room.getComplex().getTitle().length() == 0) {
            Entities.Contact contact = DatabaseHelper.getContactByComplexId(room.getComplexId());
            Entities.User user = contact.getPeer();
            vh.nameTV.setText(user.getTitle().split(" ")[0] + " : " + room.getTitle());
            NetworkHelper.loadRoomAvatar(user.getAvatar(), vh.avatarIV);
            DataSyncer.syncBaseUserWithServer(user.getBaseUserId(), new OnBaseUserSyncListener() {
                @Override
                public void userSynced(Entities.BaseUser baseUser) {
                    DataSyncer.syncRoomWithServer(room.getComplexId(), room.getRoomId(), new OnRoomSyncListener() {
                        @Override
                        public void roomSynced(Entities.BaseRoom room) {
                            try {
                                if (!baseUser.getTitle().equals(user.getTitle())) {
                                    user.setTitle(baseUser.getTitle());
                                    vh.nameTV.setText(baseUser.getTitle().split(" ")[0] + " : " + room.getTitle());
                                }
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
                public void roomSynced(Entities.BaseRoom r) {
                    try {
                        if (!(r.getComplex().getTitle().equals(room.getComplex().getTitle())
                                && r.getTitle().equals(room.getTitle()))) {
                            room.setTitle(r.getTitle());
                            vh.nameTV.setText(r.getComplex().getTitle() + " : " + r.getTitle());
                        }
                        NetworkHelper.loadRoomAvatar(r.getAvatar(), vh.avatarIV);
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
            if (room.getComplex().getMode() == 1) {
                if (lastAction.getAuthorId() == myId) {
                    ((RoomItem) holder).stateIV.setImageResource(R.drawable.ic_seen);
                    ((RoomItem) holder).stateIV.setVisibility(View.VISIBLE);
                    ((RoomItem) holder).unreadCount.setVisibility(View.GONE);
                } else {
                    ((RoomItem) holder).unreadCount.setVisibility(View.GONE);
                    ((RoomItem) holder).stateIV.setVisibility(View.GONE);
                }
            } else {
                long unreadCount = DatabaseHelper.getUnreadMessagesCount(room.getRoomId());
                if (unreadCount == 0) {
                    ((RoomItem) holder).unreadCount.setVisibility(View.GONE);
                    if (lastAction.getAuthorId() == myId) {
                        if (lastAction.getSeenCount() > 0) {
                            ((RoomItem) holder).stateIV.setImageResource(R.drawable.ic_seen);
                            ((RoomItem) holder).stateIV.setVisibility(View.VISIBLE);
                        } else {
                            Entities.MessageLocal messageLocal = DatabaseHelper.getMessageLocalById(lastAction.getMessageId());
                            if (messageLocal != null && messageLocal.isSent()) {
                                ((RoomItem) holder).stateIV.setImageResource(R.drawable.ic_done);
                                ((RoomItem) holder).stateIV.setVisibility(View.VISIBLE);
                            } else {
                                ((RoomItem) holder).stateIV.setVisibility(View.GONE);
                            }
                        }
                    } else {
                        ((RoomItem) holder).stateIV.setVisibility(View.GONE);
                    }
                } else {
                    ((RoomItem) holder).unreadCount.setText(unreadCount + "");
                    ((RoomItem) holder).unreadCount.setVisibility(View.VISIBLE);
                    ((RoomItem) holder).stateIV.setVisibility(View.GONE);
                }
            }
        } else {
            vh.lastActionTV.setText("");
        }
        vh.itemView.setOnClickListener(view ->
                activity.startActivity(new Intent(activity, ChatActivity.class)
                        .putExtra("complex_id", room.getComplex().getComplexId())
                        .putExtra("room_id", room.getRoomId())
                        .putExtra("start_file_id", -1L)));
    }

    @Override
    public int getItemCount() {
        return this.rooms.size();
    }

    class RoomItem extends RecyclerView.ViewHolder {

        CircleImageView avatarIV;
        TextView nameTV;
        TextView lastActionTV;
        TextView unreadCount;
        ImageView stateIV;

        RoomItem(View itemView) {
            super(itemView);
            this.avatarIV = itemView.findViewById(R.id.homeRoomItemAvatar);
            this.nameTV = itemView.findViewById(R.id.homeRoomItemName);
            this.lastActionTV = itemView.findViewById(R.id.homeRoomItemLastAction);
            this.unreadCount = itemView.findViewById(R.id.homeRoomItemUnreadCount);
            this.stateIV = itemView.findViewById(R.id.homeRoomItemMyMessageState);
        }
    }
}
