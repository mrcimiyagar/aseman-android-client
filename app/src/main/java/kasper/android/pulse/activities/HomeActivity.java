package kasper.android.pulse.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.crashlytics.android.Crashlytics;
import com.github.javiersantos.bottomdialogs.BottomDialog;

import org.michaelbel.bottomsheet.BottomSheet;

import java.util.ArrayList;
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
import kasper.android.pulse.retrofit.AuthHandler;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.ContactHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ComplexRemoved;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
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

        if (me != null)
            Crashlytics.setUserEmail(me.getUserSecret().getEmail());

        if (ContextCompat.checkSelfPermission(HomeActivity.this
                , Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HomeActivity.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        } else {
            initViews();
            initUiData();
            loginToServer();
        }
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

    public void onBotsBtnClicked(View view) {
        startActivity(new Intent(this, BotsActivity.class));
    }

    public void onStoreBtnClicked(View view) {
        startActivity(new Intent(this, BotStoreActivity.class));
    }

    @SuppressLint("RtlHardcoded")
    public void onSettingsBtnClicked(View view) {
        drawerLayout.closeDrawer(Gravity.LEFT);
        startActivity(new Intent(this, SettingsActivity.class));
    }

    public void onGlobalSearchBtnClicked(View view) {
        startActivity(new Intent(this, SearchActivity.class));
    }

    @SuppressLint("RtlHardcoded")
    public void onMoreMenuBtnClicked(final View view) {
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

    private void showComplexDeleteDialog(long compleXId) {
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
                .onPositive(dialog -> deleteComplex(compleXId))
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
        @SuppressLint("RtlHardcoded")
        RoomsAdapter roomsAdapter = new RoomsAdapter(HomeActivity.this
                , complex.getComplexId()
                , DatabaseHelper.getRooms(complex.getComplexId())
                , room -> {
                    drawerLayout.closeDrawer(Gravity.LEFT);
                    startActivity(new Intent(HomeActivity.this
                            , RoomActivity.class)
                            .putExtra("complex_id", complex.getComplexId())
                            .putExtra("room_id", room.getRoomId()));
                });
        menuRoomsRV.setAdapter(roomsAdapter);
        DataSyncer.syncRoomsWithServer(complex.getComplexId(), new OnRoomsSyncListener() {
            @Override
            public void roomsSynced(List<Entities.Room> rooms) {
                @SuppressLint("RtlHardcoded")
                RoomsAdapter roomsAdapter = new RoomsAdapter(HomeActivity.this
                        , complex.getComplexId(), rooms, room -> {
                            drawerLayout.closeDrawer(Gravity.LEFT);
                            startActivity(new Intent(HomeActivity.this
                                    , RoomActivity.class)
                                    .putExtra("complex_id", complex.getComplexId())
                                    .putExtra("room_id", room.getRoomId()));
                        });
                menuRoomsRV.setAdapter(roomsAdapter);
            }
            @Override
            public void syncFailed() {
                Toast.makeText(HomeActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private int doneTasksCount = 0;
    private final Object TASKS_LOCK = new Object();
    private List<Entities.Contact> syncedContacts = new ArrayList<>();
    private List<Entities.Complex> syncedComplexes = new ArrayList<>();
    private List<Entities.Room> syncedRooms = new ArrayList<>();
    private List<Entities.Bot> syncedBotCreationsBots = new ArrayList<>();
    private List<Entities.BotCreation> syncedBotCreations = new ArrayList<>();
    private List<Entities.Bot> syncedBotSubscriptionsBots = new ArrayList<>();
    private List<Entities.BotSubscription> syncedBotSubscriptions = new ArrayList<>();

    private void loginToServer() {
        getStatusSnackbar().show();
        AuthHandler authHandler = NetworkHelper.getRetrofit().create(AuthHandler.class);
        Call<Packet> call = authHandler.login();
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                Entities.Session session = packet.getSession();
                DatabaseHelper.updateSession(session);
                getStatusSnackbar().dismiss();
                startSyncing();
            }

            @Override
            public void onServerFailure() {
                getStatusSnackbar().setAction("Retry Login", view -> loginToServer());
                getStatusSnackbar().setActionTextColor(getResources().getColor(R.color.colorBlue));
            }

            @Override
            public void onConnectionFailure() {
                getStatusSnackbar().setAction("Retry Login", view -> loginToServer());
                getStatusSnackbar().setActionTextColor(getResources().getColor(R.color.colorBlue));
            }
        });
    }

    private void startSyncing() {
        getStatusSnackbar().setText("Syncing data ...");
        getStatusSnackbar().show();
        initContacts();
        initComplexes();
        initRooms();
        initBots();
    }

    private void initContacts() {
        ContactHandler contactHandler = NetworkHelper.getRetrofit().create(ContactHandler.class);
        Call<Packet> contactsCall = contactHandler.getContacts();
        NetworkHelper.requestServer(contactsCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                syncedContacts = packet.getContacts();
                getStatusSnackbar().dismiss();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onServerFailure() {
                syncedContacts = new ArrayList<>();
                getStatusSnackbar().dismiss();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onConnectionFailure() {
                getStatusSnackbar().dismiss();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }
        });
    }

    private void initComplexes() {
        ComplexHandler complexHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
        Call<Packet> contactsCall = complexHandler.getComplexes();
        NetworkHelper.requestServer(contactsCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                syncedComplexes = packet.getComplexes();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onServerFailure() {
                syncedContacts = new ArrayList<>();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onConnectionFailure() {
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }
        });
    }

    private void initRooms() {
        RoomHandler roomHandler = NetworkHelper.getRetrofit().create(RoomHandler.class);
        Packet packet = new Packet();
        Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(0);
        packet.setComplex(complex);
        Call<Packet> call = roomHandler.getRooms(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                syncedRooms = packet.getRooms();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onServerFailure() {
                syncedRooms = new ArrayList<>();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onConnectionFailure() {
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }
        });
    }

    private void initBots() {
        Call<Packet> creationCall = NetworkHelper.getRetrofit().create(RobotHandler.class).getCreatedBots();
        NetworkHelper.requestServer(creationCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                syncedBotCreationsBots = packet.getBots();
                syncedBotCreations = packet.getBotCreations();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onServerFailure() {
                syncedBotCreationsBots = new ArrayList<>();
                syncedBotCreations = new ArrayList<>();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onConnectionFailure() {
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }
        });
        Call<Packet> subscriptionCall = NetworkHelper.getRetrofit().create(RobotHandler.class).getSubscribedBots();
        NetworkHelper.requestServer(subscriptionCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                syncedBotSubscriptionsBots = packet.getBots();
                syncedBotSubscriptions = packet.getBotSubscriptions();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onServerFailure() {
                syncedBotSubscriptionsBots = new ArrayList<>();
                syncedBotSubscriptions = new ArrayList<>();
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }

            @Override
            public void onConnectionFailure() {
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }
        });
    }

    private void notifyTaskDone() {
        synchronized (TASKS_LOCK) {
            doneTasksCount++;
            if (doneTasksCount == 4) {
                for (Entities.Contact contact : syncedContacts) {
                    boolean result = DatabaseHelper.notifyContactCreated(contact);
                    if (result) {
                        Core.getInstance().bus().post(new ContactCreated(contact));
                    }
                }
                for (Entities.Complex complex : syncedComplexes) {
                    DatabaseHelper.notifyComplexCreated(complex);
                    Core.getInstance().bus().post(new ComplexCreated(complex));
                }
                for (Entities.Room room : syncedRooms) {
                    DatabaseHelper.notifyRoomCreated(room);
                    Core.getInstance().bus().post(new RoomCreated(room.getComplexId(), room));
                }
                for (int counter = 0; counter < syncedBotCreationsBots.size(); counter++) {
                    DatabaseHelper.notifyBotCreated(syncedBotCreationsBots.get(counter)
                            , syncedBotCreations.get(counter));
                }
                for (int counter = 0; counter < syncedBotSubscriptionsBots.size(); counter++) {
                    DatabaseHelper.notifyBotSubscribed(syncedBotSubscriptionsBots.get(counter)
                            , syncedBotSubscriptions.get(counter));
                }

                Core.getInstance().bus().post(new UiThreadRequested(() -> {
                    getStatusSnackbar().dismiss();
                    getStatusSnackbar().setText("Starting services ...");
                    getStatusSnackbar().show();
                    startServices();
                }));
            }
        }
    }

    private void startServices() {
        startService(new Intent(HomeActivity.this, NotificationsService.class));
        startService(new Intent(HomeActivity.this, FilesService.class));
        startService(new Intent(HomeActivity.this, MusicsService.class));
        getStatusSnackbar().dismiss();
    }
}
