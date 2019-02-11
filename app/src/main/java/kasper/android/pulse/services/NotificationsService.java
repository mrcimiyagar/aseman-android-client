package kasper.android.pulse.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import java.util.Objects;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.callbacks.ui.FileListener;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.models.notifications.Notifications;
import kasper.android.pulse.retrofit.NotifHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static kasper.android.pulse.helpers.GraphicHelper.runOnUiThread;

public class NotificationsService extends IntentService {

    private HubConnection connection;

    public NotificationsService() {
        super("NotificationsService");
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
        Log.d("KasperLogger", "Notification service started");
        startConnection();
        return START_STICKY;
    }

    private void startConnection() {
        new Thread(() -> {
            while (!connectToHub()) {
                try {
                    Thread.sleep(5000);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        Log.d("KasperLogger", "Notification service destroyed");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("KasperLogger", "Notification service task removed");
        if (connection != null)
            connection.stop();
    }

    public boolean connectToHub() {
        if (connection != null) connection.stop();
        connection = HubConnectionBuilder.create(NetworkHelper.SERVER_IP + "NotificationsHub/").build();
        connection.on("NotifyInviteCreated", this::onInviteCreated, Notifications.InviteCreationNotification.class);
        connection.on("NotifyInviteCancelled", this::onInviteCancelled, Notifications.InviteCancellationNotification.class);
        connection.on("NotifyUserJointComplex", this::onUserJointComplex, Notifications.UserJointComplexNotification.class);
        connection.on("NotifyInviteAccepted", this::onInviteAccepted, Notifications.InviteAcceptanceNotification.class);
        connection.on("NotifyInviteIgnored", this::onInviteIgnored, Notifications.InviteIgnoranceNotification.class);
        connection.on("NotifyTextMessageReceived", this::onTextMessage, Notifications.TextMessageNotification.class);
        connection.on("NotifyPhotoMessageReceived", this::onPhotoMessage, Notifications.PhotoMessageNotification.class);
        connection.on("NotifyAudioMessageReceived", this::onAudioMessage, Notifications.AudioMessageNotification.class);
        connection.on("NotifyVideoMessageReceived", this::onVideoMessage, Notifications.VideoMessageNotification.class);
        connection.on("NotifyServiceMessageReceived", this::onServiceMessage, Notifications.ServiceMessageNotification.class);
        connection.on("NotifyContactCreated", this::onContactCreated, Notifications.ContactCreationNotification.class);
        connection.on("NotifyComplexDeleted", this::onComplexDeletion, Notifications.ComplexDeletionNotification.class);
        connection.on("NotifyRoomDeleted", this::onRoomDeletion, Notifications.RoomDeletionNotification.class);
        connection.on("NotifyBotSentBotView", this::onBotSentBotView, Notifications.BotSentBotViewNotification.class);
        connection.on("NotifyBotUpdatedBotView", this::onBotUpdatedBotView, Notifications.BotUpdatedBotViewNotification.class);
        connection.on("NotifyBotAnimatedBotView", this::onBotAnimatedBotView, Notifications.BotAnimatedBotViewNotification.class);
        connection.on("NotifyBotRanCommandsOnBotView", this::onBotRanCommandsOnBotView, Notifications.BotRanCommandsOnBotViewNotification.class);

        connection.onClosed(exception -> new Thread(() -> {
            try {
                GraphicHelper.getConnectionListener().reconnecting();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            startConnection();
        }).start());

        try {
            connection.start().doOnError(Throwable::printStackTrace).blockingAwait();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        Entities.Session session = DatabaseHelper.getSingleSession();
        if (session != null) {
            try {
                String authResult = connection.invoke(String.class, "Login"
                        , session.getSessionId(), session.getToken()).blockingGet();
                Log.d("Aseman", "SignalR Login Result : " + authResult);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        new Thread(() -> {
            try {
                while (connection != null && connection.getConnectionState() == HubConnectionState.CONNECTED) {
                    Log.d("Aseman", "sending keep-alive");
                    String authResult1 = connection.invoke(String.class
                            , "KeepAlive").blockingGet();
                    Log.d("Aseman", "received keep-alive answer : " + authResult1);
                    Thread.sleep(10000);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        try {
            GraphicHelper.getConnectionListener().connected();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private void onInviteCreated(final Notifications.InviteCreationNotification icn) {

        notifyServerNotifReceived(icn.getNotificationId());
    }

    private void onInviteCancelled(final Notifications.InviteCancellationNotification icn) {

        notifyServerNotifReceived(icn.getNotificationId());
    }

    private void onInviteAccepted(final Notifications.InviteAcceptanceNotification ian) {

        notifyServerNotifReceived(ian.getNotificationId());
    }

    private void onInviteIgnored(final Notifications.InviteIgnoranceNotification iin) {

        notifyServerNotifReceived(iin.getNotificationId());
    }

    private void onUserJointComplex(final Notifications.UserJointComplexNotification ujcn) {

        notifyServerNotifReceived(ujcn.getNotificationId());
    }

    private void onComplexDeletion(final Notifications.ComplexDeletionNotification cdn) {

        notifyServerNotifReceived(cdn.getNotificationId());
    }

    private void onRoomDeletion(final Notifications.RoomDeletionNotification rdn) {

        notifyServerNotifReceived(rdn.getNotificationId());
    }

    private void onBotSentBotView(final Notifications.BotSentBotViewNotification notif) {
        Log.d("Aseman", "Received BotView Init notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            runOnUiThread(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).buildUi(notif.getViewData());
            });

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotUpdatedBotView(final Notifications.BotUpdatedBotViewNotification notif) {
        Log.d("Aseman", "Received BotView Update notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            runOnUiThread(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).updateUi(notif.getUpdateData());
            });

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotAnimatedBotView(final Notifications.BotAnimatedBotViewNotification notif) {
        Log.d("Aseman", "Received BotView Animation notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            runOnUiThread(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).animateUi(notif.getAnimData());
            });

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotRanCommandsOnBotView(final Notifications.BotRanCommandsOnBotViewNotification notif) {
        Log.d("Aseman", "Received BotView RunCommands notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            runOnUiThread(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).runCommands(notif.getCommandsData());
            });

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onContactCreated(final Notifications.ContactCreationNotification ccn) {
        Log.d("Aseman", "Received contact notification");
        DatabaseHelper.notifyComplexCreated(ccn.getContact().getComplex());
        DatabaseHelper.notifyRoomCreated(ccn.getContact().getComplex().getRooms().get(0));
        DatabaseHelper.notifyUserCreated(ccn.getContact().getUser());
        DatabaseHelper.notifyUserCreated(ccn.getContact().getPeer());
        DatabaseHelper.notifyContactCreated(ccn.getContact());
        runOnUiThread(() -> {
            try {
                Log.d("Aseman", NetworkHelper.getMapper().writeValueAsString(ccn.getContact()));
                GraphicHelper.getContactListener().contactCreated(ccn.getContact());
            } catch (Exception ignored) { }
        });

        notifyServerNotifReceived(ccn.getNotificationId());
    }

    private void onTextMessage(final Notifications.TextMessageNotification mcn) {
        Log.d("Aseman", "Received text message notification");
        final Entities.TextMessage message = mcn.getMessage();
        DatabaseHelper.notifyTextMessageReceived(message);
        runOnUiThread(() -> {
            try {
                GraphicHelper.getRoomListener().updateRoomLastMessage(mcn.getMessage().getRoomId(), message);
            } catch (Exception ignored) { }
        });
        runOnUiThread(() -> {
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            try {
                GraphicHelper.getMessageListener().messageReceived(message, messageLocal);
            } catch (Exception ignored) { }
        });
        showMessageNotification(message, message.getText());

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onPhotoMessage(final Notifications.PhotoMessageNotification mcn) {
        Log.d("Aseman", "Received photo message notification");
        final Entities.PhotoMessage message = mcn.getMessage();
        DatabaseHelper.notifyPhotoMessageReceived(message);
        runOnUiThread(() -> {
            try {
                GraphicHelper.getRoomListener().updateRoomLastMessage(mcn.getMessage().getRoomId(), message);
            } catch (Exception ignored) { }
        });
        GraphicHelper.runOnUiThread(() -> {
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getPhoto().getFileId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            for (FileListener fileListener : GraphicHelper.getFileListeners()) {
                fileListener.fileReceived(DocTypes.Photo, message.getPhoto(), fileLocal);
            }
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            try {
                GraphicHelper.getMessageListener().messageReceived(message, messageLocal);
            } catch (Exception ignored) { }
        });
        showMessageNotification(message, "Photo");

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onAudioMessage(final Notifications.AudioMessageNotification mcn) {
        Log.d("Aseman", "Received audio message notification");
        final Entities.AudioMessage message = mcn.getMessage();
        DatabaseHelper.notifyAudioMessageReceived(message);
        GraphicHelper.runOnUiThread(() -> {
            try {
                GraphicHelper.getRoomListener().updateRoomLastMessage(mcn.getMessage().getRoomId(), message);
            } catch (Exception ignored) { }
        });
        GraphicHelper.runOnUiThread(() -> {
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getAudio().getFileId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            for (FileListener fileListener : GraphicHelper.getFileListeners()) {
                fileListener.fileReceived(DocTypes.Audio, message.getAudio(), fileLocal);
            }
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            try {
                GraphicHelper.getMessageListener().messageReceived(message, messageLocal);
            } catch (Exception ignored) { }
        });
        showMessageNotification(message, "Audio");

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onVideoMessage(final Notifications.VideoMessageNotification mcn) {
        Log.d("Aseman", "Received video message notification");
        final Entities.VideoMessage message = mcn.getMessage();
        DatabaseHelper.notifyVideoMessageReceived(message);
        GraphicHelper.runOnUiThread(() -> {
            try {
                GraphicHelper.getRoomListener().updateRoomLastMessage(mcn.getMessage().getRoomId(), message);
            } catch (Exception ignored) { }
        });
        GraphicHelper.runOnUiThread(() -> {
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getVideo().getFileId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            for (FileListener fileListener : GraphicHelper.getFileListeners()) {
                fileListener.fileReceived(DocTypes.Video, message.getVideo(), fileLocal);
            }
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            try {
                GraphicHelper.getMessageListener().messageReceived(message, messageLocal);
            } catch (Exception ignored) { }
        });
        showMessageNotification(message, "Video");

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onServiceMessage(final Notifications.ServiceMessageNotification mcn) {
        Log.d("Aseman", "Received service message notification");
        final Entities.ServiceMessage message = mcn.getMessage();
        DatabaseHelper.notifyServiceMessageReceived(message);
        GraphicHelper.runOnUiThread(() -> {
            try {
                GraphicHelper.getRoomListener().updateRoomLastMessage(mcn.getMessage().getRoomId(), message);
            } catch (Exception ignored) { }
        });
        GraphicHelper.runOnUiThread(() -> {
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            try {
                GraphicHelper.getMessageListener().messageReceived(message, messageLocal);
            } catch (Exception ignored) { }
        });
        showMessageNotification(message, message.getText());

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void showMessageNotification(Entities.Message message, String text) {
        try {
            Log.d("Aseman", NetworkHelper.getMapper().writeValueAsString(message));
        } catch (Exception ignored) { }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(message.getRoom().getComplex().getTitle() + ":" + message.getRoom().getTitle())
                .setContentText((message.getAuthor() != null ? message.getAuthor().getTitle() : "Aseman") + " : " + text)
                .setAutoCancel(true);

        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= 21) {
            mBuilder.setVibrate(new long[0]);
        }

        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("complex_id", message.getRoom().getComplex().getComplexId());
        intent.putExtra("room_id", message.getRoom().getRoomId());
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pIntent);

        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        assert mNotificationManager != null;
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void notifyServerNotifReceived(long notifId) {
        Packet packet = new Packet();
        Notifications.Notification notification = new Notifications.Notification();
        notification.setNotificationId(notifId);
        packet.setNotif(notification);
        Call<Packet> call = NetworkHelper.getRetrofit().create(NotifHandler.class).notifyNotifReceived(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
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
}