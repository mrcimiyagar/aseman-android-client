package kasper.android.pulse.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.anadeainc.rxbus.Subscribe;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.PulseHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;
import kasper.android.pulse.rxbus.notifications.WorkerAdded;
import kasper.android.pulse.rxbus.notifications.WorkerRemoved;
import kasper.android.pulse.rxbus.notifications.WorkerUpdated;
import kasper.android.pulseframework.components.PulseView;
import retrofit2.Call;

public class RoomActivity extends AppCompatActivity {

    private long complexId;
    private long roomId;
    private long myId;
    private boolean afterChat;

    private ImageView backgroundView;
    private RelativeLayout widgetContainer;
    private ImageButton msgFAB;
    private ImageButton docsFAB;
    private ImageButton botsFAB;

    private List<Entities.Workership> workerships;
    private List<Entities.Bot> bots;
    private Hashtable<Long, PulseView> pulseTable;

    private Entities.MemberAccess memberAccess;

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
    protected void onDestroy() {
        PulseHelper.setCurrentComplexId(-1);
        PulseHelper.setCurrentRoomId(-1);
        for (Entities.Bot bot : bots)
            PulseHelper.getPulseViewTable().remove(bot.getBaseUserId());
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    private void load() {
        initViews();
        widgetContainer.removeAllViews();
        backgroundView.setBackgroundResource(0);
        backgroundView.setBackgroundColor(Color.TRANSPARENT);
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

    private void initViews() {
        backgroundView = findViewById(R.id.fragment_room_background_container);
        widgetContainer = findViewById(R.id.fragment_room_widget_container);
        msgFAB = findViewById(R.id.roomMessagesFAB);
        docsFAB = findViewById(R.id.roomFilesFAB);
        botsFAB = findViewById(R.id.roomBotsFAB);
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

        handleWorkerAccess();
    }

    private void handleWorkerAccess() {
        if (memberAccess.isCanModifyWorkers()) {
            botsFAB.setColorFilter(Color.WHITE);
            botsFAB.setOnClickListener(view ->
                    startActivityForResult(new Intent(RoomActivity.this, AddBotToRoomActivity.class)
                                    .putExtra("complex_id", complexId)
                                    .putExtra("room_id", roomId)
                                    .putExtra("existing_bots", workerships
                                            .toArray(new Entities.Workership[0]))
                            , 1));
        } else {
            botsFAB.setColorFilter(Color.GRAY);
            botsFAB.setOnClickListener(view -> {});
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
                bots = new ArrayList<>();
                pulseTable = new Hashtable<>();
                int maxHeight = 0;
                for (Entities.Workership ws : workerships) {
                    if (ws.getPosY() + ws.getHeight() > maxHeight) {
                        maxHeight = ws.getPosY() + ws.getHeight();
                    }
                }
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) widgetContainer.getLayoutParams();
                params.height = GraphicHelper.dpToPx(maxHeight + 88);
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

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
