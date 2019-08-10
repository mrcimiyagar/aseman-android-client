package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.anadeainc.rxbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.ContactHandler;
import kasper.android.pulse.retrofit.InviteHandler;
import kasper.android.pulse.retrofit.MessageHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.services.AsemanService;
import kasper.android.pulse.services.MusicsService;
import lombok.Synchronized;
import retrofit2.Call;

public class StartupActivity extends AppCompatActivity {

    long startTime;

    private int doneTasksCount = 0;
    private final Object TASKS_LOCK = new Object();
    private List<Entities.Contact> syncedContacts = new ArrayList<>();
    private List<Entities.Complex> syncedComplexes = new ArrayList<>();
    private List<Entities.ComplexSecret> syncedComplexSecrets = new ArrayList<>();
    private List<Entities.Bot> syncedBotCreationsBots = new ArrayList<>();
    private List<Entities.BotCreation> syncedBotCreations = new ArrayList<>();
    private List<Entities.Bot> syncedBotSubscriptionsBots = new ArrayList<>();
    private List<Entities.BotSubscription> syncedBotSubscriptions = new ArrayList<>();
    private List<Entities.Invite> syncedInvites = new ArrayList<>();
    private List<Entities.Message> syncedMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        Core.getInstance().bus().register(this);

        startTime = System.currentTimeMillis();

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("newUser"))
            startSyncing();
        else
            syncDone();
    }

    @Override
    protected void onDestroy() {
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void onShowingToast(ShowToast showToast) {
        Toast.makeText(this, showToast.getText(), Toast.LENGTH_SHORT).show();
    }

    private void syncDone() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - startTime;
        if (diff >= 2000) {
            loadNextPage();
        } else {
            new Handler().postDelayed(this::loadNextPage, 2000 - diff);
        }
    }

    private void loadNextPage() {
        Entities.Session session = DatabaseHelper.getSingleSession();
        if (session != null && session.getToken().length() > 0) {
            startActivity(new Intent(StartupActivity.this, HomeActivity.class));
            finish();
        } else {
            startActivity(new Intent(StartupActivity.this, RegisterActivity.class));
            finish();
        }
    }

    private void startSyncing() {
        new Thread(() -> {
            Intent i = new Intent(StartupActivity.this, AsemanService.class);
            i.putExtra("reset", true);
            startService(i);
        }).start();
        new Thread(() -> {
            Intent i = new Intent(StartupActivity.this, MusicsService.class);
            i.putExtra("reset", true);
            startService(i);
        }).start();
        initContacts();
        initBots();
        initComplexes();
        initInvites();
    }

    private void initContacts() {
        ContactHandler contactHandler = NetworkHelper.getRetrofit().create(ContactHandler.class);
        Call<Packet> contactsCall = contactHandler.getContacts();
        NetworkHelper.requestServer(contactsCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet p) {
                new Thread(() -> {
                    syncedContacts = p.getContacts();
                    if (syncedContacts == null)
                        syncedContacts = new ArrayList<>();
                    LogHelper.log("Startup", "done contacts.");
                    notifyTaskDone();
                }).start();
            }
            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initContacts();
                }).start();
            }
            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initContacts();
                }).start();
            }
        });
    }

    private void initComplexes() {
        ComplexHandler complexHandler = NetworkHelper.getRetrofit().create(ComplexHandler.class);
        Call<Packet> contactsCall = complexHandler.getComplexes();
        NetworkHelper.requestServer(contactsCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet p) {
                new Thread(() -> {
                    syncedComplexes = p.getComplexes();
                    if (syncedComplexes == null)
                        syncedComplexes = new ArrayList<>();
                    syncedComplexSecrets = p.getComplexSecrets();
                    if (syncedComplexSecrets == null)
                        syncedComplexSecrets = new ArrayList<>();
                    initLastActions();
                    LogHelper.log("Startup", "done complexes.");
                    notifyTaskDone();
                }).start();
            }

            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initComplexes();
                }).start();
            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initComplexes();
                }).start();
            }
        });
    }

    private void initBots() {
        Call<Packet> creationCall = NetworkHelper.getRetrofit().create(RobotHandler.class).getCreatedBots();
        NetworkHelper.requestServer(creationCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet p) {
                new Thread(() -> {
                    syncedBotCreationsBots = p.getBots();
                    if (syncedBotCreationsBots == null)
                        syncedBotCreationsBots = new ArrayList<>();
                    syncedBotCreations = p.getBotCreations();
                    if (syncedBotCreations == null)
                        syncedBotCreations = new ArrayList<>();
                    LogHelper.log("Startup", "done bots.");
                    notifyTaskDone();
                }).start();
            }

            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initBots();
                }).start();
            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initBots();
                }).start();
            }
        });
        Call<Packet> subscriptionCall = NetworkHelper.getRetrofit().create(RobotHandler.class).getSubscribedBots();
        NetworkHelper.requestServer(subscriptionCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet p) {
                new Thread(() -> {
                    syncedBotSubscriptionsBots = p.getBots();
                    if (syncedBotSubscriptionsBots == null)
                        syncedBotSubscriptionsBots = new ArrayList<>();
                    syncedBotSubscriptions = p.getBotSubscriptions();
                    if (syncedBotSubscriptions == null)
                        syncedBotSubscriptions = new ArrayList<>();
                    LogHelper.log("Startup", "done subscriptions.");
                    notifyTaskDone();
                }).start();
            }

            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initBots();
                }).start();
            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initBots();
                }).start();
            }
        });
    }

    private void initInvites() {
        NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(InviteHandler.class).getInvites(), new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet p) {
                new Thread(() -> {
                    syncedInvites = p.getInvites();
                    if (syncedInvites == null)
                        syncedInvites = new ArrayList<>();
                    LogHelper.log("Startup", "done invites.");
                    notifyTaskDone();
                }).start();
            }
            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initInvites();
                }).start();
            }
            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initInvites();
                }).start();
            }
        });
    }

    private void initLastActions() {
        List<Entities.BaseRoom> rooms = new ArrayList<>();
        for (Entities.Complex complex : syncedComplexes) {
            for (Entities.BaseRoom room : complex.getAllRooms()) {
                Entities.Room r = new Entities.Room();
                r.setComplexId(room.getComplexId());
                r.setRoomId(room.getRoomId());
                rooms.add(r);
            }
        }
        Packet packet = new Packet();
        packet.setBaseRooms(rooms);
        Call<Packet> call = NetworkHelper.getRetrofit().create(MessageHandler.class).getLastActions(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                new Thread(() -> {
                    syncedMessages = new ArrayList<>();
                    List<Entities.BaseRoom> baseRooms = packet.getBaseRooms();
                    if (baseRooms == null)
                        baseRooms = new ArrayList<>();
                    for (Entities.BaseRoom room : baseRooms) {
                        if (room.getLastAction() != null)
                            syncedMessages.add(room.getLastAction());
                    }
                    LogHelper.log("Startup", "done last actions.");
                    notifyTaskDone();
                }).start();
            }
            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initLastActions();
                }).start();
            }
            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    initLastActions();
                }).start();
            }
        });
    }

    @Synchronized
    private void notifyTaskDone() {
        doneTasksCount++;
        LogHelper.log("Startup", "task done count " + doneTasksCount);
        if (doneTasksCount >= 6) {
            for (Entities.Contact contact : syncedContacts) {
                DatabaseHelper.notifyContactCreated(contact);
                DatabaseHelper.notifyUserCreated(contact.getPeer());
            }
            for (Entities.Complex complex : syncedComplexes) {
                DatabaseHelper.notifyComplexCreated(complex);
                for (Entities.BaseRoom room : complex.getAllRooms()) {
                    DatabaseHelper.notifyRoomCreated(room);
                }
                for (Entities.Membership mem : complex.getMembers()) {
                    DatabaseHelper.notifyUserCreated(mem.getUser());
                    DatabaseHelper.notifyMembershipCreated(mem);
                    if (mem.getMemberAccess() != null)
                        DatabaseHelper.notifyMemberAccessCreated(mem.getMemberAccess());
                }
                if (complex.getInvites() != null) {
                    for (Entities.Invite invite : complex.getInvites()) {
                        DatabaseHelper.notifyUserCreated(invite.getUser());
                        DatabaseHelper.notifyInviteReceived(invite);
                    }
                }
            }
            for (Entities.ComplexSecret complexSecret : syncedComplexSecrets) {
                DatabaseHelper.notifyComplexSecretCreated(complexSecret);
            }
            for (int counter = 0; counter < syncedBotCreationsBots.size(); counter++) {
                DatabaseHelper.notifyBotCreated(syncedBotCreationsBots.get(counter)
                        , syncedBotCreations.get(counter));
            }
            for (int counter = 0; counter < syncedBotSubscriptionsBots.size(); counter++) {
                DatabaseHelper.notifyBotSubscribed(syncedBotSubscriptionsBots.get(counter)
                        , syncedBotSubscriptions.get(counter));
            }
            for (Entities.Invite invite : syncedInvites) {
                DatabaseHelper.notifyComplexCreated(invite.getComplex());
                DatabaseHelper.notifyUserCreated(invite.getUser());
                DatabaseHelper.notifyInviteReceived(invite);
            }
            for (Entities.Message message : syncedMessages) {
                if (message instanceof Entities.TextMessage) {
                    DatabaseHelper.notifyTextMessageReceived((Entities.TextMessage) message);
                } else if (message instanceof Entities.PhotoMessage) {
                    DatabaseHelper.notifyPhotoMessageReceived((Entities.PhotoMessage) message);
                } else if (message instanceof Entities.AudioMessage) {
                    DatabaseHelper.notifyAudioMessageReceived((Entities.AudioMessage) message);
                } else if (message instanceof Entities.VideoMessage) {
                    DatabaseHelper.notifyVideoMessageReceived((Entities.VideoMessage) message);
                } else if (message instanceof Entities.ServiceMessage) {
                    DatabaseHelper.notifyServiceMessageReceived((Entities.ServiceMessage) message);
                }
            }
            runOnUiThread(this::syncDone);
        }
    }
}
