package kasper.android.pulse.fragments;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Database;

import com.anadeainc.rxbus.Subscribe;
import com.bumptech.glide.Glide;
import com.google.android.material.appbar.AppBarLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.ProfileActivity;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.adapters.FeedAdapter;
import kasper.android.pulse.adapters.HomeAdapter;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.AppBarStateChangeListener;
import kasper.android.pulse.extras.FeedDecoration;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.extras.RoomTypes;
import kasper.android.pulse.rxbus.notifications.AppBarStateChanged;
import kasper.android.pulse.rxbus.notifications.UserProfileUpdated;

/**
 * A simple {@link Fragment} subclass.
 */
public class DashboardFragment extends BaseFragment {

    private CircleImageView userAvatar;
    private TextView userTitle;

    public DashboardFragment() {

    }

    public static DashboardFragment instantiate() {
        return new DashboardFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    AppBarStateChangeListener.State appBarState = AppBarStateChangeListener.State.EXPANDED;
    ValueAnimator va, va2, va3;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_dashboard, container, false);

        BlurView blurView = contentView.findViewById(R.id.blurView);
        View decorView = getActivity().getWindow().getDecorView();
        ViewGroup rootView = decorView.findViewById(android.R.id.content);
        Drawable windowBackground = decorView.getBackground();
        blurView.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurAlgorithm(new RenderScriptBlur(getActivity()))
                .setBlurRadius(20)
                .setHasFixedTransformationMatrix(false);

        CardView searchCard = contentView.findViewById(R.id.searchCard);
        EditText searchInput = contentView.findViewById(R.id.searchInput);

        AppBarLayout appbar = contentView.findViewById(R.id.appbar);
        appbar.setLiftable(true);
        appbar.addOnOffsetChangedListener(new AppBarStateChangeListener() {
            @Override
            public void onStateChanged(AppBarLayout appBarLayout, State state) {
                if (state == State.EXPANDED) {
                    if (appBarState != state) {
                        if (va != null && va2 != null && va3 != null) {
                            va.cancel();
                            va2.cancel();
                            va3.cancel();
                        }
                        va = ValueAnimator.ofArgb(0, 255);
                        va.addUpdateListener(animation -> searchCard.setCardBackgroundColor(Color.argb(
                                (int) animation.getAnimatedValue(), 255, 255, 255)));
                        va2 = ValueAnimator.ofArgb(255, 0);
                        va2.addUpdateListener(animation -> {
                            searchInput.setHintTextColor(Color.rgb(
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue()));
                            searchInput.setTextColor(Color.rgb(
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue()));
                        });
                        va3 = ValueAnimator.ofInt(20, 1);
                        va3.addUpdateListener(animation -> {
                            blurView.setBlurRadius((int) animation.getAnimatedValue());
                            if (((int) animation.getAnimatedValue()) == 1) {
                                blurView.setVisibility(View.GONE);
                            }
                        });
                        Core.getInstance().bus().post(new AppBarStateChanged(AppBarStateChanged.AppBarState.EXPANDED));
                        va.start();
                        va2.start();
                        va3.start();
                    }
                } else if (state == State.COLLAPSED) {
                    if (appBarState != state) {
                        if (va != null && va2 != null && va3 != null) {
                            va.cancel();
                            va2.cancel();
                            va3.cancel();
                        }
                        va = ValueAnimator.ofArgb(255, 0);
                        va.addUpdateListener(animation -> searchCard.setCardBackgroundColor(Color.argb(
                                (int) animation.getAnimatedValue(), 255, 255, 255)));
                        va2 = ValueAnimator.ofArgb(0, 255);
                        va2.addUpdateListener(animation -> {
                            searchInput.setHintTextColor(Color.rgb(
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue()));
                            searchInput.setTextColor(Color.rgb(
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue(),
                                    (int) animation.getAnimatedValue()));
                        });
                        blurView.setVisibility(View.VISIBLE);
                        va3 = ValueAnimator.ofInt(1, 20);
                        va3.addUpdateListener(animation -> {
                            blurView.setBlurRadius((int) animation.getAnimatedValue());
                        });
                        Core.getInstance().bus().post(new AppBarStateChanged(AppBarStateChanged.AppBarState.COLLAPSED));
                        va.start();
                        va2.start();
                        va3.start();
                    }
                }
                appBarState = state;
            }

            @Override
            public void onOffsetChanged(State state, float offset) {

            }
        });

