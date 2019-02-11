package kasper.android.pulse.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Hashtable;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.AudiosAdapter;
import kasper.android.pulse.adapters.PhotosAdapter;
import kasper.android.pulse.adapters.VideosAdapter;
import kasper.android.pulse.callbacks.ui.OnDocSelectListener;
import kasper.android.pulse.helpers.CallbackHelper;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;

/**
 * A simple {@link Fragment} subclass.
 */
public class DocFragment extends Fragment {

    private long roomId;
    private String docType;
    private long selectCallbackId;
    private long scrollCallbackId;

    public static DocFragment instantiate(long roomId, String docType, long selectCallbackId, long scrollCallbackId) {
        DocFragment docFragment = new DocFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("room_id", roomId);
        bundle.putString("doc_type", docType);
        bundle.putLong("select-callback", selectCallbackId);
        bundle.putLong("scroll-callback", scrollCallbackId);
        docFragment.setArguments(bundle);
        return docFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        roomId = getArguments().getLong("room_id");
        docType = getArguments().getString("doc_type");
        selectCallbackId = getArguments().getLong("select-callback");
        scrollCallbackId = getArguments().getLong("scroll-callback");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View contentView = inflater.inflate(R.layout.fragment_doc, container, false);

        OnDocSelectListener selectCallback = new OnDocSelectListener() {
            @Override
            public void docLongClicked(Entities.File file) {
                CallbackHelper.invoke(selectCallbackId, 0, file);
            }
        };

        RecyclerView docsRV = contentView.findViewById(R.id.fragment_doc_recycler_view);

        docsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                CallbackHelper.invoke(scrollCallbackId, 1, recyclerView, dx, dy);
            }
        });

        switch (docType) {
            case "PHOTO": {
                docsRV.setLayoutManager(new GridLayoutManager(getActivity(), 3
                        , RecyclerView.VERTICAL, false));
                Pair<List<Entities.Photo>, Hashtable<Long, Entities.FileLocal>> files =
                        DatabaseHelper.getPhotos(roomId);
                Hashtable<Long, Entities.FileLocal> fileLocals = files.second;
                int blockSize = getResources().getDisplayMetrics().widthPixels / 3;
                docsRV.setAdapter(new PhotosAdapter((AppCompatActivity) getActivity()
                        , files.first, files.second, roomId, blockSize, selectCallback));
                break;
            }
            case "AUDIO": {
                docsRV.setLayoutManager(new LinearLayoutManager(getActivity()
                        , RecyclerView.VERTICAL, false));
                Pair<List<Entities.Audio>, Hashtable<Long, Entities.FileLocal>> files =
                        DatabaseHelper.getAudios(roomId);
                Hashtable<Long, Entities.FileLocal> fileLocals = files.second;
                docsRV.setAdapter(new AudiosAdapter((AppCompatActivity) getActivity()
                        , files.first, files.second, roomId, selectCallback));
                break;
            }
            case "VIDEO": {
                docsRV.setLayoutManager(new GridLayoutManager(getActivity(), 3
                        , RecyclerView.VERTICAL, false));
                Pair<List<Entities.Video>, Hashtable<Long, Entities.FileLocal>> files =
                        DatabaseHelper.getVideos(roomId);
                Hashtable<Long, Entities.FileLocal> fileLocals = files.second;
                int blockSize = getResources().getDisplayMetrics().widthPixels / 3;
                docsRV.setAdapter(new VideosAdapter((AppCompatActivity) getActivity()
                        , files.first, files.second, roomId, blockSize, selectCallback));
                break;
            }
        }

        return contentView;
    }
}
