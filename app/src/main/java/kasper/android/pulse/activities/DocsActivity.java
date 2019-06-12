package kasper.android.pulse.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.ImageButton;

import com.google.android.material.tabs.TabLayout;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.FragmentsAdapter;
import kasper.android.pulse.callbacks.ui.OnDocSelectListener;
import kasper.android.pulse.fragments.BaseFragment;
import kasper.android.pulse.fragments.DocFragment;
import kasper.android.pulse.helpers.CallbackHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.services.AsemanService;

public class DocsActivity extends BaseActivity {

    private long complexId;
    private long roomId;

    ViewPager docsVP;
    CardView buttonsContainer;
    TabLayout docsTL;
    ImageButton backBTN;

    private boolean btnsContainerOpened = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_docs);

        if (getIntent().getExtras() != null) {
            complexId = getIntent().getExtras().getLong("complex_id");
            roomId = getIntent().getExtras().getLong("room_id");
        }

        docsVP = findViewById(R.id.fragment_docs_fragment_container);
        buttonsContainer = findViewById(R.id.fragment_docs_buttons_container);
        docsTL = findViewById(R.id.fragment_docs_tab_bar);
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
        final long scrollCallbackId = CallbackHelper.register(scrollListener);
        OnDocSelectListener selectListener = file ->
                startActivity(new Intent(DocsActivity.this, ChatActivity.class)
                        .putExtra("complexId", complexId)
                        .putExtra("room_id", roomId)
                        .putExtra("start_file_id", file.getFileId()));
        final long id = CallbackHelper.register(selectListener);

        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            List<BaseFragment> fragments = new ArrayList<>();
                            fragments.add(DocFragment.instantiate(roomId, "PHOTO", id, scrollCallbackId));
                            fragments.add(DocFragment.instantiate(roomId, "AUDIO", id, scrollCallbackId));
                            fragments.add(DocFragment.instantiate(roomId, "VIDEO", id, scrollCallbackId));
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
                        } else
                            finish();
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
