package kasper.android.pulse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.FeedAdapter;
import kasper.android.pulse.adapters.HomeAdapter;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.RoomTypes;

/**
 * A simple {@link Fragment} subclass.
 */
public class FeedFragment extends BaseFragment {

    private RecyclerView feedRV;
    private long complexId;

    public FeedFragment() {

    }

    public static FeedFragment instantiate(long complexId) {
        FeedFragment fileFragment = new FeedFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("complex-id", complexId);
        fileFragment.setArguments(bundle);
        return fileFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey("complex-id"))
                complexId = getArguments().getLong("complex-id");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_doc, container, false);
        feedRV = contentView.findViewById(R.id.fragment_doc_recycler_view);
        feedRV.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));

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

        feedRV.setAdapter(new FeedAdapter((AppCompatActivity) getActivity(), feedItems));
        return contentView;
    }

    private Entities.Post generatePost(Entities.Wall wall, long index) {
        Entities.Post post = new Entities.Post();
        post.setPostId(index);
        post.setTitle("Look at this amazing zebra");
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

    public void dispose() {
        if (feedRV != null && feedRV.getAdapter() != null)
            ((HomeAdapter) feedRV.getAdapter()).dispose();
    }
}
