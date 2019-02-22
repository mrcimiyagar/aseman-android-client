package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;

import com.anadeainc.rxbus.Subscribe;

import java.util.Hashtable;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.annotation.Nullable;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.BotProfileUpdating;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.ComplexProfileUpdating;
import kasper.android.pulse.models.extras.RoomProfileUpdating;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.extras.UserProfileUpdating;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.retrofit.UserHandler;
import kasper.android.pulse.rxbus.notifications.BotProfileUpdated;
import kasper.android.pulse.rxbus.notifications.ComplexProfileUpdated;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.RoomProfileUpdated;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.rxbus.notifications.UserProfileUpdated;

public class ProfileService extends IntentService {

    private static LinkedBlockingDeque<UserProfileUpdating> userQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<BotProfileUpdating> botQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<ComplexProfileUpdating> complexQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<RoomProfileUpdating> roomQueue = new LinkedBlockingDeque<>();

    private static Thread userThread, complexThread, roomThread, botThread;
    private static boolean alive = false;
    private static Hashtable<Long, Object> map = new Hashtable<>();

    public ProfileService() {
        super("ProfileService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        LogHelper.log("Aseman", "Profile service started");

        Core.getInstance().bus().register(this);

        alive = true;

        if (userThread == null) {
            userThread = new Thread(() -> {
                try {
                    while (alive) {
                        UserProfileUpdating updating = userQueue.take();
                        try {
                            long fileId = FilesService.uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                    , -1, -1, true, false));
                            map.put(fileId, updating.getUser());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            userQueue.offerFirst(updating);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!userThread.isAlive())
            userThread.start();

        if (botThread == null) {
            botThread = new Thread(() -> {
                try {
                    while (alive) {
                        BotProfileUpdating updating = botQueue.take();
                        try {
                            long fileId = FilesService.uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                    , -1, -1, true, false));
                            map.put(fileId, updating.getBot());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            botQueue.offerFirst(updating);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!botThread.isAlive())
            botThread.start();

        if (complexThread == null) {
            complexThread = new Thread(() -> {
                try {
                    while (alive) {
                        ComplexProfileUpdating updating = complexQueue.take();
                        try {
                            long fileId = FilesService.uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                    , -1, -1, true, false));
                            map.put(fileId, updating.getComplex());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            complexQueue.offerFirst(updating);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!complexThread.isAlive())
            complexThread.start();

        if (roomThread == null) {
            roomThread = new Thread(() -> {
                try {
                    while (alive) {
                        RoomProfileUpdating updating = roomQueue.take();
                        try {
                            long fileId = FilesService.uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                    , -1, -1, true, false));
                            map.put(fileId, updating.getRoom());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            roomQueue.offerFirst(updating);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!roomThread.isAlive())
            roomThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.log("Aseman", "File service destroyed");
        Core.getInstance().bus().unregister(this);
        alive = false;
        super.onDestroy();
    }

    public static void updateUserProfileAvatar(UserProfileUpdating updating) {
        userQueue.offer(updating);
    }

    public static void updateBotProfileAvatar(BotProfileUpdating updating) {
        botQueue.offer(updating);
    }

    public static void updateComplexProfileAvatar(ComplexProfileUpdating updating) {
        complexQueue.offer(updating);
    }

    public static void updateRoomProfileAvatar(RoomProfileUpdating updating) {
        roomQueue.offer(updating);
    }

    @Subscribe
    public void onFileUploaded(FileUploaded fileUploaded) {
        long localFileId = fileUploaded.getLocalFileId();
        long onlineFileId = fileUploaded.getOnlineFileId();
        if (map.containsKey(localFileId)) {
            Object object = map.get(localFileId);
            if (object instanceof Entities.User) {
                Entities.User user = (Entities.User) object;
                user.setAvatar(onlineFileId);
                Packet packet = new Packet();
                packet.setUser(user);
                NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(UserHandler.class).updateUserProfile(packet), new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        DatabaseHelper.notifyUserCreated(user);
                        Core.getInstance().bus().post(new UserProfileUpdated(user));
                    }
                    @Override
                    public void onServerFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                    @Override
                    public void onConnectionFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                });
            } else if (object instanceof Entities.Bot) {
                Entities.Bot bot = (Entities.Bot) object;
                bot.setAvatar(onlineFileId);
                Packet packet = new Packet();
                packet.setBot(bot);
                NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(RobotHandler.class).updateBotProfile(packet), new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        DatabaseHelper.notifyBotUpdated(bot);
                        Core.getInstance().bus().post(new BotProfileUpdated(bot));
                    }
                    @Override
                    public void onServerFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                    @Override
                    public void onConnectionFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                });
            } else if (object instanceof Entities.Complex) {
                Entities.Complex complex = (Entities.Complex) object;
                complex.setAvatar(onlineFileId);
                Packet packet = new Packet();
                packet.setComplex(complex);
                NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(ComplexHandler.class).updateComplexProfile(packet), new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        DatabaseHelper.notifyComplexCreated(complex);
                        Core.getInstance().bus().post(new ComplexProfileUpdated(complex));
                    }
                    @Override
                    public void onServerFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                    @Override
                    public void onConnectionFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                });
            } else if (object instanceof Entities.Room) {
                Entities.Room room = (Entities.Room) object;
                room.setAvatar(onlineFileId);
                Packet packet = new Packet();
                packet.setRoom(room);
                NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(RoomHandler.class).updateRoomProfile(packet), new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        DatabaseHelper.notifyRoomCreated(room);
                        Core.getInstance().bus().post(new RoomProfileUpdated(room));
                    }
                    @Override
                    public void onServerFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                    @Override
                    public void onConnectionFailure() {
                        Core.getInstance().bus().post(new ShowToast("Profile update failure"));
                    }
                });
            }
        }
    }
}