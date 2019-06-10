package kasper.android.pulse.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.crashlytics.android.Crashlytics;
import com.github.javiersantos.bottomdialogs.BottomDialog;

import org.michaelbel.bottomsheet.BottomSheet;

import java.util.List;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ComplexesAdapter;
import kasper.android.pulse.adapters.HomeAdapter;
import kasper.android.pulse.adapters.RoomsAdapter;
import kasper.android.pulse.callbacks.middleware.OnRoomsSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.rxbus.notifications.ComplexRemoved;
import kasper.android.pulse.rxbus.notifications.ConnectionStateChanged;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.UserProfileUpdated;
import kasper.android.pulse.services.AsemanService;
import kasper.android.pulse.services.MusicsService;
import retrofit2.Call;

public class HomeActivity extends BaseActivity {

    DrawerLayout drawerLayout;

    RecyclerView homeRV;

    RecyclerView menuComplexesRV;
    RecyclerView menuRoomsRV;
    TextView complexNameTV;
    CircleImageView myAvatarIV;
    TextView myTitleTV;

    FrameLayout msgView;
    TextView progressView;

    private long chosenComplexId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Core.getInstance().bus().register(this);

        Entities.User me = DatabaseHelper.getMe();
        if (me != null) Crashlytics.setUserEmail(me.getUserSecret().getEmail());
        initViews();
        initUiData();
        startServices();
    }

    @Override
    protected void onDestroy() {
        if (menuComplexesRV.getAdapter() != null)
            ((ComplexesAdapter) menuComplexesRV.getAdapter()).dispose();
        if (menuRoomsRV.getAdapter() != null)
            ((RoomsAdapter) menuRoomsRV.getAdapter()).dispose();
        if (homeRV.getAdapter() != null)
            ((HomeAdapter) homeRV.getAdapter()).dispose();
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (homeRV.getAdapter() != null)
            ((HomeAdapter) homeRV.getAdapter()).softRefresh();
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressLint("RtlHardcoded")
    public void onMenuBtnClicked(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    @SuppressLint("RtlHardcoded")
    public void onSettingsBtnClicked(View view) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onOptionsBtnClicked(View view) {
        showOptionsMenu(R.menu.home_options_menu, view, menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.explore:
                    startActivity(new Intent(HomeActivity.this, SearchActivity.class));
                    return true;
                case R.id.store:
                    startActivity(new Intent(HomeActivity.this, BotStoreActivity.class));
                    return true;
                case R.id.bots:
                    startActivity(new Intent(HomeActivity.this, BotsActivity.class));
                    return true;
                case R.id.invites:
                    startActivity(new Intent(HomeActivity.this, InvitesActivity.class));
                    return true;
                default:
                    return false;
            }
        });
    }

    @SuppressLint("RtlHardcoded")
    public void onMenuMoreMenuBtnClicked(final View view) {
        final Entities.Complex complex = DatabaseHelper.getComplexById(chosenComplexId);
        String[] itemTitles;
        Drawable[] itemIcons;
        Runnable[] itemClicks;

        Drawable profileDrawable = getResources().getDrawable(R.drawable.ic_profile);
        profileDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable addDrawable = getResources().getDrawable(R.drawable.ic_add);
        addDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable deleteDrawable = getResources().getDrawable(R.drawable.ic_delete);
        deleteDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        Runnable viewProfileClick = () ->
                startActivity(new Intent(HomeActivity.this, ComplexProfileActivity.class)
                        .putExtra("complex", DatabaseHelper.getComplexById(chosenComplexId)));

        Runnable addRoomClick = () -> {
            startActivity(new Intent(HomeActivity.this, CreateRoomActivity.class)
                    .putExtra("complex_id", chosenComplexId));
            drawerLayout.closeDrawer(Gravity.LEFT);
        };

        Runnable deleteComplexClick = () -> showComplexDeleteDialog(chosenComplexId);

        if (complex.getMode() == 1) {
            itemIcons = new Drawable[] {
                    profileDrawable, addDrawable
            };
            itemTitles = new String[] {
                    "View Profile", "Add Room"
            };
            itemClicks = new Runnable[] {
                    viewProfileClick, addRoomClick
            };
        } else if (complex.getMode() == 2) {
            itemIcons = new Drawable[] {
                    profileDrawable, addDrawable
            };
            itemTitles = new String[] {
                    "View Profile", "Add Room"
            };
            itemClicks = new Runnable[] {
                    viewProfileClick, addRoomClick
            };
        } else if (complex.getMode() == 3) {
            if (complex.getComplexSecret() != null) {
                itemIcons = new Drawable[] {
                        profileDrawable, addDrawable, deleteDrawable
                };
                itemTitles = new String[] {
                        "View Profile", "Add Room", "Delete Complex"
                };
                itemClicks = new Runnable[] {
                        viewProfileClick, addRoomClick, deleteComplexClick
                };
            } else {
                itemIcons = new Drawable[] {
                        profileDrawable
                };
                itemTitles = new String[] {
                        "View Profile"
                };
                itemClicks = new Runnable[] {
                        viewProfileClick
                };
            }
        } else {
            itemIcons = new Drawable[0];
            itemTitles = new String[0];
            itemClicks = new Runnable[0];
        }

        if (itemClicks.length > 0) {
            BottomSheet.Builder builder = new BottomSheet.Builder(this);
            builder.setItems(itemTitles, itemIcons,
                    (dialogInterface, i) -> itemClicks[i].run()).setTitle("Complex")
                    .setDarkTheme(true)
                    .setTitleTextColor(Color.WHITE)
                    .setContentType(BottomSheet.LIST)
                    .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                    .setItemTextColor(Color.WHITE)
                    .show();
        }
    }

    private void showComplexDeleteDialog(long complexId) {
        new BottomDialog.Builder(this)
                .setTitle("Delete complex")
                .setContent("Do you really want to delete this complex ?")
                .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                .setTitleColor(Color.WHITE)
                .setTextColor(Color.WHITE)
                .setPositiveText("Delete")
                .setPositiveBackgroundColorResource(R.color.colorPrimary)
                .setPositiveBackgroundColor(getResources().getColor(R.color.colorBlue))
                .setPositiveTextColor(Color.WHITE)
                .onPositive(dialog -> deleteComplex(complexId))
                .setNegativeText("Cancel")
                .setNegativeTextColor(Color.WHITE)
                .onNegative(BottomDialog::dismiss)
                .show();
    }

    private void deleteComplex(long complexId) {
        Packet packet = new Packet();
        Entities.Complex complex = DatabaseHelper.getComplexById(complexId);
        packet.setComplex(complex);
        final ComplexHandler complexHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
        Call<Packet> call = complexHandler.deleteComplex(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                DatabaseHelper.notifyComplexRemoved(complexId);
                Core.getInstance().bus().post(new ComplexRemoved(complexId));
                for (Entities.BaseRoom room : complex.getAllRooms()) {
                    Core.getInstance().bus().post(new RoomRemoved(room));
                }
                Entities.User user = DatabaseHelper.getMe();
                if (user != null)
                    notifyComplexChosen(DatabaseHelper.getComplexById(
                            user.getUserSecret().getHomeId()));
            }

            @Override
            public void onServerFailure() {
                Toast.makeText(HomeActivity.this, "Complex delete failure", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionFailure() {
                Toast.makeText(HomeActivity.this, "Complex delete failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        drawerLayout = findViewById(R.id.homeDL);
        homeRV = findViewById(R.id.homeRV);
        menuComplexesRV = findViewById(R.id.menuComplexesRV);
        menuRoomsRV = findViewById(R.id.menuRoomsRV);
        complexNameTV = findViewById(R.id.homeComplexNameTV);
        myAvatarIV = findViewById(R.id.homeMyAvatarIV);
        myTitleTV = findViewById(R.id.homeMyTitleTV);
        msgView = findViewById(R.id.homeMessageView);
        progressView = findViewById(R.id.homeProgressView);
    }

    @Subscribe
    public void onConnectionStateChanged(ConnectionStateChanged connectionStateChanged) {
        switch (connectionStateChanged.getState()) {
            case Connected:
                hideSnack();
                break;
            case Connecting:
                showSnack("Connecting to server", "Dismiss", view -> hideSnack());
                break;
        }
    }

    @Subscribe
    public void onProfileUpdated(UserProfileUpdated profileUpdated) {
        NetworkHelper.loadUserAvatar(profileUpdated.getUser().getAvatar(), myAvatarIV);
        myTitleTV.setText(profileUpdated.getUser().getTitle());
    }

    private void initUiData() {
        homeRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        homeRV.setAdapter(new HomeAdapter(this, DatabaseHelper.getAllRooms()));
        menuComplexesRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        menuRoomsRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        List<Entities.Complex> dbComplexes = DatabaseHelper.getComplexes();
        if (dbComplexes.size() > 0) {
            @SuppressLint("RtlHardcoded")
            ComplexesAdapter complexesAdapter = new ComplexesAdapter(HomeActivity.this
                    , dbComplexes, HomeActivity.this::notifyComplexChosen, () -> {
                drawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(new Intent(HomeActivity.this, CreateComplexActivity.class));
            });
            menuComplexesRV.setAdapter(complexesAdapter);
            notifyComplexChosen(dbComplexes.get(0));
        }

        Entities.User user = DatabaseHelper.getMe();
        if (user != null) {
            NetworkHelper.loadUserAvatar(user.getAvatar(), myAvatarIV);
            myTitleTV.setText(user.getTitle());
        }
    }

    private void notifyComplexChosen(final Entities.Complex complex) {
        chosenComplexId = complex.getComplexId();
        if (complex.getTitle() != null &&  complex.getTitle().length() > 0) {
            complexNameTV.setText(complex.getTitle());
        } else {
            Entities.User user = DatabaseHelper.getHumanById(DatabaseHelper
                    .getContactByComplexId(complex.getComplexId()).getPeerId());
            complexNameTV.setText(user.getTitle());
        }
        initRoomsAdapter(complex, DatabaseHelper.getRooms(complex.getComplexId()));
        DataSyncer.syncRoomsWithServer(complex.getComplexId(), new OnRoomsSyncListener() {
            @Override
            public void roomsSynced(List<Entities.BaseRoom> rooms) {
                if (complex.getComplexId() == chosenComplexId)
                    initRoomsAdapter(complex, rooms);
            }
            @Override
            public void syncFailed() { }
        });
    }

    private void initRoomsAdapter(Entities.Complex complex, List<Entities.BaseRoom> rooms) {
        if (menuRoomsRV.getAdapter() != null)
            ((RoomsAdapter) menuRoomsRV.getAdapter()).dispose();
        @SuppressLint("RtlHardcoded")
        RoomsAdapter roomsAdapter = new RoomsAdapter(HomeActivity.this
                , complex.getComplexId()
                , rooms
                , room -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            startActivity(new Intent(HomeActivity.this
                    , RoomActivity.class)
                    .putExtra("complex_id", complex.getComplexId())
                    .putExtra("room_id", room.getRoomId()));
        });
        menuRoomsRV.setAdapter(roomsAdapter);
    }

    private void startServices() {
        showSnack("Starting services...");
        new Thread(() -> {
            Intent intent = new Intent(HomeActivity.this, AsemanService.class);
            startService(intent);
        }).start();
        new Thread(() -> {
            Intent intent = new Intent(HomeActivity.this, MusicsService.class);
            startService(intent);
        }).start();
        hideSnack();
    }
}
