package kasper.android.pulse.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;

import android.os.PersistableBundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.crashlytics.android.Crashlytics;

import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.michaelbel.bottomsheet.BottomSheet;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ActiveNowAdapter;
import kasper.android.pulse.adapters.ComplexesAdapter;
import kasper.android.pulse.adapters.FragmentsAdapter;
import kasper.android.pulse.adapters.RoomsAdapter;
import kasper.android.pulse.callbacks.middleware.OnRoomsSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.fragments.BaseFragment;
import kasper.android.pulse.fragments.FeedFragment;
import kasper.android.pulse.fragments.RoomsFragment;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.RoomTypes;
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
    CoordinatorLayout parent;

    ViewPager homeVP;
    SmartTabLayout homeTB;
    RecyclerView peopleRV;

    RecyclerView menuComplexesRV;
    RecyclerView menuRoomsRV;
    TextView complexNameTV;
    CircleImageView myAvatarIV;
    TextView myTitleTV;

    FrameLayout msgView;
    TextView progressView;

    private long chosenComplexId;

    MapView mapView;
    MapboxMap mapboxMap;

    private LocationEngine locationEngine;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;
    private HomeActivityLocationCallback callback = new HomeActivityLocationCallback(this);

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getResources().getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_home);

        this.thisIsHome();

        Core.getInstance().bus().register(this);

        Entities.User me = DatabaseHelper.getMe();
        if (me != null) Crashlytics.setUserEmail(me.getUserSecret().getEmail());
        initViews();
        mapView = findViewById(R.id.mapView);
        initUiData();
        startServices();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                HomeActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(new Style.Builder().fromUrl("mapbox://styles/theprogrammermachine/cjwqcofhy05c41cqnc6wl1ech")
                        , new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(mapboxMap, style);
                    }
                });
            }
        });

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_MOVE:
                        homeVP.requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        homeVP.requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return mapView.onTouchEvent(event);
            }
        });
    }

    @SuppressWarnings( {"MissingPermission"})
    public void enableLocationComponent(MapboxMap mapboxMap, Style style) {
        if (PermissionsManager.areLocationPermissionsGranted(HomeActivity.this)) {
            LocationComponent locationComponent = mapboxMap.getLocationComponent();
            LocationComponentActivationOptions locationComponentActivationOptions =
                    LocationComponentActivationOptions.builder(this, style)
                            .useDefaultLocationEngine(false)
                            .build();
            locationComponent.activateLocationComponent(locationComponentActivationOptions);
            locationComponent.setLocationComponentEnabled(true);
            locationComponent.setCameraMode(CameraMode.TRACKING);
            locationComponent.setRenderMode(RenderMode.COMPASS);
            initLocationEngine();
        } else {
            permissionsManager = new PermissionsManager(new PermissionsListener() {
                @Override
                public void onExplanationNeeded(List<String> permissionsToExplain) {

                }
                @Override
                public void onPermissionResult(boolean granted) {
                    if (granted) {
                        enableLocationComponent(mapboxMap, style);
                    }
                }
            });
            permissionsManager.requestLocationPermissions(HomeActivity.this);
        }
    }

    @SuppressLint("MissingPermission")
    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        locationEngine.getLastLocation(callback);
    }

    private static class HomeActivityLocationCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<HomeActivity> activityWeakReference;

        HomeActivityLocationCallback(HomeActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
        }
        @Override
        public void onSuccess(LocationEngineResult result) {
            HomeActivity activity = activityWeakReference.get();
            if (activity != null) {
                Location location = result.getLastLocation();
                if (location == null) return;
                if (activity.mapboxMap != null && result.getLastLocation() != null) {
                    activity.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());
                }
            }
        }
        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        if (menuComplexesRV.getAdapter() != null)
            ((ComplexesAdapter) menuComplexesRV.getAdapter()).dispose();
        if (menuRoomsRV.getAdapter() != null)
            ((RoomsAdapter) menuRoomsRV.getAdapter()).dispose();
        if (homeVP.getAdapter() != null)
            ((FragmentsAdapter) homeVP.getAdapter()).dispose();
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(callback);
        }
        mapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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
        parent = findViewById(R.id.parent);
        drawerLayout = findViewById(R.id.homeDL);
        homeVP = findViewById(R.id.homeVP);
        homeTB = findViewById(R.id.homeTB);
        peopleRV = findViewById(R.id.peopleRV);
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

    TextView[] tabs = new TextView[3];
    String[] tabTitles = new String[3];
    private int chosenTab = 0;
    SmartTabLayout.TabProvider tabProvider = new SmartTabLayout.TabProvider() {
        @Override
        public View createTabView(ViewGroup container, int position, PagerAdapter adapter) {
            Log.d("KasperLogger", "tab " + position);
            TextView textView = new TextView(HomeActivity.this);
            LinearLayout.LayoutParams paranms = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            textView.setLayoutParams(paranms);
            textView.setText(tabTitles[position]);
            if (position == homeVP.getCurrentItem()) {
                textView.setTextColor(getResources().getColor(R.color.colorBlackBlue));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            } else {
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            }
            textView.setTypeface(null, Typeface.BOLD);
            textView.setPadding(GraphicHelper.dpToPx(16), 0, GraphicHelper.dpToPx(16), 0);
            textView.setGravity(Gravity.CENTER);
            tabs[position] = textView;
            return textView;
        }
    };

    private void initUiData() {
        List<BaseFragment> pages = new ArrayList<>();
        pages.add(FeedFragment.instantiate(chosenComplexId));
        pages.add(RoomsFragment.instantiate(chosenComplexId, RoomTypes.Private));
        pages.add(RoomsFragment.instantiate(chosenComplexId, RoomTypes.Contact));
        homeVP.setOffscreenPageLimit(3);
        homeVP.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), pages));
        chosenTab = 0;
        tabs = new TextView[3];
        tabTitles = new String[] {
                "What's going on", "Home Rooms", "Contact Rooms"
        };
        homeTB.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                tabs[chosenTab].setTextColor(Color.WHITE);
                tabs[chosenTab].setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
                chosenTab = position;
                tabs[chosenTab].setTextColor(getResources().getColor(R.color.colorBlackBlue));
                tabs[chosenTab].setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        homeTB.setCustomTabView(tabProvider);
        homeTB.setViewPager(homeVP);

        peopleRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        List<Entities.Contact> contacts = DatabaseHelper.getContacts();
        List<Entities.User> users = new ArrayList<>();
        users.add(DatabaseHelper.getMe());
        for (Entities.Contact contact : contacts) users.add(contact.getPeer());
        peopleRV.setAdapter(new ActiveNowAdapter(this, users));
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
        List<BaseFragment> pages = new ArrayList<>();
        if (chosenComplexId == DatabaseHelper.getMe().getUserSecret().getHomeId()) {
            pages.add(FeedFragment.instantiate(chosenComplexId));
            pages.add(RoomsFragment.instantiate(chosenComplexId, RoomTypes.Private));
            pages.add(RoomsFragment.instantiate(chosenComplexId, RoomTypes.Contact));
            homeVP.setOffscreenPageLimit(3);
            homeVP.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), pages));
            chosenTab = 0;
            tabs = new TextView[3];
            tabTitles = new String[] {
                    "What's going on", "Home Rooms", "Contac Rooms"
            };
            homeTB.setViewPager(homeVP);
        } else {
            pages.add(FeedFragment.instantiate(chosenComplexId));
            pages.add(RoomsFragment.instantiate(chosenComplexId, RoomTypes.Group));
            pages.add(RoomsFragment.instantiate(chosenComplexId, RoomTypes.Single));
            homeVP.setOffscreenPageLimit(3);
            homeVP.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), pages));
            chosenTab = 0;
            tabs = new TextView[3];
            tabTitles = new String[] {
                    "What's going on", "Complex Groups", "Complex Privateds"
            };
            homeTB.setViewPager(homeVP);
        }
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
