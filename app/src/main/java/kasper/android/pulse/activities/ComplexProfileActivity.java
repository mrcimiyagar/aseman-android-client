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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.michaelbel.bottomsheet.BottomSheet;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ComplexProfileAdapter;
import kasper.android.pulse.callbacks.middleware.OnComplexSyncListener;
import kasper.android.pulse.callbacks.middleware.OnRoomsSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.LinearDecoration;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.ComplexProfileUpdating;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.rxbus.notifications.ComplexProfileUpdated;
import kasper.android.pulse.rxbus.notifications.MembershipCreated;
import kasper.android.pulse.services.AsemanService;
import retrofit2.Call;

public class ComplexProfileActivity extends BaseActivity {

    private Entities.Complex complex;

    private ImageView avatarIV;
    private TextView titleTV;
    private TextView memberCountTV;
    private RecyclerView roomsRV;

    private long myId;
    private long memberCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_profile);
        FloatingActionButton editFAB = findViewById(R.id.editFAB);
        avatarIV = findViewById(R.id.complexProfileAvatarIV);
        titleTV = findViewById(R.id.complexProfileTitleTV);
        memberCountTV = findViewById(R.id.complexProfileSubTitleTV);
        roomsRV = findViewById(R.id.complexProfileRoomsRV);

        Core.getInstance().bus().register(this);

        Entities.User me = DatabaseHelper.getMe();
        if (me != null) myId = me.getBaseUserId();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getIntent().getExtras() != null)
            complex = (Entities.Complex) getIntent().getExtras().getSerializable("complex");

        ImageButton optionsMenuBtn = findViewById(R.id.optionsMenuBtn);
        if (complex.getMode() == 1 || complex.getMode() == 2 || complex.getComplexSecret() == null)
            optionsMenuBtn.setVisibility(View.GONE);

        roomsRV.setLayoutManager(new LinearLayoutManager(ComplexProfileActivity.this
                , RecyclerView.VERTICAL, false));
        roomsRV.addItemDecoration(new LinearDecoration(GraphicHelper.dpToPx(16), GraphicHelper.dpToPx(88)));

        if (complex.getComplexSecret() == null || complex.getMode() < 3)
            editFAB.setVisibility(View.GONE);

        fillContent(complex);
        DataSyncer.syncComplexWithServer(complex.getComplexId(), new OnComplexSyncListener() {
            @Override
            public void complexSynced(Entities.Complex complex) {
                ComplexProfileActivity.this.complex.setTitle(complex.getTitle());
                ComplexProfileActivity.this.complex.setAvatar(complex.getAvatar());
                fillContent(complex);
            }
            @Override
            public void syncFailed() { }
        });
        fillRooms(DatabaseHelper.getRooms(complex.getComplexId()));
        DataSyncer.syncRoomsWithServer(complex.getComplexId(), new OnRoomsSyncListener() {
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
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @SuppressLint("SetTextI18n")
    private void fillContent(Entities.Complex complex) {
        if (complex.getMode() == 2) {
            Entities.Contact contact = DatabaseHelper.getContactByComplexId(complex.getComplexId());
            Entities.User user = contact.getPeer();
            titleTV.setText(user.getTitle());
            NetworkHelper.loadUserAvatar(user.getAvatar(), avatarIV);
        } else {
            titleTV.setText(complex.getTitle());
            NetworkHelper.loadComplexAvatar(complex.getAvatar(), avatarIV);
        }
        memberCount = DatabaseHelper.getMembersCount(complex.getComplexId());
        memberCountTV.setText(memberCount + " " + (memberCount == 1 ? "member" : "members"));
    }

    public void fillRooms(List<Entities.Room> rooms) {
        roomsRV.setAdapter(new ComplexProfileAdapter(this, myId, rooms));
    }

    public void onOptionsMenuBtnClicked(View view) {
        showOptionsMenu(R.menu.complex_profile_options_menu, view, item -> {
            switch (item.getItemId()) {
                case R.id.invites:
                    startActivity(new Intent(ComplexProfileActivity.this, ComplexInvitesActivity.class)
                            .putExtra("complex_id", complex.getComplexId()));
                    return true;
            }
            return false;
        });
    }

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123) {
            if (resultCode == RESULT_OK) {
                if (data.getExtras() != null) {
                    String path = data.getExtras().getString("path");
                    AsemanService.updateComplexProfileAvatar(new ComplexProfileUpdating(path, complex));
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
        Drawable editDrawable = getResources().getDrawable(R.drawable.ic_edit);
        editDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable viewDrawable = getResources().getDrawable(R.drawable.ic_view);
        viewDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable deleteDrawable = getResources().getDrawable(R.drawable.ic_delete);
        deleteDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        Runnable setNewPhotoRunnable = () -> startActivityForResult(new Intent(ComplexProfileActivity.this, PickImageActivity.class), 123);

        Runnable deletePhotoRunnable = () -> new BottomDialog.Builder(ComplexProfileActivity.this)
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

        Runnable viewPhotoRunnable = () -> {
            Pair<View, String> picture = Pair.create(avatarIV, "photo");
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(ComplexProfileActivity.this, picture);
            Intent intent = new Intent(ComplexProfileActivity.this, PhotoViewerActivity.class);
            if (complex.getAvatar() > 0) {
                intent.putExtra("fileId", complex.getAvatar());
                ComplexProfileActivity.this.startActivity(intent, options.toBundle());
            }
        };

        Drawable[] itemIcons;
        String[] itemTitles;
        Runnable[] itemClicks;

        if (!complex.getTitle().toLowerCase().equals("home") &&
                DatabaseHelper.getComplexSecretByComplexId(complex.getComplexId()) != null) {
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
                itemClicks = new Runnable[] {
                        setNewPhotoRunnable,
                        deletePhotoRunnable,
                        viewPhotoRunnable
                };
            } else {
                itemIcons = new Drawable[]{
                        editDrawable
                };
                itemTitles = new String[]{
                        "Set new photo"
                };
                itemClicks = new Runnable[] {
                        setNewPhotoRunnable
                };
            }
        } else {
            if (complex.getAvatar() > 0) {
                itemIcons = new Drawable[]{
                        viewDrawable
                };
                itemTitles = new String[]{
                        "View photo"
                };
                itemClicks = new Runnable[] {
                        viewPhotoRunnable
                };
            } else {
                itemIcons = new Drawable[0];
                itemTitles = new String[0];
                itemClicks = new Runnable[0];
            }
        }

        if (itemClicks.length > 0) {
            BottomSheet.Builder builder = new BottomSheet.Builder(this);
            builder.setItems(itemTitles, itemIcons,
                    (dialogInterface, i) -> itemClicks[i].run())
                    .setTitle("Profile photo")
                    .setDarkTheme(true)
                    .setTitleTextColor(Color.WHITE)
                    .setContentType(BottomSheet.LIST)
                    .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                    .setItemTextColor(Color.WHITE)
                    .show();
        }
    }

    @Subscribe
    public void onComplexProfileUpdated(ComplexProfileUpdated updated) {
        complex.setAvatar(updated.getComplex().getAvatar());
        NetworkHelper.loadComplexAvatar(complex.getAvatar(), avatarIV);
    }

    @SuppressLint("SetTextI18n")
    @Subscribe
    public void onMembershipCreated(MembershipCreated membershipCreated) {
        if (membershipCreated.getMembership().getComplex().getComplexId() == complex.getComplexId()) {
            memberCount++;
            memberCountTV.setText(memberCount + " " + (memberCount == 1 ? "member" : "members"));
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
