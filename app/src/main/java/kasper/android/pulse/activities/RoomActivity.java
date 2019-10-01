package kasper.android.pulse.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.Pair;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.ramotion.navigationtoolbar.HeaderLayout;
import com.ramotion.navigationtoolbar.NavigationToolBarLayout;

import org.jetbrains.annotations.NotNull;
import org.michaelbel.bottomsheet.BottomSheet;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.BotPickerAdapter;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.components.LockableNestedScrollView;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.MyDragShadowBuilder;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.PulseHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.rxbus.notifications.BotPicked;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;
import kasper.android.pulse.rxbus.notifications.WorkerAdded;
import kasper.android.pulse.rxbus.notifications.WorkerRemoved;
import kasper.android.pulse.rxbus.notifications.WorkerUpdated;
import kasper.android.pulseframework.components.PulseView;
import kasper.android.pulseframework.models.Controls;
import retrofit2.Call;

public class RoomActivity extends BaseActivity {

    private long complexId;
    private long roomId;
    private long myId;
    private boolean afterChat;

    private CardView dock;
    private TextView roomTitleTV;
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

        NavigationToolBarLayout t = findViewById(R.id.bottomBar);
        t.setAdapter(new HeaderLayout.Adapter<HeaderLayout.ViewHolder>() {
            @Override
            public int getItemCount() {
                return 2;
            }

            @NotNull
            @Override
            public HeaderLayout.ViewHolder onCreateViewHolder(@NotNull ViewGroup viewGroup) {
                FrameLayout frameLayout = new FrameLayout(RoomActivity.this);
                frameLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                frameLayout.setBackgroundColor(Color.WHITE);
                return new HeaderLayout.ViewHolder(frameLayout);
            }

            @Override
            public void onBindViewHolder(@NotNull HeaderLayout.ViewHolder viewHolder, int i) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
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
            botsFAB.setOnClickListener(view ->
                    startActivityForResult(new Intent(RoomActivity.this, AddBotToRoomActivity.class)
                                    .putExtra("complex_id", complexId)
                                    .putExtra("room_id", roomId)
                                    .putExtra("existing_bots", workerships
                                            .toArray(new Entities.Workership[0]))
                            , 1));
        } else {
            botsFAB.setOnClickListener(view -> {});
        }
    }

