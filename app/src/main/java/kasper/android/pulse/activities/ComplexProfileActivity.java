package kasper.android.pulse.activities;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.michaelbel.bottomsheet.BottomSheet;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ComplexProfileAdapter;
import kasper.android.pulse.callbacks.middleware.OnComplexSyncListener;
import kasper.android.pulse.callbacks.middleware.OnRoomsSyncListener;
import kasper.android.pulse.callbacks.network.OnFileUploadListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.LinearDecoration;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.rxbus.notifications.ComplexProfileUpdated;
import retrofit2.Call;

public class ComplexProfileActivity extends AppCompatActivity {

    private long complexId;
    private Entities.Complex complex;

    private ImageView avatarIV;
    private TextView titleTV;
    private TextView memberCountTV;
    private RecyclerView roomsRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_profile);
        FloatingActionButton editFAB = findViewById(R.id.editFAB);
        avatarIV = findViewById(R.id.complexProfileAvatarIV);
        titleTV = findViewById(R.id.complexProfileTitleTV);
        memberCountTV = findViewById(R.id.complexProfileSubTitleTV);
        roomsRV = findViewById(R.id.complexProfileRoomsRV);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getIntent().getExtras() != null)
            complexId = getIntent().getExtras().getLong("complex-id");
        roomsRV.setLayoutManager(new LinearLayoutManager(ComplexProfileActivity.this
                , RecyclerView.VERTICAL, false));
        roomsRV.addItemDecoration(new LinearDecoration(GraphicHelper.dpToPx(16), GraphicHelper.dpToPx(88)));

        complex = DatabaseHelper.getComplexById(complexId);

        if (complex.getComplexSecret() != null && !complex.getTitle().toLowerCase().equals("home"))
            editFAB.show();
        else
            editFAB.hide();

        fillContent(complex);
        DataSyncer.syncComplexWithServer(complexId, new OnComplexSyncListener() {
            @Override
            public void complexSynced(Entities.Complex complex) {
                ComplexProfileActivity.this.complex.setTitle(complex.getTitle());
                ComplexProfileActivity.this.complex.setAvatar(complex.getAvatar());
                fillContent(complex);
            }
            @Override
            public void syncFailed() { }
        });
        fillRooms(DatabaseHelper.getRooms(complexId));
        DataSyncer.syncRoomsWithServer(complexId, new OnRoomsSyncListener() {
            @Override
            public void roomsSynced(List<Entities.Room> rooms) {
                fillRooms(rooms);
            }

            @Override
            public void syncFailed() { }
        });
    }

    @Override
    protected void onDestroy() {
        if (roomsRV.getAdapter() != null)
            ((ComplexProfileAdapter) roomsRV.getAdapter()).dispose();
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void fillContent(Entities.Complex complex) {
        titleTV.setText(complex.getTitle());
        long memCount = DatabaseHelper.getMembersCount(complexId);
        memberCountTV.setText(memCount + " " + (memCount == 1 ? "member" : "members"));
        NetworkHelper.loadComplexAvatar(complex.getAvatar(), avatarIV);
    }

    public void fillRooms(List<Entities.Room> rooms) {
        roomsRV.setAdapter(new ComplexProfileAdapter(this, rooms));
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras() != null) {
                    String path = data.getExtras().getString("path");
                    Pair<Entities.File, Entities.FileLocal> pair = DatabaseHelper.notifyPhotoUploading(
                            true, path, 256, 256);
                    Entities.File file = pair.first;
                    NetworkHelper.uploadFile(file, -1, -1, true, path
                            , progress -> {

                            }, (OnFileUploadListener) (fileId, fileUsageId) -> {
                                if (complex != null) {
                                    complex.setAvatar(fileId);
                                    updateProfile();
                                }
                            });
                }
            }
        } else if (requestCode == 456) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras() != null) {
                    String title = data.getExtras().getString("title");
                    if (title != null && complex != null) {
                        complex.setTitle(title);
                        updateProfile();
                    }
                }
            }
        }
    }

    public void onEditTitleBtnClicked(View view) {
        startActivityForResult(new Intent(this, TitleEditorActivity.class)
                .putExtra("title", complex.getTitle()), 456);
    }

    public void onAvatarImageClicked(View view) {
        if (!complex.getTitle().toLowerCase().equals("home")) {
            Drawable editDrawable = getResources().getDrawable(R.drawable.ic_edit);
            editDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable viewDrawable = getResources().getDrawable(R.drawable.ic_view);
            viewDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable deleteDrawable = getResources().getDrawable(R.drawable.ic_delete);
            deleteDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

            Drawable[] itemIcons;
            String[] itemTitles;

            if (complex != null) {
                if (complex.getAvatar() > 0) {
                    itemIcons = new Drawable[]{
                            editDrawable,
                            deleteDrawable,
                            viewDrawable
                    };
                    itemTitles = new String[]{
                            "Set new photo",
                            "Delete photo",
                            "View photo"
                    };
                } else {
                    itemIcons = new Drawable[]{
                            editDrawable
                    };
                    itemTitles = new String[]{
                            "Set new photo"
                    };
                }
                BottomSheet.Builder builder = new BottomSheet.Builder(this);
                builder.setItems(itemTitles, itemIcons,
                        (dialogInterface, i) -> {
                            if (i == 0) {
                                startActivityForResult(new Intent(this, PickImageActivity.class), 123);
                            } else if (i == 1) {
                                new BottomDialog.Builder(this)
                                        .setTitle("Delete complex")
                                        .setContent("Do you really want to delete profile photo ?")
                                        .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                                        .setTitleColor(Color.WHITE)
                                        .setTextColor(Color.WHITE)
                                        .setPositiveText("Delete")
                                        .setPositiveBackgroundColorResource(R.color.colorPrimary)
                                        .setPositiveBackgroundColor(getResources().getColor(R.color.colorBlue))
                                        .setPositiveTextColor(Color.WHITE)
                                        .onPositive(dialog -> {
                                            complex.setAvatar(-1L);
                                            updateProfile();
                                        })
                                        .setNegativeText("Cancel")
                                        .setNegativeTextColor(Color.WHITE)
                                        .onNegative(BottomDialog::dismiss)
                                        .show();
                            } else if (i == 2) {
                                Pair<View, String> picture = Pair.create(avatarIV, "photo");
                                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ComplexProfileActivity.this, picture);
                                Intent intent = new Intent(ComplexProfileActivity.this, PhotoViewerActivity.class);
                                if (complex.getAvatar() > 0) {
                                    intent.putExtra("fileId", complex.getAvatar());
                                    ComplexProfileActivity.this.startActivity(intent, options.toBundle());
                                }
                            }
                        })
                        .setTitle("Profile photo")
                        .setDarkTheme(true)
                        .setTitleTextColor(Color.WHITE)
                        .setContentType(BottomSheet.LIST)
                        .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                        .setItemTextColor(Color.WHITE)
                        .show();
            }
        }
    }

    private void updateProfile() {
        Packet packet = new Packet();
        if (complex != null) {
            packet.setComplex(complex);
            ComplexHandler profileHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
            Call<Packet> call = profileHandler.updateComplexProfile(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    DatabaseHelper.notifyComplexCreated(complex);
                    titleTV.setText(complex.getTitle());
                    NetworkHelper.loadUserAvatar(complex.getAvatar(), avatarIV);
                    Core.getInstance().bus().post(new ComplexProfileUpdated(complex));
                }

                @Override
                public void onServerFailure() {
                    Toast.makeText(ComplexProfileActivity.this, "Profile update failure", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onConnectionFailure() {
                    Toast.makeText(ComplexProfileActivity.this, "Profile update failure", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
