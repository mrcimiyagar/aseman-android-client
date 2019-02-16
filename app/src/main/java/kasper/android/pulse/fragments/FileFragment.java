package kasper.android.pulse.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.FilesAdapter;
import kasper.android.pulse.callbacks.ui.OnDocsLoadedListener;
import kasper.android.pulse.callbacks.ui.OnFileSelectListener;
import kasper.android.pulse.helpers.CallbackHelper;
import kasper.android.pulse.models.extras.Doc;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.tasks.DocsLoadTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class FileFragment extends BaseFragment {

    private String docType;
    private long selectCallbackId;
    private long scrollCallbackId;

    private RecyclerView docsRV;
    private DocsLoadTask docsLoadTask;

    public FileFragment() {

    }

    public static FileFragment instantiate(String docType, long selectCallbackId, long scrollCallbackId) {
        FileFragment fileFragment = new FileFragment();
        Bundle bundle = new Bundle();
        bundle.putString("doc-type", docType);
        bundle.putLong("select-callback", selectCallbackId);
        bundle.putLong("scroll-callback", scrollCallbackId);
        fileFragment.setArguments(bundle);
        return fileFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey("doc-type"))
                docType = getArguments().getString("doc-type");
            if (getArguments().containsKey("select-callback"))
                selectCallbackId = getArguments().getLong("select-callback");
            if (getArguments().containsKey("scroll-callback"))
                scrollCallbackId = getArguments().getLong("scroll-callback");
        }
    }

    @Override
    public void onDestroy() {
        if (docsLoadTask != null) {
            try {
                docsLoadTask.cancel(true);
            } catch (Exception ignored) {

            }
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_doc, container, false);
        docsRV = contentView.findViewById(R.id.fragment_doc_recycler_view);
        final OnFileSelectListener fileSelectListener = (path, docType) ->
                CallbackHelper.invoke(selectCallbackId, 0, path, docType);
        docsRV.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                CallbackHelper.invoke(scrollCallbackId, 1, recyclerView, dx, dy);
            }
        });
        docsRV.setLayoutManager(new GridLayoutManager(getActivity(), 3, RecyclerView.VERTICAL, false));
        final int blockSize = getResources().getDisplayMetrics().widthPixels / 3;
        docsLoadTask = new DocsLoadTask(docType, docs ->
                docsRV.setAdapter(new FilesAdapter(docs, blockSize, fileSelectListener)));
        docsLoadTask.execute();
        return contentView;
    }
}