    public void onMenuBtnClicked(View view) {
        Drawable messageDrw = getResources().getDrawable(R.drawable.ic_message);
        messageDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable fileDrw = getResources().getDrawable(R.drawable.ic_folder);
        fileDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable settingsDrw = getResources().getDrawable(R.drawable.ic_settings);
        settingsDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        Drawable editDrw = getResources().getDrawable(R.drawable.ic_edit);
        editDrw.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        String[] itemTitles = new String[] {
                "Messages", "Files", "Settings", "Edit Bots"
        };
        Drawable[] itemIcons = new Drawable[] {
                messageDrw, fileDrw, settingsDrw, editDrw
        };

        BottomSheet.Builder builder = new BottomSheet.Builder(this);
        builder.setItems(itemTitles, itemIcons,
                (dialogInterface, i) -> {
            if (i == 0) {
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
                ValueAnimator valAnim = ValueAnimator.ofInt(GraphicHelper.dpToPx(-48), GraphicHelper.dpToPx(-148));
                valAnim.addUpdateListener(animation -> {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dock.getLayoutParams();
                    lp.bottomMargin = (int)animation.getAnimatedValue();
                    dock.setLayoutParams(lp);
                });
                valAnim.setDuration(300);
                ValueAnimator valAnim2 = ValueAnimator.ofInt(GraphicHelper.dpToPx(-96), GraphicHelper.dpToPx(16));
                valAnim2.addUpdateListener(animation -> {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) addBotFAB.getLayoutParams();
                    lp.bottomMargin = (int)animation.getAnimatedValue();
                    addBotFAB.setLayoutParams(lp);
                });
                valAnim2.setDuration(550);
                ValueAnimator valAnim3 = ValueAnimator.ofInt(GraphicHelper.dpToPx(-96), GraphicHelper.dpToPx(-28));
                valAnim3.addUpdateListener(animation -> {
                    RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) closeEditBtn.getLayoutParams();
                    lp.leftMargin = (int)animation.getAnimatedValue();
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
                            View.DragShadowBuilder myShadow = new MyDragShadowBuilder(pulseView);
                            pulseView.startDrag(dragData,
                                    myShadow,
                                    pulseView,
                                    0
                            );
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

    private void initViews() {
        dock = findViewById(R.id.dock);
        roomTitleTV = findViewById(R.id.roomTitle);
        roomAvatarIV = findViewById(R.id.roomAvatar);
        backgroundView = findViewById(R.id.roomBackground);
        widgetContainer = findViewById(R.id.fragment_room_widget_container);
        msgFAB = findViewById(R.id.roomMessagesFAB);
        docsFAB = findViewById(R.id.roomFilesFAB);
        botsFAB = findViewById(R.id.roomBotsFAB);
        addBotFAB = findViewById(R.id.addBotFAB);
        closeEditBtn = findViewById(R.id.closeEditBtn);
        botPickerShadow = findViewById(R.id.botPickerShadow);
        botPicker = findViewById(R.id.botPicker);
        botPickerRV = findViewById(R.id.botPickerRV);
        dragPanel = findViewById(R.id.dragPanel);
        scrollView = findViewById(R.id.scrollView);
    }

    private void initSettings() {
        addBotFAB.setVisibility(View.VISIBLE);
        addBotFAB.hide();
        Entities.BaseRoom room = DatabaseHelper.getRoomById(roomId);
        roomTitleTV.setText(room.getTitle());
        NetworkHelper.loadRoomAvatar(room.getAvatar(), roomAvatarIV);
        GlideApp.with(this).load(room.getBackgroundUrl()).into(backgroundView);
    }

    private void initListeners() {
        if (!afterChat) {
            msgFAB.setColorFilter(Color.WHITE);
            msgFAB.setOnClickListener(v -> startActivity(new Intent(RoomActivity.this, ChatActivity.class)
                    .putExtra("complex_id", complexId)
                    .putExtra("room_id", roomId)
                    .putExtra("start_file_id", -1L)
                    .putExtra("after_room", true)));
        } else {
            msgFAB.setColorFilter(Color.GRAY);
        }

        docsFAB.setOnClickListener(v -> startActivity(new Intent(RoomActivity.this, DocsActivity.class)
                .putExtra("complex_id", complexId)
                .putExtra("room_id", roomId)));

        scrollView.setScrollingEnabled(false);

        widgetContainer.setOnDragListener(dragListener);

        handleWorkerAccess();
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
                params.height = Math.max(GraphicHelper.dpToPx(maxHeight + 88), GraphicHelper.getScreenHeight());
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
                == 0 ? RelativeLayout.LayoutParams.MATCH_PARENT : ws
                .getWidth() == -1 ? RelativeLayout.LayoutParams.WRAP_CONTENT
                : GraphicHelper.dpToPx(ws.getWidth()), ws.getHeight() == 0
                ? RelativeLayout.LayoutParams.MATCH_PARENT : ws.getHeight()
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

    final View.OnDragListener dragListener = (v, event) -> {
        if (event.getAction() == DragEvent.ACTION_DRAG_ENDED ||
                event.getAction() == DragEvent.ACTION_DRAG_EXITED ||
                event.getAction() == DragEvent.ACTION_DRAG_ENTERED) {
            return true;
        }
        else if (event.getAction() == DragEvent.ACTION_DRAG_STARTED) {
            return true;
        } else if (event.getAction() == DragEvent.ACTION_DRAG_LOCATION) {
            return true;
        } else if (event.getAction() == DragEvent.ACTION_DROP) {
            Entities.Workership w = new Entities.Workership();
            w.setWidth(150);
            w.setHeight(150);
            w.setPosX((int)(GraphicHelper.pxToDp(event.getX() - GraphicHelper.dpToPx(75))));
            w.setPosY((int)(GraphicHelper.pxToDp(event.getY() - GraphicHelper.dpToPx(75))));
            w.setRoomId(roomId);
            if (editedBot != null) {
                w.setBotId(editedBot.getBaseUserId());
                editedWorkerships.add(w);
            } else if (pickedBot != null) {
                w.setBotId(pickedBot.getBaseUserId());
                addedWorkerships.add(w);
            }
            try {
                View tvState = (View) event.getLocalState();
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
                        View.DragShadowBuilder myShadow = new MyDragShadowBuilder(tvState);
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
                    lpWidth = RelativeLayout.LayoutParams.MATCH_PARENT;
                } else if (workership.getWidth() == -1) {
                    lpWidth = RelativeLayout.LayoutParams.WRAP_CONTENT;
                } else if (workership.getWidth() > 0) {
                    lpWidth = GraphicHelper.dpToPx(workership.getWidth());
                }
                if (workership.getHeight() == 0) {
                    lpHeight = RelativeLayout.LayoutParams.MATCH_PARENT;
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
        }
        ValueAnimator valAnim = ValueAnimator.ofInt(GraphicHelper.dpToPx(-148), GraphicHelper.dpToPx(-48));
        valAnim.addUpdateListener(animation -> {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) dock.getLayoutParams();
            lp.bottomMargin = (int) animation.getAnimatedValue();
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
        if (((RelativeLayout.LayoutParams)botPicker.getLayoutParams()).rightMargin > GraphicHelper.dpToPx(-340)) {
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
    }
}
