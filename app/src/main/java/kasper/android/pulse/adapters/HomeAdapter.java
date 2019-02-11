package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ChatActivity;
import kasper.android.pulse.callbacks.ui.ContactListener;
import kasper.android.pulse.callbacks.ui.MessageListener;
import kasper.android.pulse.callbacks.ui.RoomListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;

public class HomeAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private final List<Entities.Room> rooms;
    private MessageListener messageListener;
    private RoomListener roomListener;
    private ContactListener contactListener;
    private Hashtable<Long, Entities.Message> sendingMessages;

    public HomeAdapter(AppCompatActivity activity, List<Entities.Room> rooms) {
        this.activity = activity;
        this.rooms = rooms;
        this.sendingMessages = new Hashtable<>();
        this.notifyDataSetChanged();

        this.messageListener = new MessageListener() {
            @Override
            public void messageReceived(Entities.Message message, Entities.MessageLocal messageLocal) {
                GraphicHelper.runOnUiThread(() -> {
                    int counter = 0;
                    for (Entities.Room room : rooms) {
                        if (message.getRoom().getRoomId() == room.getRoomId()) {
                            room.setLastAction(message);
                            notifyItemChanged(counter + 2);
                            rooms.add(0, room);
                            rooms.remove(counter + 1);
                            notifyItemMoved(counter, 0);
                            break;
                        }
                        counter++;
                    }
                });
            }

            @Override
            public void messageDeleted(Entities.Message message) {

            }

            @Override
            public void messageSending(Entities.Message message, Entities.MessageLocal messageLocal) {
                GraphicHelper.runOnUiThread(() -> {
                    int counter = 0;
                    for (Entities.Room room : rooms) {
                        if (message.getRoom().getRoomId() == room.getRoomId()) {
                            sendingMessages.put(message.getMessageId(), message.clone());
                            room.setLastAction(message);
                            notifyItemChanged(counter + 2);
                            rooms.add(0, room);
                            rooms.remove(counter);
                            notifyItemMoved(counter + 2, 2);
                            break;
                        }
                        counter++;
                    }
                });
            }

            @Override
            public void messageSent(long localMessageId, long onlineMessageId) {
                GraphicHelper.runOnUiThread(() -> {
                    Entities.Message message = sendingMessages.remove(localMessageId);
                    message.setMessageId(onlineMessageId);
                    int counter = 0;
                    for (Entities.Room room : rooms) {
                        if (message.getRoom().getRoomId() == room.getRoomId()) {
                            room.setLastAction(message);
                            notifyItemChanged(counter + 2);
                            rooms.add(0, room);
                            rooms.remove(counter);
                            notifyItemMoved(counter + 2, 2);
                            break;
                        }
                        counter++;
                    }
                });
            }
        };
        GraphicHelper.addMessageListener(this.messageListener);

        roomListener = new RoomListener() {
            @Override
            public void roomCreated(long complexId, Entities.Room room) {
                GraphicHelper.runOnUiThread(() -> addRoom(room));
            }

            @Override
            public void roomsCreated(long complexId, List<Entities.Room> rooms) {
                for (Entities.Room room : rooms) {
                    roomCreated(complexId, room);
                }
            }

            @Override
            public void roomRemoved(Entities.Room room) {
                GraphicHelper.runOnUiThread(() -> removeRoom(room));
            }

            @Override
            public void updateRoomLastMessage(long roomId, Entities.Message message) {

            }
        };
        GraphicHelper.addRoomListener(roomListener);

        contactListener = contact ->
                GraphicHelper.runOnUiThread(() -> {
                    addContact(contact);
                    addRoom(contact.getComplex().getRooms().get(0));
                });
        GraphicHelper.addContactListener(contactListener);
    }

    private void addRoom(Entities.Room room) {
        GraphicHelper.runOnUiThread(() -> {
            boolean exists = false;
            for (Entities.Room r : rooms) {
                if (r.getRoomId() == room.getRoomId()) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                rooms.add(0, room);
                GraphicHelper.runOnUiThread(() -> notifyItemInserted(0));
            }
        });
    }

    private void removeRoom(Entities.Room room) {
        GraphicHelper.runOnUiThread(() -> {
            int counter = 0;
            for (Entities.Room r : rooms) {
                if (r.getRoomId() == room.getRoomId()) {
                    final int index = counter;
                    rooms.remove(index);
                    GraphicHelper.runOnUiThread(
                            () -> notifyItemRemoved(index + 2));
                    break;
                }
                counter++;
            }
        });
    }

    private void addContact(Entities.Contact contact) {
        if (peopleRV != null) {
            ((ActiveNowAdapter) Objects.requireNonNull(peopleRV.getAdapter())).addContact(contact);
        }
    }

    private RecyclerView peopleRV;

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.getAdapterPosition() == 0) {
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
            NetworkHelper.loadRoomAvatar(room.getAvatar(), vh.avatarIV);
            vh.nameTV.setText(room.getComplex().getTitle() + " : " + room.getTitle());
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
