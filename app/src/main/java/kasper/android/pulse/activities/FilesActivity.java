package kasper.android.pulse.activities;

import android.Manifest;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.FragmentsAdapter;
import kasper.android.pulse.callbacks.ui.OnFileSelectListener;
import kasper.android.pulse.fragments.BaseFragment;
import kasper.android.pulse.fragments.FileFragment;
import kasper.android.pulse.helpers.CallbackHelper;
import kasper.android.pulse.helpers.GraphicHelper;

public class FilesActivity extends BaseActivity {

    private long selectCallbackId;

    ViewPager docsVP;
    TabLayout docsTL;
    CardView buttonsContainer;
    ImageButton backBTN;

    private boolean btnsContainerOpened = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            init();
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

    private void init() {
        if (getIntent().getExtras() != null)
            selectCallbackId = getIntent().getExtras().getLong("select-callback");

        docsVP = findViewById(R.id.fragment_docs_fragment_container);
        docsTL = findViewById(R.id.fragment_docs_tab_bar);
        buttonsContainer = findViewById(R.id.fragment_docs_buttons_container);
        backBTN = findViewById(R.id.fragment_files_back_image_button);

        docsVP.setOffscreenPageLimit(3);

        backBTN.setOnClickListener(v -> onBackPressed());

        RecyclerView.OnScrollListener scrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && btnsContainerOpened) {
                    btnsContainerOpened = false;
                    buttonsContainer.animate().y(GraphicHelper.getScreenHeight() + GraphicHelper
                            .dpToPx(16)).setDuration(350).start();
                } else if (dy < 0 && !btnsContainerOpened) {
                    btnsContainerOpened = true;
                    buttonsContainer.animate().y(GraphicHelper.getScreenHeight() - GraphicHelper
                            .dpToPx(72 + 24)).setDuration(350).start();
                }
            }
        };
        long scrollCallbackId = CallbackHelper.register(scrollListener);
        OnFileSelectListener selectListener = (path, docType) -> {
            CallbackHelper.invoke(selectCallbackId, 0, path, docType);
            finish();
        };
        long selectCallback = CallbackHelper.register(selectListener);
        List<BaseFragment> fragments = new ArrayList<>();
        fragments.add(FileFragment.instantiate("PHOTO", selectCallback, scrollCallbackId));
        fragments.add(FileFragment.instantiate("AUDIO", selectCallback, scrollCallbackId));
        fragments.add(FileFragment.instantiate("VIDEO", selectCallback, scrollCallbackId));
        docsVP.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), fragments));
        docsTL.setupWithViewPager(docsVP);

        TabLayout.Tab tab0 = docsTL.getTabAt(0);
        if (tab0 != null) tab0.setIcon(R.drawable.ic_photo);

        TabLayout.Tab tab1 = docsTL.getTabAt(1);
        if (tab1 != null) tab1.setIcon(R.drawable.ic_audio);

        TabLayout.Tab tab2 = docsTL.getTabAt(2);
        if (tab2 != null) tab2.setIcon(R.drawable.ic_video);

        for (int counter = 0; counter < docsTL.getTabCount(); counter++) {
            TabLayout.Tab tab = docsTL.getTabAt(counter);
            if (tab != null && tab.getIcon() != null)
                DrawableCompat.setTint(tab.getIcon(), Color.WHITE);
        }
    }
}
