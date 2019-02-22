package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.mbms.FileServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.anadeainc.rxbus.Subscribe;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.util.Pair;
import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.AuthHandler;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.ContactHandler;
import kasper.android.pulse.retrofit.MessageHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import kasper.android.pulse.services.FilesService;
import kasper.android.pulse.services.MusicsService;
import kasper.android.pulse.services.NotificationsService;
import retrofit2.Call;

public class StartupActivity extends BaseActivity {

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
    private long syncingRoomsCount = 0;
    private List<Entities.Message> syncedMessages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        Core.getInstance().bus().register(this);

        stopService(new Intent(this, FilesService.class));
        stopService(new Intent(this, MusicsService.class));
        stopService(new Intent(this, NotificationsService.class));

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
    public void onUiThreadRequested(UiThreadRequested uiThreadRequested) {
        this.runOnUiThread(uiThreadRequested.getRunnable());
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
        initContacts();
        initBots();
        initComplexes();
    }

    private void initContacts() {
        ContactHandler contactHandler = NetworkHelper.getRetrofit().create(ContactHandler.class);
        Call<Packet> contactsCall = contactHandler.getContacts();
        NetworkHelper.requestServer(contactsCall, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet p) {
                new Thread(() -> {
                    syncedContacts = p.getContacts();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
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
                    syncedComplexSecrets = p.getComplexSecrets();
                    initMessages();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
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
                    syncedBotCreations = p.getBotCreations();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
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
                    syncedBotSubscriptions = p.getBotSubscriptions();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
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

    private void initMessages() {
        syncingRoomsCount = 0;
        List<Pair<Long, Long>> messageContainers = new ArrayList<>();
        for (Entities.Complex complex : syncedComplexes) {
            syncingRoomsCount += complex.getRooms().size();
            for (Entities.Room room : complex.getRooms()) {
                messageContainers.add(new Pair<>(complex.getComplexId(), room.getRoomId()));
            }
        }
        for (Pair<Long, Long> messageContainer : messageContainers)
            if (messageContainer.first != null && messageContainer.second != null)
                fetchRoomMessages(messageContainer.first, messageContainer.second);
    }

    private void fetchRoomMessages(long complexId, long roomId) {
        Packet packet = new Packet();
        Entities.Complex complex = new Entities.Complex();
        complex.setComplexId(complexId);
        packet.setComplex(complex);
        Entities.Room room = new Entities.Room();
        room.setRoomId(roomId);
        packet.setRoom(room);
        Call<Packet> call = NetworkHelper.getRetrofit().create(MessageHandler.class).getMessages(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) {
                new Thread(() -> {
                    syncedMessages.addAll(packet.getMessages());
                    synchronized (TASKS_LOCK) {
                        notifyMessageTaskDone();
                    }
                }).start();
            }
            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    fetchRoomMessages(complexId, roomId);
                }).start();
            }
            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                    } catch (Exception ignored) { }
                    fetchRoomMessages(complexId, roomId);
                }).start();
            }
        });
    }

    private void notifyMessageTaskDone() {
        synchronized (TASKS_LOCK) {
            syncingRoomsCount--;
            if (syncingRoomsCount == 0) {
                synchronized (TASKS_LOCK) {
                    notifyTaskDone();
                }
            }
        }
    }

    private void notifyTaskDone() {
        synchronized (TASKS_LOCK) {
            doneTasksCount++;
            if (doneTasksCount == 5) {
                for (Entities.Contact contact : syncedContacts) {
                    DatabaseHelper.notifyContactCreated(contact);
                    DatabaseHelper.notifyUserCreated(contact.getPeer());
                }
                for (Entities.Complex complex : syncedComplexes) {
                    DatabaseHelper.notifyComplexCreated(complex);
                    for (Entities.Room room : complex.getRooms()) {
                        DatabaseHelper.notifyRoomCreated(room);
                    }
                    for (Entities.Membership mem : complex.getMembers()) {
                        DatabaseHelper.notifyMembershipCreated(mem);
                        DatabaseHelper.notifyUserCreated(mem.getUser());
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
}
