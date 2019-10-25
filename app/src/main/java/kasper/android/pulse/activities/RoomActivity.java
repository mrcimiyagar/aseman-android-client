package kasper.android.pulse.activities;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.ViewCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ogaclejapan.smarttablayout.SmartTabLayout;

import org.michaelbel.bottomsheet.BottomSheet;

import java.security.cert.PolicyNode;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TransferQueue;

import de.hdodenhof.circleimageview.CircleImageView;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ActiveNowAdapter;
import kasper.android.pulse.adapters.BotPickerAdapter;
import kasper.android.pulse.adapters.ComplexesAdapter;
import kasper.android.pulse.adapters.FragmentsAdapter;
import kasper.android.pulse.adapters.RoomsAdapter;
import kasper.android.pulse.callbacks.middleware.OnRoomsSyncListener;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.callbacks.ui.OnRoomIconClickListener;
import kasper.android.pulse.components.LockableNestedScrollView;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.MyDragShadowBuilder;
import kasper.android.pulse.fragments.BaseFragment;
import kasper.android.pulse.fragments.FeedFragment;
import kasper.android.pulse.fragments.RoomsFragment;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.middleware.DataSyncer;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.extras.RoomTypes;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.PulseHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.rxbus.notifications.BotPicked;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ComplexRemoved;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.RoomSelected;
import kasper.android.pulse.rxbus.notifications.WorkerAdded;
import kasper.android.pulse.rxbus.notifications.WorkerRemoved;
import kasper.android.pulse.rxbus.notifications.WorkerUpdated;
import kasper.android.pulseframework.components.PulseView;
import retrofit2.Call;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class RoomActivity extends BaseActivity {

    private long complexId;
    private long roomId;
    private long myId;
    private boolean afterChat;

    private CardView dock;
    private TextView roomTitleTV;

    RecyclerView menuComplexesRV;
    RecyclerView menuRoomsRV;
    TextView complexNameTV;
    CircleImageView myAvatarIV;
    TextView myTitleTV;

    private CircleImageView roomAvatarIV;
    private ImageView backgroundView;
    private RelativeLayout widgetContainer;
    private ImageButton msgFAB;
    private ImageButton docsFAB;
    private ImageView botsFAB;
    private FloatingActionButton addBotFAB;
    private CardView closeEditBtn;
    private FrameLayout botPickerShadow;
    private LinearLayout botPicker;
    private RecyclerView botPickerRV;
    private LockableNestedScrollView scrollView;
    private RelativeLayout roomContainer;
    private CardView controlPage;
    private RelativeLayout dockContainer;
    private FrameLayout shadowBox;
    private DrawerLayout drawerLayout;
    ViewPager homeVP;
    SmartTabLayout homeTB;
    LinearLayout dockFirstStage;
    LinearLayout dockSecondStage;
    CardView searchCloseContainer;

    private long chosenComplexId;

    private FrameLayout dragPanel;

    private List<Entities.Workership> workerships;
    private List<Entities.Bot> bots;
    private Hashtable<Long, PulseView> pulseTable;

    private Entities.MemberAccess memberAccess;
    private boolean dragMode = false;
    private boolean transferringWidget = false;
    private PulseView draggingPulseView = null;

    private WidgetLangingLocation difference = null;
    private WidgetLangingLocation startPoint = null;
    private LinkedBlockingQueue<WidgetLangingLocation> widgetLandingQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<WidgetLangingLocation> widgetInnerLandingQueue = new LinkedBlockingQueue<>();
    private List<Entities.Workership> addedWorkerships = new ArrayList<>();
    private List<Entities.Workership> editedWorkerships = new ArrayList<>();
    private Entities.Bot pickedBot = null, editedBot = null;

    private class WidgetLangingLocation {
        private float x;
        private float y;

        public WidgetLangingLocation(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }
    }

    @Subscribe
    public void onRoomSelected(RoomSelected roomSelected) {

        closeControlPage();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (PulseView pv : pulseTable.values())
                    pv.animate().alpha(0).scaleX(0.25f).scaleY(0.25f).setDuration(650).start();

                workerships = new ArrayList<>();
                bots = new ArrayList<>();
                pulseTable = new Hashtable<>();

                complexId = roomSelected.getRoom().getComplexId();
                roomId = roomSelected.getRoom().getRoomId();
                PulseHelper.setCurrentComplexId(complexId);
                PulseHelper.setCurrentRoomId(roomId);

                Entities.User me = DatabaseHelper.getMe();
                if (me != null)
                    myId = me.getBaseUserId();

                memberAccess = DatabaseHelper.getMemberAccessByComplexAndUserId(complexId, myId);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        load();
                        initSettings();
                    }
                }, 650);
            }
        }, 450);
    }

    List<Entities.Complex> complexes;
    List<LinearLayout> tabs;
    private int chosenTab = 0;
    SmartTabLayout.TabProvider tabProvider;

    @Subscribe
    public void onComplexCreated(ComplexCreated complexCreated) {
        complexes.add(complexCreated.getComplex());
        notifyComplexChosen(complexCreated.getComplex());
    }

    private void initUiData() {

        menuComplexesRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        menuRoomsRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));

        complexes = DatabaseHelper.getComplexes();
        if (complexes.size() > 0) {
            List<BaseFragment> pages = new ArrayList<>();
            for (int counter = 0; counter < complexes.size(); counter++)
                pages.add(RoomsFragment.instantiate(complexes.get(counter).getComplexId(), RoomTypes.All));
            homeVP.setOffscreenPageLimit(3);
            homeVP.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), pages));
            chosenTab = 0;
            tabs = new ArrayList<>(complexes.size());
            tabProvider = (container, position, adapter) -> {
                LinearLayout layout = new LinearLayout(RoomActivity.this);
                layout.setLayoutParams(new ViewGroup.LayoutParams(GraphicHelper.dpToPx(112), GraphicHelper.dpToPx(64)));
                layout.setOrientation(LinearLayout.VERTICAL);
                layout.setGravity(Gravity.CENTER);
                CircleImageView imageView = new CircleImageView(RoomActivity.this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        GraphicHelper.dpToPx(32),
                        GraphicHelper.dpToPx(32));
                imageView.setLayoutParams(params);
                NetworkHelper.loadComplexAvatar(complexes.get(position).getAvatar(), imageView);
                layout.addView(imageView);
                TextView textView = new TextView(RoomActivity.this);
                textView.setText(complexes.get(position).getTitle());
                textView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
                textView.setGravity(Gravity.CENTER);
                textView.setTextColor(Color.WHITE);
                layout.addView(textView);
                tabs.add(layout);
                return layout;
            };

            homeTB.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                }
                @Override
                public void onPageSelected(int position) {
                    chosenTab = position;
                }
                @Override
                public void onPageScrollStateChanged(int state) {

                }
            });
            homeTB.setCustomTabView(tabProvider);
            homeTB.setViewPager(homeVP);

            @SuppressLint("RtlHardcoded")
            ComplexesAdapter complexesAdapter = new ComplexesAdapter(RoomActivity.this
                    , complexes, RoomActivity.this::notifyComplexChosen, () -> {
                drawerLayout.closeDrawer(Gravity.LEFT);
                startActivity(new Intent(RoomActivity.this, CreateComplexActivity.class));
            });
            menuComplexesRV.setAdapter(complexesAdapter);
            notifyComplexChosen(complexes.get(0));
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
            for (int counter = 0; counter < complexes.size(); counter++)
                pages.add(RoomsFragment.instantiate(complexes.get(counter).getComplexId(), RoomTypes.All));
            homeVP.setOffscreenPageLimit(3);
            homeVP.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), pages));
            chosenTab = 0;
            homeTB.setViewPager(homeVP);
        } else {
            for (int counter = 0; counter < complexes.size(); counter++)
                pages.add(RoomsFragment.instantiate(complexes.get(counter).getComplexId(), RoomTypes.All));
            homeVP.setOffscreenPageLimit(3);
            homeVP.setAdapter(new FragmentsAdapter(getSupportFragmentManager(), pages));
            chosenTab = 0;
            homeTB.setViewPager(homeVP);
        }
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
                Toast.makeText(RoomActivity.this, "Complex delete failure", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionFailure() {
                Toast.makeText(RoomActivity.this, "Complex delete failure", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getSliderInterface().lock();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        Core.getInstance().bus().register(this);

        workerships = new ArrayList<>();
        bots = new ArrayList<>();
        pulseTable = new Hashtable<>();

        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey("complex_id"))
                complexId = getIntent().getExtras().getLong("complex_id");
            if (getIntent().getExtras().containsKey("room_id"))
                roomId = getIntent().getExtras().getLong("room_id");
            if (getIntent().getExtras().containsKey("after_chat"))
                afterChat = getIntent().getExtras().getBoolean("after_chat");
        }

        PulseHelper.setCurrentComplexId(complexId);
        PulseHelper.setCurrentRoomId(roomId);

        Entities.User me = DatabaseHelper.getMe();
        if (me != null)
            myId = me.getBaseUserId();

        memberAccess = DatabaseHelper.getMemberAccessByComplexAndUserId(complexId, myId);

        load();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (shadowBox != null && shadowBox.getAlpha() > 0)
            shadowBox.animate().alpha(0).setDuration(350).start();
        initSettings();
        PulseHelper.setCurrentComplexId(complexId);
        PulseHelper.setCurrentRoomId(roomId);
        PulseHelper.setPulseViewTable(pulseTable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PulseHelper.setCurrentComplexId(-1);
        PulseHelper.setCurrentRoomId(-1);
        for (Entities.Bot bot : bots)
            PulseHelper.getPulseViewTable().remove(bot.getBaseUserId());
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    private void load() {
        initViews();

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) controlPage.getLayoutParams();
        if (GraphicHelper.getScreenWidth() > GraphicHelper.dpToPx(700)) {
            params.width = GraphicHelper.dpToPx(700);
            params.addRule(RelativeLayout.CENTER_HORIZONTAL);
            controlPage.setLayoutParams(params);
            controlPage.setRadius(GraphicHelper.dpToPx(32));
        } else {
            params.width = MATCH_PARENT;
            controlPage.setLayoutParams(params);
            controlPage.setRadius(0);
        }

        widgetContainer.removeAllViews();
        backgroundView.setImageResource(0);
        initListeners();
        loadBots();
    }

    @Subscribe
    public void onWorkerAdded(WorkerAdded workerAdded) {
        Entities.Workership workership = workerAdded.getWorkership();
        workerships.add(workership);
        insertNewPulseView(workership);
    }

    @Subscribe
    public void onWorkerUpdated(WorkerUpdated workerUpdated) {
        Entities.Workership workership = workerUpdated.getWorkership();
        for (int counter = 0; counter < workerships.size(); counter++) {
            if (workership.getBotId() == workerships.get(counter).getBotId()) {
                workerships.set(counter, workership);
                break;
            }
        }
        updateExistingPulseViewDimensions(workership);
    }

    @Subscribe
    public void onWorkerRemoved(WorkerRemoved workerRemoved) {
        Entities.Workership workership = workerRemoved.getWorkership();
        for (int counter = 0; counter < workerships.size(); counter++) {
            if (workership.getBotId() == workerships.get(counter).getBotId()) {
                workerships.remove(counter);
                break;
            }
        }
        removeExistingPulseView(workership);
    }

    public void onBotsBtnClicked(View v) {
        if (memberAccess.isCanModifyWorkers()) {
            botsFAB.setOnClickListener(view -> {
                    if (dock.getY() == dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100))
                        startActivityForResult(new Intent(RoomActivity.this, AddBotToRoomActivity.class)
                                        .putExtra("complex_id", complexId)
                                        .putExtra("room_id", roomId)
                                        .putExtra("existing_bots", workerships
                                                .toArray(new Entities.Workership[0]))
                            , 1);
            });
        } else {
            botsFAB.setOnClickListener(view -> {});
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    public void onMenuBtnClicked(View view) {
        if (dock.getY() == dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100)) {
            Drawable messageDrw = getResources().getDrawable(R.drawable.ic_message);
            messageDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable fileDrw = getResources().getDrawable(R.drawable.ic_folder);
            fileDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable settingsDrw = getResources().getDrawable(R.drawable.ic_settings);
            settingsDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            Drawable editDrw = getResources().getDrawable(R.drawable.ic_edit);
            editDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

            String[] itemTitles = new String[]{
                    "Messages", "Files", "Settings", "Edit Bots"
            };
            Drawable[] itemIcons = new Drawable[]{
                    messageDrw, fileDrw, settingsDrw, editDrw
            };

            BottomSheet.Builder builder = new BottomSheet.Builder(this);
            builder.setItems(itemTitles, itemIcons,
                    (dialogInterface, i) -> {
                        if (i == 0) {
                            shadowBox.animate().alpha(1).setDuration(350).start();
                            startActivity(new Intent(RoomActivity.this, ChatActivity.class)
                                    .putExtra("complex_id", complexId)
                                    .putExtra("room_id", roomId)
                                    .putExtra("start_file_id", -1L)
                                    .putExtra("after_room", true));
                        } else if (i == 1) {
                            startActivity(new Intent(RoomActivity.this, DocsActivity.class)
                                    .putExtra("complex_id", complexId)
                                    .putExtra("room_id", roomId));
                        } else if (i == 2) {
                            startActivity(new Intent(RoomActivity.this, EditRoomDesktopActivity.class)
                                    .putExtra("room-id", roomId));
                        } else if (i == 3) {
                            ValueAnimator valAnim = ValueAnimator.ofInt(GraphicHelper.dpToPx(0), GraphicHelper.dpToPx(115));
                            valAnim.addUpdateListener(animation -> {
                                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dock.getLayoutParams();
                                lp.topMargin = (int) animation.getAnimatedValue();
                                dock.setLayoutParams(lp);
                            });
                            valAnim.setDuration(300);
                            ValueAnimator valAnim2 = ValueAnimator.ofInt(GraphicHelper.dpToPx(-96), GraphicHelper.dpToPx(16));
                            valAnim2.addUpdateListener(animation -> {
                                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) addBotFAB.getLayoutParams();
                                lp.bottomMargin = (int) animation.getAnimatedValue();
                                addBotFAB.setLayoutParams(lp);
                            });
                            valAnim2.setDuration(550);
                            ValueAnimator valAnim3 = ValueAnimator.ofInt(GraphicHelper.dpToPx(-96), GraphicHelper.dpToPx(-28));
                            valAnim3.addUpdateListener(animation -> {
                                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) closeEditBtn.getLayoutParams();
                                lp.leftMargin = (int) animation.getAnimatedValue();
                                closeEditBtn.setLayoutParams(lp);
                            });
                            valAnim3.setDuration(425);
                            valAnim.start();
                            valAnim2.start();
                            valAnim3.start();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    addBotFAB.show();
                                }
                            }, 450);
                            for (Map.Entry<Long, PulseView> pair : pulseTable.entrySet()) {
                                Long botId = pair.getKey();
                                PulseView pulseView = pair.getValue();
                                pulseView.disableChildrenTouch();
                                pulseView.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View v) {
                                        Entities.Bot b = new Entities.Bot();
                                        b.setBaseUserId(botId);
                                        editedBot = b;
                                        pickedBot = null;
                                        ClipData.Item item = new ClipData.Item(pulseView.getTag().toString());
                                        ClipData dragData = new ClipData(
                                                pulseView.getTag().toString(),
                                                new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                                                item);
                                        View.DragShadowBuilder myShadow = new MyDragShadowBuilder(pulseView, true);
                                        pulseView.startDrag(dragData,
                                                myShadow,
                                                pulseView,
                                                0
                                        );
                                        return true;
                                    }
                                });

                                scrollView.setScrollingEnabled(false);

                                pulseView.setOnTouchListener(new View.OnTouchListener() {

                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {

                                        if (event.getPointerCount() == 2) {
                                            MotionEvent.PointerCoords coords1 = new MotionEvent.PointerCoords();
                                            event.getPointerCoords(0, coords1);
                                            MotionEvent.PointerCoords coords2 = new MotionEvent.PointerCoords();
                                            event.getPointerCoords(1, coords2);

                                            double dx = Math.abs(coords1.x - coords2.x);
                                            double dy = Math.abs(coords1.y - coords2.y);

                                            if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                                                resizeStartDx = dx;
                                                resizeStartDy = dy;
                                                resizeStartWidth = v.getMeasuredWidth();
                                                resizeStartHeight = v.getMeasuredHeight();
                                            } else if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_MASK) {
                                                double sizeX = v.getMeasuredWidth() + (dx - resizeStartDx);
                                                double sizeY = v.getMeasuredHeight() + (dy + resizeStartDy);
                                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
                                                if (sizeX > GraphicHelper.dpToPx(56))
                                                    params.width = (int)sizeX;
                                                if (sizeY > GraphicHelper.dpToPx(56))
                                                    params.height = (int)sizeY;

                                                Toast.makeText(RoomActivity.this, params.width + " " + params.height, Toast.LENGTH_SHORT).show();

                                                v.setLayoutParams(params);
                                                v.requestLayout();
                                            }
                                        }

                                        return true;
                                    }
                                });
                            }
                        }
                    }).setTitle("GoTo...")
                    .setDarkTheme(true)
                    .setTitleTextColor(Color.WHITE)
                    .setContentType(BottomSheet.LIST)
                    .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                    .setItemTextColor(Color.WHITE)
                    .show();
        }
    }

    private int resizeStartWidth = 0, resizeStartHeight = 0;
    private double resizeStartDx = 0, resizeStartDy = 0;

    private void initViews() {
        dock = findViewById(R.id.dock);
        roomTitleTV = findViewById(R.id.roomTitle);
        roomAvatarIV = findViewById(R.id.roomAvatar);
        backgroundView = findViewById(R.id.roomBackground);
        widgetContainer = findViewById(R.id.fragment_room_widget_container);
        addBotFAB = findViewById(R.id.addBotFAB);
        closeEditBtn = findViewById(R.id.closeEditBtn);
        botPickerShadow = findViewById(R.id.botPickerShadow);
        botPicker = findViewById(R.id.botPicker);
        botPickerRV = findViewById(R.id.botPickerRV);
        dragPanel = findViewById(R.id.dragPanel);
        scrollView = findViewById(R.id.scrollView);
        botsFAB = findViewById(R.id.roomBotsFAB);
        roomContainer = findViewById(R.id.activity_room_container);
        controlPage = findViewById(R.id.controlPage);
        dockContainer = findViewById(R.id.dockContainer);
        shadowBox = findViewById(R.id.shadowBox);
        drawerLayout = findViewById(R.id.drawerLayout);
        menuComplexesRV = findViewById(R.id.menuComplexesRV);
        menuRoomsRV = findViewById(R.id.menuRoomsRV);
        complexNameTV = findViewById(R.id.homeComplexNameTV);
        myAvatarIV = findViewById(R.id.homeMyAvatarIV);
        myTitleTV = findViewById(R.id.homeMyTitleTV);
        homeVP = findViewById(R.id.homeVP);
        homeTB = findViewById(R.id.homeTB);
        dockFirstStage = findViewById(R.id.dockFirstStage);
        dockSecondStage = findViewById(R.id.dockSecondStage);
        searchCloseContainer = findViewById(R.id.searchCloseContainer);
    }

    public void onStoreBtnClicked(View view) {
        startActivity(new Intent(this, BotStoreActivity.class));
    }

    private void initSettings() {
        addBotFAB.setVisibility(View.VISIBLE);
        addBotFAB.hide();
        Entities.BaseRoom room = DatabaseHelper.getRoomById(roomId);
        roomTitleTV.setText(room.getTitle());
        NetworkHelper.loadRoomAvatar(room.getAvatar(), roomAvatarIV);
        DrawableCrossFadeFactory factory =
                new DrawableCrossFadeFactory.Builder().setCrossFadeEnabled(true).build();
        GlideApp.with(this).load(room.getBackgroundUrl()).transition(withCrossFade(factory)).centerCrop().into(backgroundView);
    }

    private void initRoomsAdapter(Entities.Complex complex, List<Entities.BaseRoom> rooms) {
        if (menuRoomsRV.getAdapter() != null)
            ((RoomsAdapter) menuRoomsRV.getAdapter()).dispose();
        @SuppressLint("RtlHardcoded")
        RoomsAdapter roomsAdapter = new RoomsAdapter(RoomActivity.this
                , complex.getComplexId()
                , rooms
                , room -> {
            drawerLayout.closeDrawer(Gravity.LEFT);
            startActivity(new Intent(RoomActivity.this
                    , RoomActivity.class)
                    .putExtra("complex_id", complex.getComplexId())
                    .putExtra("room_id", room.getRoomId()));
        });
        menuRoomsRV.setAdapter(roomsAdapter);
    }

    @SuppressLint("RtlHardcoded")
    public void onMenuDrawerBtnClicked(View view) {
        drawerLayout.openDrawer(Gravity.LEFT);
    }

    boolean draggingDock = false;

    private void initListeners() {

        widgetContainer.setOnDragListener(dragListener);

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dock.getLayoutParams();
        if (GraphicHelper.pxToDp(GraphicHelper.getScreenWidth()) < 400) {
            lp.width = MATCH_PARENT;
        } else {
            lp.width = GraphicHelper.dpToPx(400);
        }
        dock.setLayoutParams(lp);

        dock.setTag("Dock");
        dock.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    draggingDock = true;
                    ClipData.Item item = new ClipData.Item(v.getTag().toString());
                    ClipData dragData = new ClipData(
                            dock.getTag().toString(),
                            new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                            item);
                    View.DragShadowBuilder myShadow = new MyDragShadowBuilder(dock, false);
                    dock.startDrag(dragData,
                            myShadow,
                            dock,
                            0
                    );
                }
                return true;
            }
        });

        dock.setY(GraphicHelper.getScreenHeight());

        dockContainer.post(new Runnable() {
            @Override
            public void run() {
                dock.setX(dockContainer.getMeasuredWidth() / 2 - dock.getMeasuredWidth() / 2);
                dock.setY(dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100));

                controlPage.setY(GraphicHelper.getScreenHeight() - GraphicHelper.dpToPx(0));
            }
        });

        CardView searchBar = findViewById(R.id.searchBar);
        searchBar.setY(GraphicHelper.getScreenHeight() / 2  + GraphicHelper.dpToPx(100));
        CardView searchCloseContainer = findViewById(R.id.searchCloseContainer);
        searchCloseContainer.setX(GraphicHelper.dpToPx(-90));

        handleWorkerAccess();
        initUiData();
    }

    MotionEvent mEvent = null;

    private void handleWorkerAccess() {
        if (memberAccess.isCanModifyWorkers()) {
            botsFAB.setColorFilter(Color.WHITE);
        } else {
            botsFAB.setColorFilter(Color.GRAY);
        }
    }

    @Subscribe
    public void onMemberAccessUpdated(MemberAccessUpdated updated) {
        if (updated.getMemberAccess().getMembership().getUserId() == myId
                && updated.getMemberAccess().getMembership().getComplexId() == complexId) {
            memberAccess = updated.getMemberAccess();
            handleWorkerAccess();
        }
    }

    private void loadBots() {
        Packet packet = new Packet();
        Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(complexId);
        packet.setComplex(complex);
        Entities.Room room = new Entities.Room();
        room.setRoomId(roomId);
        packet.setBaseRoom(room);
        final RobotHandler robotHandler = NetworkHelper.getRetrofit().create(RobotHandler.class);
        Call<Packet> call = robotHandler.getWorkerships(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                workerships = packet.getWorkerships();
                if (workerships == null)
                    workerships = new ArrayList<>();
                bots = new ArrayList<>();
                pulseTable = new Hashtable<>();
                int maxHeight = 0;
                for (Entities.Workership ws : workerships) {
                    if (ws.getPosY() + ws.getHeight() > maxHeight) {
                        maxHeight = ws.getPosY() + ws.getHeight();
                    }
                }
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) widgetContainer.getLayoutParams();
                params.height = Math.max(GraphicHelper.dpToPx(maxHeight + 200), GraphicHelper.getScreenHeight());
                widgetContainer.setLayoutParams(params);
                for (final Entities.Workership ws : workerships) {
                    insertNewPulseView(ws);
                }
            }

            @Override
            public void onServerFailure() {

            }

            @Override
            public void onConnectionFailure() {

            }
        });
    }

    private void insertNewPulseView(Entities.Workership ws) {
        Entities.Bot bot = new Entities.Bot();
        bot.setBaseUserId(ws.getBotId());
        PulseView pulseView = new PulseView(RoomActivity.this);
        pulseView.setup(RoomActivity.this, controlId -> {
            Packet packet = new Packet();
            Entities.Complex c = new Entities.Complex();
            c.setComplexId(complexId);
            packet.setComplex(c);
            Entities.Room r = new Entities.Room();
            r.setRoomId(roomId);
            packet.setBaseRoom(r);
            packet.setBot(bot);
            packet.setControlId(controlId);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(PulseHandler.class).clickBotView(packet)
                    , new ServerCallback() {
                        @Override
                        public void onRequestSuccess(Packet packet) { }
                        @Override
                        public void onServerFailure() { }
                        @Override
                        public void onConnectionFailure() { }
                    });
        });
        pulseView.setLayoutParams(new RelativeLayout.LayoutParams(ws.getWidth()
                == 0 ? MATCH_PARENT : ws
                .getWidth() == -1 ? RelativeLayout.LayoutParams.WRAP_CONTENT
                : GraphicHelper.dpToPx(ws.getWidth()), ws.getHeight() == 0
                ? MATCH_PARENT : ws.getHeight()
                == -1 ? RelativeLayout.LayoutParams.WRAP_CONTENT : GraphicHelper
                .dpToPx(ws.getHeight())));
        pulseView.setX(GraphicHelper.dpToPx(ws.getPosX()));
        pulseView.setY(GraphicHelper.dpToPx(ws.getPosY()));
        if (ws.getPosX() == -1)
            ((RelativeLayout.LayoutParams) pulseView.getLayoutParams()).addRule(RelativeLayout.CENTER_HORIZONTAL);
        widgetContainer.addView(pulseView);
        pulseTable.put(bot.getBaseUserId(), pulseView);
        PulseHelper.getPulseViewTable().put(bot.getBaseUserId(), pulseView);

        Packet packet2 = new Packet();
        Entities.Complex packComplex = new Entities.Complex();
        packComplex.setComplexId(complexId);
        packet2.setComplex(packComplex);
        Entities.Room packRoom = new Entities.Room();
        packRoom.setRoomId(roomId);
        packet2.setBaseRoom(packRoom);
        packet2.setBot(bot);
        Call<Packet> call2 = NetworkHelper.getRetrofit()
                .create(PulseHandler.class).requestBotView(packet2);
        NetworkHelper.requestServer(call2, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) { }
            @Override
            public void onServerFailure() { }
            @Override
            public void onConnectionFailure() { }
        });
        pulseView.setTag("PulseView" + bot.getBaseUserId());
    }

    private WidgetLangingLocation downLocation = null;
    private long dragStartTime = 0;
    private float dragStartPoint = 0, dragEndPoint = 0;

    private void closeControlPage() {

        dock.animate()
                .y(dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100))
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator())
                .start();
        controlPage.animate().
                y(dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(0))
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator())
                .start();
        dockSecondStage.animate().alpha(0).setDuration(250).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dockSecondStage.setVisibility(View.GONE);
                dockFirstStage.setVisibility(View.VISIBLE);
                dockFirstStage.animate().alpha(1).setDuration(250).start();
            }
        }, 250);
    }

    public void onAddBtnClicked(View view) {
        Drawable messageDrw = getResources().getDrawable(R.drawable.ic_message);
        messageDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable fileDrw = getResources().getDrawable(R.drawable.ic_folder);
        fileDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable settingsDrw = getResources().getDrawable(R.drawable.ic_settings);
        settingsDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable editDrw = getResources().getDrawable(R.drawable.ic_edit);
        editDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        String[] itemTitles = new String[]{
                "Add room to this group", "Add new group"
        };
        Drawable[] itemIcons = new Drawable[]{
                messageDrw, fileDrw, settingsDrw, editDrw
        };

        BottomSheet.Builder builder = new BottomSheet.Builder(this);
        builder.setItems(itemTitles, itemIcons,
                (dialogInterface, i) -> {
                    if (i == 0) {
                        shadowBox.animate().alpha(1).setDuration(350).start();
                        startActivity(new Intent(RoomActivity.this, CreateRoomActivity.class)
                                .putExtra("complex_id", chosenComplexId));
                    } else if (i == 1) {
                        startActivity(new Intent(RoomActivity.this, CreateComplexActivity.class));
                    }
                }).setTitle("Add...")
                .setDarkTheme(true)
                .setTitleTextColor(Color.WHITE)
                .setContentType(BottomSheet.LIST)
                .setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3))
                .setItemTextColor(Color.WHITE)
                .show();
    }

    public void onSearchBtnClicked(View view) {
        if (dock.getY() == dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100)) {
            for (PulseView pulseView : pulseTable.values()) {
                pulseView.animate()
                        .alpha(0)
                        .scaleY(0.1f)
                        .scaleX(0.1f)
                        .setDuration(500)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dock.animate()
                            .y(GraphicHelper.getScreenHeight())
                            .setDuration(500)
                            .start();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            CardView searchBar = findViewById(R.id.searchBar);
                            searchBar.setVisibility(View.VISIBLE);
                            searchBar.animate()
                                    .alpha(1)
                                    .y(GraphicHelper.getScreenHeight() / 2 - GraphicHelper.dpToPx(100))
                                    .setDuration(500)
                                    .setInterpolator(new OvershootInterpolator())
                                    .start();
                            CardView searchCloseContainer = findViewById(R.id.searchCloseContainer);
                            searchCloseContainer.animate()
                                    .x(-GraphicHelper.dpToPx(28))
                                    .setDuration(500)
                                    .setInterpolator(new OvershootInterpolator())
                                    .start();
                        }
                    }, 400);
                }
            }, 400);
        }
    }

    public void onSearchCloseBtnClicked(View view) {
        closeSearchPage();
    }

    private void closeSearchPage() {
        CardView searchBar = findViewById(R.id.searchBar);
        searchBar.animate()
                .alpha(0)
                .y(GraphicHelper.getScreenHeight() / 2  + GraphicHelper.dpToPx(100))
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();
        searchCloseContainer.animate()
                .x(GraphicHelper.dpToPx(-90))
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator())
                .start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dock.animate()
                        .y(dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100))
                        .setDuration(500)
                        .setInterpolator(new OvershootInterpolator())
                        .start();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        searchBar.setVisibility(View.GONE);
                        for (PulseView pulseView : pulseTable.values()) {
                            pulseView.animate()
                                    .alpha(1)
                                    .scaleY(1)
                                    .scaleX(1)
                                    .setDuration(500)
                                    .setInterpolator(new OvershootInterpolator())
                                    .start();
                        }
                    }
                }, 400);
            }
        }, 400);
    }

    private void openControlPage() {
        dock.animate()
                .y(GraphicHelper.dpToPx(12))
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator())
                .start();
        controlPage.animate()
                .y(GraphicHelper.dpToPx(12 + 100))
                .setDuration(450)
                .setInterpolator(new OvershootInterpolator())
                .start();
        dockFirstStage.animate().alpha(0).setDuration(250).start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dockFirstStage.setVisibility(View.GONE);
                dockSecondStage.setVisibility(View.VISIBLE);
                dockSecondStage.animate().alpha(1).setDuration(250).start();
            }
        }, 250);
    }

    final View.OnDragListener dragListener = (v, event) -> {
        if (event.getAction() == DragEvent.ACTION_DRAG_ENDED ||
                event.getAction() == DragEvent.ACTION_DRAG_EXITED ||
                event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
            return true;
        }
        else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            dragStartTime = System.currentTimeMillis();
            return true;
        } else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
            if (draggingDock) {
                float value = event.getY() - dock.getMeasuredHeight() / 2f;
                if (value > dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100))
                    value = dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100);
                dock.animate().y(value).setDuration(5).start();
                controlPage.animate().y(event.getY() + dock.getMeasuredHeight() / 2f - GraphicHelper.dpToPx(48)).setDuration(5).start();
            }
            return true;
        } else if (event.getAction() == DragEvent.ACTION_DROP) {
            if (editedBot == null && pickedBot == null) {
                long dragEndTime = System.currentTimeMillis();
                if (dragEndPoint - dragStartPoint > GraphicHelper.dpToPx(12) &&
                        dragEndTime - dragStartTime > 0 && (
                        (dragEndPoint - dragStartPoint) / (dragEndTime - dragStartTime) > 1 ||
                        dragEndPoint - dragStartPoint > GraphicHelper.dpToPx(10))) {
                    float direction = dragEndPoint - dragStartPoint;
                    if (direction > 0) {
                        closeControlPage();
                    } else {
                        openControlPage();
                    }
                } else {
                    if (event.getY() >= dockContainer.getMeasuredHeight() / 2f) {
                        closeControlPage();
                    } else {
                        openControlPage();
                    }
                }
                draggingDock = false;
                return true;
            }
            View tvState = (View) event.getLocalState();
            if (editedBot != null) {
                Entities.Workership w = null;
                for (Entities.Workership worker : workerships) {
                    if (worker.getBotId() == editedBot.getBaseUserId()) {
                        w = worker;
                        break;
                    }
                }
                if (w == null) {
                    for (Entities.Workership worker : addedWorkerships) {
                        if (worker.getBotId() == editedBot.getBaseUserId()) {
                            w = worker;
                            break;
                        }
                    }
                    if (w == null) {
                        for (Entities.Workership worker : editedWorkerships) {
                            if (worker.getBotId() == editedBot.getBaseUserId()) {
                                w = worker;
                                break;
                            }
                        }
                        if (w != null) {
                            w.setPosX((int) (GraphicHelper.pxToDp(event.getX() - tvState.getMeasuredWidth() / 2f)));
                            w.setPosY((int) (GraphicHelper.pxToDp(event.getY() - tvState.getMeasuredHeight() / 2f)));
                        }
                    } else {
                        w.setPosX((int) (GraphicHelper.pxToDp(event.getX() - tvState.getMeasuredWidth() / 2f)));
                        w.setPosY((int) (GraphicHelper.pxToDp(event.getY() - tvState.getMeasuredHeight() / 2f)));
                    }
                }
                else {
                    w.setBotId(editedBot.getBaseUserId());
                    w.setPosX((int) (GraphicHelper.pxToDp(event.getX() - tvState.getMeasuredWidth() / 2f)));
                    w.setPosY((int) (GraphicHelper.pxToDp(event.getY() - tvState.getMeasuredHeight() / 2f)));
                    w.setRoomId(roomId);
                    editedWorkerships.add(w);
                }
            } else if (pickedBot != null) {
                Entities.Workership w = new Entities.Workership();
                w.setBotId(pickedBot.getBaseUserId());
                w.setPosX((int)(GraphicHelper.pxToDp(event.getX() - tvState.getMeasuredWidth() / 2f)));
                w.setPosY((int)(GraphicHelper.pxToDp(event.getY() - tvState.getMeasuredHeight() / 2f)));
                w.setRoomId(roomId);
                w.setWidth(150);
                w.setHeight(150);
                addedWorkerships.add(w);
            }
            try {
                ViewGroup tvParent = (ViewGroup) tvState.getParent();
                tvParent.removeView(tvState);
                ViewGroup container = (ViewGroup) v;
                container.addView(tvState);
                ((ViewGroup) tvState.getParent()).removeView(tvState);
                tvState.setX(event.getX() - tvState.getMeasuredWidth() / 2);
                tvState.setY(event.getY() - tvState.getMeasuredHeight() / 2);
                ((ViewGroup) v).addView(tvState);
                tvState.setTag("PulseView" + UUID.randomUUID().toString());
                tvState.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        ClipData.Item item = new ClipData.Item(tvState.getTag().toString());
                        ClipData dragData = new ClipData(
                                tvState.getTag().toString(),
                                new String[]{ClipDescription.MIMETYPE_TEXT_PLAIN},
                                item);
                        View.DragShadowBuilder myShadow = new MyDragShadowBuilder(tvState, true);
                        tvState.startDrag(dragData,
                                myShadow,
                                tvState,
                                0
                        );
                        return true;
                    }
                });
                v.setVisibility(View.VISIBLE);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return true;
        }
        return false;
    };

    @Subscribe
    public void onBotPicked(BotPicked botPicked) {
        closeBotPicker();
        pickedBot = botPicked.getBot();
        editedBot = null;
    }

    private void updateExistingPulseViewDimensions(Entities.Workership workership) {
        for (Map.Entry<Long, PulseView> entry : pulseTable.entrySet()) {
            if (entry.getKey() == workership.getBotId()) {
                PulseView pulseView = entry.getValue();
                pulseView.setX(GraphicHelper.dpToPx(workership.getPosX()));
                pulseView.setY(GraphicHelper.dpToPx(workership.getPosY()));
                int lpWidth = 0, lpHeight = 0;
                if (workership.getWidth() == 0) {
                    lpWidth = MATCH_PARENT;
                } else if (workership.getWidth() == -1) {
                    lpWidth = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else if (workership.getWidth() > 0) {
                    lpWidth = GraphicHelper.dpToPx(workership.getWidth());
                }
                if (workership.getHeight() == 0) {
                    lpHeight = MATCH_PARENT;
                } else if (workership.getHeight() == -1) {
                    lpHeight = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else if (workership.getHeight() > 0) {
                    lpHeight = GraphicHelper.dpToPx(workership.getHeight());
                }
                pulseView.setLayoutParams(new RelativeLayout.LayoutParams(lpWidth, lpHeight));
                int maxHeight = 0;
                for (Entities.Workership ws : workerships) {
                    if (ws.getPosY() + ws.getHeight() > maxHeight) {
                        maxHeight = ws.getPosY() + ws.getHeight();
                    }
                }
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) widgetContainer.getLayoutParams();
                params.height = GraphicHelper.dpToPx(maxHeight + 88);
                widgetContainer.setLayoutParams(params);
                break;
            }
        }
    }

    private void removeExistingPulseView(Entities.Workership workership) {
        for (Map.Entry<Long, PulseView> entry : pulseTable.entrySet()) {
            if (entry.getKey() == workership.getBotId()) {
                PulseView pulseView = entry.getValue();
                widgetContainer.removeView(pulseView);
                break;
            }
        }
    }

    private void closeBotPicker() {
        ValueAnimator valAnim = ValueAnimator.ofInt(GraphicHelper.dpToPx(0), GraphicHelper.dpToPx(-350));
        valAnim.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) botPicker.getLayoutParams();
            lp.rightMargin = (int) animation.getAnimatedValue();
            botPicker.setLayoutParams(lp);
        });
        ValueAnimator valAnim2 = ValueAnimator.ofFloat(GraphicHelper.dpToPx(0.45f), GraphicHelper.dpToPx(0f));
        valAnim2.addUpdateListener(animation -> {
            botPickerShadow.setAlpha((float)animation.getAnimatedValue());
        });
        valAnim.start();
        valAnim2.start();
        PulseHelper.setOnPreviewMode(false);
        PulseHelper.setPulseViewTablePreviews(pulseTable);
    }

    private void closeEditMode() {
        for (PulseView pulseView : pulseTable.values()) {
            pulseView.setOnLongClickListener(null);
            pulseView.setOnTouchListener(null);
        }
        ValueAnimator valAnim = ValueAnimator.ofInt(GraphicHelper.dpToPx(115), GraphicHelper.dpToPx(0));
        valAnim.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dock.getLayoutParams();
            lp.topMargin = (int)animation.getAnimatedValue();
            dock.setLayoutParams(lp);
        });
        valAnim.setDuration(300);
        ValueAnimator valAnim2 = ValueAnimator.ofInt(GraphicHelper.dpToPx(16), GraphicHelper.dpToPx(-96));
        valAnim2.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) addBotFAB.getLayoutParams();
            lp.bottomMargin = (int) animation.getAnimatedValue();
            addBotFAB.setLayoutParams(lp);
        });
        valAnim2.setDuration(550);
        ValueAnimator valAnim3 = ValueAnimator.ofInt(GraphicHelper.dpToPx(-28), GraphicHelper.dpToPx(-96));
        valAnim3.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) closeEditBtn.getLayoutParams();
            lp.leftMargin = (int) animation.getAnimatedValue();
            closeEditBtn.setLayoutParams(lp);
        });
        valAnim3.setDuration(425);
        valAnim.start();
        valAnim2.start();
        valAnim3.start();
        addBotFAB.hide();
    }

    @Override
    public void onBackPressed() {
        if (searchCloseContainer.getX() > GraphicHelper.dpToPx(-80)) {
            closeSearchPage();
        }
        else if (dock.getY() < dockContainer.getMeasuredHeight() - GraphicHelper.dpToPx(100)) {
            closeControlPage();
        }
        else if (((RelativeLayout.LayoutParams)botPicker.getLayoutParams()).rightMargin > GraphicHelper.dpToPx(-340)) {
            closeBotPicker();
            dragPanel.setVisibility(View.GONE);
            dragMode = false;
            transferringWidget = false;
            draggingPulseView = null;
        } else if (((RelativeLayout.LayoutParams) closeEditBtn.getLayoutParams()).leftMargin > GraphicHelper.dpToPx(-90)) {
            closeEditMode();
            dragMode = false;
            transferringWidget = false;
            draggingPulseView = null;
            this.getSliderInterface().unlock();
        } else {
            super.onBackPressed();
        }
    }

    public void onPickBotBtnClicked(View view) {
        transferringWidget = true;
        this.getSliderInterface().lock();
        dragPanel.setVisibility(View.VISIBLE);
        ValueAnimator valAnim = ValueAnimator.ofInt(GraphicHelper.dpToPx(-350), GraphicHelper.dpToPx(0));
        valAnim.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) botPicker.getLayoutParams();
            lp.rightMargin = (int) animation.getAnimatedValue();
            botPicker.setLayoutParams(lp);
        });
        ValueAnimator valAnim2 = ValueAnimator.ofFloat(GraphicHelper.dpToPx(0f), GraphicHelper.dpToPx(0.45f));
        valAnim2.addUpdateListener(animation -> {
            botPickerShadow.setAlpha((float)animation.getAnimatedValue());
        });
        valAnim.start();
        valAnim2.start();

        List<Entities.Bot> subBots = DatabaseHelper.getSubscribedBots();
        botPickerRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        BotPickerAdapter adapter = new BotPickerAdapter(this, dragListener, complexId, roomId, subBots);
        botPickerRV.setAdapter(adapter);
    }

    public void onCloseBotPickerBtnClicked(View view) {
        closeBotPicker();
        dragPanel.setVisibility(View.GONE);
        dragMode = false;
        transferringWidget = false;
        draggingPulseView = null;
    }

    public void onEditCloseBtnClicked(View view) {
        closeEditMode();
        dragPanel.setVisibility(View.GONE);
        this.getSliderInterface().unlock();
        for (Entities.Workership w : addedWorkerships) {
            Packet packet = new Packet();
            Entities.Complex c = new Entities.Complex();
            c.setComplexId(complexId);
            packet.setComplex(c);
            Entities.Room r = new Entities.Room();
            r.setRoomId(roomId);
            packet.setBaseRoom(r);
            packet.setWorkership(w);
            Entities.Bot b = new Entities.Bot();
            b.setBaseUserId(w.getBotId());
            packet.setBot(b);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(RobotHandler.class).addBotToRoom(packet), new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {

                }

                @Override
                public void onServerFailure() {

                }

                @Override
                public void onConnectionFailure() {

                }
            });
        }
        addedWorkerships.clear();
        for (Entities.Workership w : editedWorkerships) {
            Packet packet = new Packet();
            Entities.Complex c = new Entities.Complex();
            c.setComplexId(complexId);
            packet.setComplex(c);
            Entities.Room r = new Entities.Room();
            r.setRoomId(roomId);
            packet.setBaseRoom(r);
            packet.setWorkership(w);
            Entities.Bot b = new Entities.Bot();
            b.setBaseUserId(w.getBotId());
            packet.setBot(b);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(RobotHandler.class).updateWorkership(packet), new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {

                }

                @Override
                public void onServerFailure() {

                }

                @Override
                public void onConnectionFailure() {

                }
            });
        }
        editedWorkerships.clear();
        pickedBot = null;
        editedBot = null;
    }
}
