package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.text.Spannable;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.anadeainc.rxbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ChatActivity;
import kasper.android.pulse.activities.PostActivity;
import kasper.android.pulse.callbacks.middleware.OnBaseUserSyncListener;
import kasper.android.pulse.callbacks.middleware.OnRoomSyncListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSeen;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.RoomUnreadChanged;
import kasper.android.pulse.rxbus.notifications.RoomsCreated;

public class FeedAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private List<Entities.FeedItem> feedItems;

    public FeedAdapter(AppCompatActivity activity, List<Entities.FeedItem> feedItems) {
        this.activity = activity;
        this.feedItems = feedItems;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0)
            return new PostItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_item, parent, false));
        else
            return new EventItem(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.event_item, parent, false));
    }

    @Override
    public int getItemViewType(int position) {
        Entities.FeedItem feedItem = feedItems.get(position);
        if (feedItem instanceof Entities.Post) {
            return 0;
        } else {
            return 1;
        }
    }

    private static String getDate(long milliSeconds)
    {
        return DateFormat.format("dd/MM/yyyy\nhh:mm a", milliSeconds).toString();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        if (position == 0) {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(params.leftMargin, GraphicHelper.dpToPx(16), params.rightMargin, params.bottomMargin);
            holder.itemView.setLayoutParams(params);
        } else {
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(params.leftMargin, GraphicHelper.dpToPx(8), params.rightMargin, params.bottomMargin);
            holder.itemView.setLayoutParams(params);
        }

        if (viewType == 0) {
            Entities.Post post = (Entities.Post) feedItems.get(position);
            PostItem postItem = (PostItem) holder;
            postItem.postTitle.setText(post.getTitle());
            GlideApp.with(activity).load(post.getImageUrl()).fitCenter().into(postItem.postImage);
            postItem.postTime.setText(getDate(post.getTime()));
            postItem.postAuthorTitle.setText(post.getAuthor().getTitle());
            NetworkHelper.loadUserAvatar(post.getAuthor().getAvatar(), postItem.postAuthorAvatar);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.startActivity(new Intent(activity, PostActivity.class).putExtra("post", post));
                }
            });
        } else {
            Entities.Event event = (Entities.Event) feedItems.get(position);
            EventItem eventItem = (EventItem) holder;
            eventItem.eventTitle.setText(event.getTitle());
            eventItem.eventDesc.setText(event.getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return this.feedItems.size();
    }

    class PostItem extends RecyclerView.ViewHolder {

        private TextView postAuthorTitle;
        private CircleImageView postAuthorAvatar;
        private TextView postTitle;
        private ImageView postImage;
        private TextView postTime;

        PostItem(View itemView) {
            super(itemView);
            this.postAuthorTitle = itemView.findViewById(R.id.postAuthorTitle);
            this.postAuthorAvatar = itemView.findViewById(R.id.postAuthorAvatar);
            this.postTitle = itemView.findViewById(R.id.postTitle);
            this.postImage = itemView.findViewById(R.id.postImage);
            this.postTime = itemView.findViewById(R.id.postTime);

        }
    }

    class EventItem extends RecyclerView.ViewHolder {

        TextView eventTitle;
        TextView eventDesc;

        EventItem(View itemView) {
            super(itemView);
            this.eventTitle = itemView.findViewById(R.id.eventTitle);
            this.eventDesc = itemView.findViewById(R.id.eventDesc);
        }
    }
}
