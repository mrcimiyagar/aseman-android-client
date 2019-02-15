package kasper.android.pulse.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.core.app.ActivityCompat;

import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.PopupMenu;
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
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.AuthHandler;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.rxbus.notifications.ComplexRemoved;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import kasper.android.pulse.rxbus.notifications.UserProfileUpdated;
import kasper.android.pulse.services.FilesService;
import kasper.android.pulse.services.MusicsService;
import kasper.android.pulse.services.NotificationsService;
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

        Entities.User me = DatabaseHelper.getMe();
        if (me != null) Crashlytics.setUserEmail(me.getUserSecret().getEmail());
        initViews();
        initUiData();
        loginToServer();
    }

    @Override
    protected void onDestroy() {
        if (menuComplexesRV.getAdapter() != null)
            ((ComplexesAdapter) menuComplexesRV.getAdapter()).dispose();
        if (menuRoomsRV.getAdapter() != null)
            ((RoomsAdapter) menuRoomsRV.getAdapter()).dispose();
        if (homeRV.getAdapter() != null)
            ((HomeAdapter) homeRV.getAdapter()).dispose();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initViews();
                    initUiData();
                    loginToServer();
                } else {
                    ActivityCompat.requestPermissions(HomeActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            1);
                }
                break;
            }
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

    @SuppressLint("RestrictedApi")
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
        if (complex.getTitle().toLowerCase().equals("home") || complex.getTitle().equals("")) {
            Drawable addDrawable = getResources().getDrawable(R.drawable.ic_add);
            addDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable profileDrawable = getResources().getDrawable(R.drawable.ic_profile);
            profileDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            itemIcons = new Drawable[] {
                    profileDrawable, addDrawable
            };
            itemTitles = new String[] {
                    "View profile", "Add Room"
            };
        } else {
            Drawable profileDrawable = getResources().getDrawable(R.drawable.ic_profile);
            profileDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable addDrawable = getResources().getDrawable(R.drawable.ic_add);
            addDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable deleteDrawable = getResources().getDrawable(R.drawable.ic_delete);
            deleteDrawable.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            itemIcons = new Drawable[] {
                    profileDrawable, addDrawable, deleteDrawable
            };
            itemTitles = new String[] {
                    "Edit Complex", "Add Room", "Delete Room"
            };
        }
        BottomSheet.Builder builder = new BottomSheet.Builder(this);
        builder.setItems(itemTitles, itemIcons,
                (dialogInterface, i) -> {
                    if (i == 0) {
                        startActivity(new Intent(HomeActivity.this, ComplexProfileActivity.class)
                                .putExtra("complex-id", chosenComplexId));
                    } else if (i == 1) {
                        startActivity(new Intent(HomeActivity.this, CreateRoomActivity.class)
                                .putExtra("complex_id", chosenComplexId));
                        drawerLayout.closeDrawer(Gravity.LEFT);
                    } else if (i == 2) {
                        final long complexId = chosenComplexId;
                        showComplexDeleteDialog(complexId);
                    }
                }).setTitle("Complex")
                .setDarkTheme(true)
                .setTitleTextColor(Color.WHITE)
                .setContentType(BottomSheet.LIST)
                .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                .setItemTextColor(Color.WHITE)
                .show();
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
                for (Entities.Room room : complex.getRooms()) {
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
    public void onUiThreadRequested(UiThreadRequested uiThreadRequested) {
        this.runOnUiThread(uiThreadRequested.getRunnable());
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
            public void roomsSynced(List<Entities.Room> rooms) {
                initRoomsAdapter(complex, rooms);
            }
            @Override
            public void syncFailed() { }
        });
    }

    private void initRoomsAdapter(Entities.Complex complex, List<Entities.Room> rooms) {
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

    private void loginToServer() {
        showSnack("Logging in...");
        AuthHandler authHandler = NetworkHelper.getRetrofit().create(AuthHandler.class);
        Call<Packet> call = authHandler.login();
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                Entities.Session session = packet.getSession();
                DatabaseHelper.updateSession(session);
                startServices();
            }

            @Override
            public void onServerFailure() {
                setupSnackAction("Retry Login", view -> loginToServer());
            }

            @Override
            public void onConnectionFailure() {
                setupSnackAction("Retry Login", view -> loginToServer());
            }
        });
    }

    private void startServices() {
        showSnack("Starting services...");
        startService(new Intent(HomeActivity.this, NotificationsService.class));
        startService(new Intent(HomeActivity.this, FilesService.class));
        startService(new Intent(HomeActivity.this, MusicsService.class));
        hideSnack();
    }
}
