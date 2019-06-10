package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;
import com.microsoft.signalr.JsonHelper;

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
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSeen;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.RoomUnreadChanged;
import kasper.android.pulse.rxbus.notifications.RoomsCreated;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private final List<Entities.BaseRoom> rooms;
    private Hashtable<Long, Entities.Message> sendingMessages;
    private RecyclerView peopleRV;
    private long myId;

    public HomeAdapter(AppCompatActivity activity, List<Entities.BaseRoom> rooms) {
        this.activity = activity;
        this.rooms = rooms;
        this.sendingMessages = new Hashtable<>();
        Entities.User user = DatabaseHelper.getMe();
        if (user != null) this.myId = user.getBaseUserId();
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        if (peopleRV != null && peopleRV.getAdapter() != null)
            ((ActiveNowAdapter) peopleRV.getAdapter()).dispose();
        Core.getInstance().bus().unregister(this);
    }

    public void softRefresh() {
        for (int counter = 2; counter < getItemCount(); counter++) {
            notifyItemChanged(counter);
        }
    }

    @Subscribe
    public void onMessageReceived(MessageReceived messageReceived) {
        if (messageReceived.isBottom()) {
            Entities.Message message = messageReceived.getMessage();
            int counter = 0;
            for (Entities.BaseRoom room : rooms) {
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
    }

    @Subscribe
    public void onMessageSending(MessageSending messageSending) {
        Entities.Message message = messageSending.getMessage();
        int counter = 0;
        for (Entities.BaseRoom room : rooms) {
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
        if (message != null) {
            message.setMessageId(onlineMessageId);
            int counter = 0;
            for (Entities.BaseRoom room : rooms) {
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
    }

    @Subscribe
    public void onRoomCreated(RoomCreated roomCreated) {
        addRoom(roomCreated.getRoom());
    }

    @Subscribe
    public void onRoomsCreated(RoomsCreated roomsCreated) {
        for (Entities.BaseRoom room : rooms) {
            addRoom(room);
        }
    }

    @Subscribe
    public void onRoomRemoved(RoomRemoved roomRemoved) {
        removeRoom(roomRemoved.getRoom());
    }

    @Subscribe
    public void onRoomUnreadChanged(RoomUnreadChanged unreadChanged) {
        int counter = 0;
        for (Entities.BaseRoom room : rooms) {
            if (room.getRoomId() == unreadChanged.getRoomId()) {
                notifyItemChanged(counter + 2);
                break;
            }
            counter++;
        }
    }

    @Subscribe
    public void onMessageSeen(MessageSeen messageSeen) {
        int counter = 0;
        for (Entities.BaseRoom room : rooms) {
            if (room.getRoomId() == messageSeen.getMessage().getRoom().getRoomId()) {
                if (room.getLastAction() != null) {
                    room.getLastAction().setSeenCount(messageSeen.getMessage().getSeenCount());
                    notifyItemChanged(counter + 2);
                }
                break;
            }
            counter++;
        }
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
            notifyItemInserted(2);
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
            Entities.BaseRoom room = rooms.get(position - 2);
            RoomItem vh = (RoomItem) holder;
            if (room.getComplex().getMode() == 2) {
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
                if (room instanceof Entities.SingleRoom) {
                    Entities.SingleRoom sr = (Entities.SingleRoom) room;
                    final Entities.User peer;
                    if (sr.getUser1().getBaseUserId() == myId) {
                        peer = DatabaseHelper.getHumanById(sr.getUser2Id());
                    }
                    else if (sr.getUser2().getBaseUserId() == myId) {
                        peer = DatabaseHelper.getHumanById(sr.getUser1Id());
                    }
                    else {
                        peer = null;
                    }
                    if (peer != null) {
                        vh.nameTV.setText(sr.getComplex().getTitle() + " : " + peer.getTitle().split(" ")[0]);
                        NetworkHelper.loadRoomAvatar(peer.getAvatar(), vh.avatarIV);
                        DataSyncer.syncBaseUserWithServer(peer.getBaseUserId(), new OnBaseUserSyncListener() {
                            @Override
                            public void userSynced(Entities.BaseUser baseUser) {
                                DataSyncer.syncRoomWithServer(room.getComplexId(), room.getRoomId(), new OnRoomSyncListener() {
                                    @Override
                                    public void roomSynced(Entities.BaseRoom room) {
                                        try {
                                            if (!baseUser.getTitle().equals(peer.getTitle())) {
                                                peer.setTitle(baseUser.getTitle());
                                                vh.nameTV.setText(sr.getComplex().getTitle() + " : " + peer.getTitle().split(" ")[0]);
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
                    }
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
                            } catch (Exception ignored) {
                            }
                        }

                        @Override
                        public void syncFailed() {
                        }
                    });
                }
            }
            Entities.Message lastAction = room.getLastAction();
            if (lastAction != null) {
                if (lastAction instanceof Entities.TextMessage) {
                    if (lastAction.getAuthor() != null)
                        vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : "
                                + ((Entities.TextMessage) lastAction).getText());
                    else
                        vh.lastActionTV.setText("");
                } else if (lastAction instanceof Entities.PhotoMessage) {
                    if (lastAction.getAuthor() != null)
                        vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : " + "Photo");
                    else
                        vh.lastActionTV.setText("");
                } else if (lastAction instanceof Entities.AudioMessage) {
                    if (lastAction.getAuthor() != null)
                        vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : " + "Audio");
                    else
                        vh.lastActionTV.setText("");
                } else if (lastAction instanceof Entities.VideoMessage) {
                    if (lastAction.getAuthor() != null)
                        vh.lastActionTV.setText(lastAction.getAuthor().getTitle().split(" ")[0] + " : " + "Video");
                    else
                        vh.lastActionTV.setText("");
                } else if (lastAction instanceof Entities.ServiceMessage) {
                    vh.lastActionTV.setText("Aseman : " + ((Entities.ServiceMessage) lastAction).getText());
                }

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
