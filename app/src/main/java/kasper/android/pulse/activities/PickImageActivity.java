package kasper.android.pulse.activities;

import android.Manifest;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.FilesAdapter;
import kasper.android.pulse.callbacks.ui.OnFileSelectListener;
import kasper.android.pulse.tasks.DocsLoadTask;

public class PickImageActivity extends AppCompatActivity {

    private ImageButton backBTN;
    private RecyclerView photosRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_image);

        initViews();
        initListeners();
        initDecorations();
        initList();
    }

    private void initViews() {
        backBTN = findViewById(R.id.page_pick_photo_back_button);
        photosRV = findViewById(R.id.page_pick_photo_recycler_view);
    }

    private void initListeners() {
        backBTN.setOnClickListener(v -> onBackPressed());
    }

    private void initDecorations() {
        photosRV.setLayoutManager(new GridLayoutManager(this, 3
                , RecyclerView.VERTICAL, false));
    }

    private void initList() {
        final OnFileSelectListener fileSelectListener = (path, docType) -> {
            setResult(RESULT_OK, new Intent()
                    .putExtra("path", path));
            finish();
        };
        final int blockSize = getResources().getDisplayMetrics().widthPixels / 3;

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            new DocsLoadTask("PHOTO", docs ->
                                    photosRV.setAdapter(new FilesAdapter(docs, blockSize, fileSelectListener))).execute();
                        } else {
                            finish();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }
}