        ImageView header = contentView.findViewById(R.id.header);
        GlideApp.with(getActivity()).load(R.drawable.city).centerCrop().into(header);

        RecyclerView newsPanel = contentView.findViewById(R.id.newsPanel);
        List<Entities.FeedItem> feedItems = new ArrayList<>();
        Entities.Wall wall = new Entities.Wall();
        wall.setWallId(1);
        wall.setTitle("Keyhans wall");
        wall.setAvatar(0);
        wall.setComplexId(DatabaseHelper.getMe().getUserSecret().getHomeId());
        wall.setComplex(DatabaseHelper.getComplexById(DatabaseHelper.getMe().getUserSecret().getHomeId()));
        wall.setPosts(new ArrayList<>());

        for (int counter = 0; counter < 20; counter++) {
            if (counter % 5 != 0 || counter == 0) {
                Entities.Post post = generatePost(wall, counter);
                wall.getPosts().add(post);
                feedItems.add(post);
            } else {
                feedItems.add(generateEvent(counter));
            }
        }
        newsPanel.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        newsPanel.addItemDecoration(new FeedDecoration());
        newsPanel.setAdapter(new FeedAdapter((AppCompatActivity) getActivity(), feedItems));

        ImageButton homeShortcut = contentView.findViewById(R.id.homeShortcut);
        homeShortcut.setOnClickListener(v -> {
            Entities.Complex c = DatabaseHelper.getMe().getUserSecret().getHome();
            startActivity(new Intent(getActivity(), RoomActivity.class)
                    .putExtra("complex_id", c.getComplexId())
                    .putExtra("room_id", c.getAllRooms().get(0).getRoomId()));
        });

        LinearLayout profileTag = contentView.findViewById(R.id.profileTag);
        profileTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), ProfileActivity.class)
                        .putExtra("user-id", DatabaseHelper.getMe().getBaseUserId()));
            }
        });

        userAvatar = contentView.findViewById(R.id.userAvatar);
        userTitle = contentView.findViewById(R.id.userTitle);

        Entities.User me = DatabaseHelper.getMe();

        if (me != null) {
            NetworkHelper.loadUserAvatar(me.getAvatar(), userAvatar);
            userTitle.setText(me.getTitle().equals("New User") ?
                    me.getTitle() :
                    me.getTitle().split(" ")[0]);
        }

        Core.getInstance().bus().register(this);

        return contentView;
    }

    private Entities.Post generatePost(Entities.Wall wall, long index) {
        Entities.Post post = new Entities.Post();
        post.setPostId(index);
        post.setTitle("New article from teacher");
        post.setImageUrl("https://images.unsplash.com/photo-1479030160180-b1860951d696?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1050&q=80");
        post.setAuthor(DatabaseHelper.getMe());
        post.setAuthorId(post.getAuthor().getBaseUserId());
        post.setTime(System.currentTimeMillis());
        post.setWall(wall);
        post.setWallId(wall.getWallId());
        ArrayList<Entities.PostSection> sections = new ArrayList<>();
        for (int counter = 0; counter < 10; counter++) {
            Entities.PostTextSection textSection = new Entities.PostTextSection();
            textSection.setPostId(post.getPostId());
            textSection.setPost(post);
            textSection.setPostSectionId(index * 30 + counter);
            textSection.setText("Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry's standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.");
            sections.add(textSection);

            Entities.PostImageSection imageSection = new Entities.PostImageSection();
            imageSection.setPostSectionId(index * 30 + counter);
            imageSection.setPostId(post.getPostId());
            imageSection.setPost(post);
            imageSection.setImageUrl("https://images.unsplash.com/photo-1483030096298-4ca126b58199?ixlib=rb-1.2.1&ixid=eyJhcHBfaWQiOjEyMDd9&auto=format&fit=crop&w=1050&q=80");
            sections.add(imageSection);
        }
        post.setSections(sections);
        return post;
    }

    private Entities.Event generateEvent(long index) {
        Entities.Event event = new Entities.Event();
        event.setEventId(index);
        event.setTitle("Achievement");
        event.setDescription("Congratulations Keyhan Mohammadi, you did you works for today and you have 11 hours , 52 minutes free time until your next day work");
        return event;
    }

    @Subscribe
    public void onUserProfileEdited(UserProfileUpdated userProfileUpdated) {
        NetworkHelper.loadUserAvatar(userProfileUpdated.getUser().getAvatar(), userAvatar);
        userTitle.setText(userProfileUpdated.getUser().getTitle());
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }
}
