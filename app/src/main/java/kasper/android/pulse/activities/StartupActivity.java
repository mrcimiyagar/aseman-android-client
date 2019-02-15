package kasper.android.pulse.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.ContactHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import retrofit2.Call;

public class StartupActivity extends AppCompatActivity {

    long startTime;

    private int doneTasksCount = 0;
    private final Object TASKS_LOCK = new Object();
    private List<Entities.Contact> syncedContacts = new ArrayList<>();
    private List<Entities.Complex> syncedComplexes = new ArrayList<>();
    private List<Entities.Room> syncedRooms = new ArrayList<>();
    private List<Entities.Bot> syncedBotCreationsBots = new ArrayList<>();
    private List<Entities.BotCreation> syncedBotCreations = new ArrayList<>();
    private List<Entities.Bot> syncedBotSubscriptionsBots = new ArrayList<>();
    private List<Entities.BotSubscription> syncedBotSubscriptions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        startTime = System.currentTimeMillis();

        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey("newUser"))
            startSyncing();
        else
            syncDone();
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
        initComplexes();
        initRooms();
        initBots();
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
                    syncedContacts = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    syncedContacts = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
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
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
            }

            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    syncedContacts = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    syncedContacts = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
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
            public void onRequestSuccess(Packet p) {
                new Thread(() -> {
                    syncedRooms = p.getRooms();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
            }

            @Override
            public void onServerFailure() {
                new Thread(() -> {
                    syncedRooms = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    syncedRooms = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
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
                    syncedBotCreationsBots = new ArrayList<>();
                    syncedBotCreations = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();

            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    syncedBotCreationsBots = new ArrayList<>();
                    syncedBotCreations = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
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
                    syncedBotSubscriptionsBots = new ArrayList<>();
                    syncedBotSubscriptions = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
            }

            @Override
            public void onConnectionFailure() {
                new Thread(() -> {
                    syncedBotSubscriptionsBots = new ArrayList<>();
                    syncedBotSubscriptions = new ArrayList<>();
                    synchronized (TASKS_LOCK) {
                        notifyTaskDone();
                    }
                }).start();
            }
        });
    }

    private void notifyTaskDone() {
        synchronized (TASKS_LOCK) {
            doneTasksCount++;
            if (doneTasksCount == 4) {
                for (Entities.Contact contact : syncedContacts) {
                    DatabaseHelper.notifyContactCreated(contact);
                    DatabaseHelper.notifyUserCreated(contact.getPeer());
                }
                for (Entities.Complex complex : syncedComplexes) {
                    DatabaseHelper.notifyComplexCreated(complex);
                }
                for (Entities.Room room : syncedRooms) {
                    DatabaseHelper.notifyRoomCreated(room);
                }
                for (int counter = 0; counter < syncedBotCreationsBots.size(); counter++) {
                    DatabaseHelper.notifyBotCreated(syncedBotCreationsBots.get(counter)
                            , syncedBotCreations.get(counter));
                }
                for (int counter = 0; counter < syncedBotSubscriptionsBots.size(); counter++) {
                    DatabaseHelper.notifyBotSubscribed(syncedBotSubscriptionsBots.get(counter)
                            , syncedBotSubscriptions.get(counter));
                }
                runOnUiThread(this::syncDone);
            }
        }
    }
}
