package kasper.android.pulse.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.models.notifications.Notifications;
import kasper.android.pulse.retrofit.NotifHandler;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ConnectionStateChanged;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.FileReceived;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSeen;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import retrofit2.Call;

public class NotificationsService extends IntentService {

    private static String connectionState;
    public static String getConnectionState() {
        return connectionState;
    }

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

    @Override
    public void onDestroy() {
        Log.d("KasperLogger", "Notification service destroyed");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("KasperLogger", "Notification service task removed");
        try {
            if (connection != null) connection.stop().blockingAwait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
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

    public boolean connectToHub() {

        try {
            if (connection != null) {
                connection.onClosed(exception -> { });
                connection.stop().blockingAwait();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        connection = HubConnectionBuilder.create(NetworkHelper.SERVER_IP + "NotificationsHub/").build();
        connection.on("NotifyMessageSeen", this::onMessageSeen, Notifications.MessageSeenNotification.class);
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
        connection.on("NotifyBotAddedToRoom", this::onBotAddedToRoom, Notifications.BotAddedToRoomNotification.class);
        connection.on("NotifyBotRemovedFromRoom", this::onBotRemovedFromRoom, Notifications.BotRemovedFromRoomNotification.class);

        connection.onClosed(exception -> new Thread(() -> {
            try {
                connectionState = "Disconnected";
                Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Reconnecting));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            startConnection();
        }).start());

        connectionState = "Connecting";

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
                    if (authResult1.equals("failure")) {
                        connection.stop();
                    } else {
                        Log.d("Aseman", "received keep-alive answer : " + authResult1);
                        Thread.sleep(10000);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        try {
            Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Connected));
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        connectionState = "Online";

        return true;
    }

    private void onBotAddedToRoom(Notifications.BotAddedToRoomNotification notif) {
        Log.d("Aseman", "Received Bot Added notification");

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotRemovedFromRoom(Notifications.BotRemovedFromRoomNotification notif) {
        Log.d("Aseman", "Received Bot Removed notification");

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onMessageSeen(final Notifications.MessageSeenNotification msn) {
        Log.d("Aseman", "Received Message Seen notification");
        Entities.Message message = DatabaseHelper.getMessageById(msn.getMessageId());
        if (message != null) {
            message.setSeenCount(msn.getMessageSeenCount());
            DatabaseHelper.notifyMessageUpdated(message);
            Core.getInstance().bus().post(new MessageSeen(message));
        }

        notifyServerNotifReceived(msn.getNotificationId());
    }

    private void onInviteCreated(final Notifications.InviteCreationNotification icn) {
        Log.d("Aseman", "Received Invite Creation notification");

        notifyServerNotifReceived(icn.getNotificationId());
    }

    private void onInviteCancelled(final Notifications.InviteCancellationNotification icn) {
        Log.d("Aseman", "Received Invite Cancellation notification");

        notifyServerNotifReceived(icn.getNotificationId());
    }

    private void onInviteAccepted(final Notifications.InviteAcceptanceNotification ian) {
        Log.d("Aseman", "Received Invite Acceptance notification");

        notifyServerNotifReceived(ian.getNotificationId());
    }

    private void onInviteIgnored(final Notifications.InviteIgnoranceNotification iin) {
        Log.d("Aseman", "Received Invite Ignorance notification");

        notifyServerNotifReceived(iin.getNotificationId());
    }

    private void onUserJointComplex(final Notifications.UserJointComplexNotification ujcn) {
        Log.d("Aseman", "Received User Joint Complex notification");

        notifyServerNotifReceived(ujcn.getNotificationId());
    }

    private void onComplexDeletion(final Notifications.ComplexDeletionNotification cdn) {
        Log.d("Aseman", "Received Complex Deletion notification");

        notifyServerNotifReceived(cdn.getNotificationId());
    }

    private void onRoomDeletion(final Notifications.RoomDeletionNotification rdn) {
        Log.d("Aseman", "Received Room Deletion notification");

        notifyServerNotifReceived(rdn.getNotificationId());
    }

    private void onBotSentBotView(final Notifications.BotSentBotViewNotification notif) {
        Log.d("Aseman", "Received BotView Init notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).buildUi(notif.getViewData());
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotUpdatedBotView(final Notifications.BotUpdatedBotViewNotification notif) {
        Log.d("Aseman", "Received BotView Update notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).updateUi(notif.getUpdateData());
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotAnimatedBotView(final Notifications.BotAnimatedBotViewNotification notif) {
        Log.d("Aseman", "Received BotView Animation notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).animateUi(notif.getAnimData());
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotRanCommandsOnBotView(final Notifications.BotRanCommandsOnBotViewNotification notif) {
        Log.d("Aseman", "Received BotView RunCommands notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                if (PulseHelper.getPulseViewTable().containsKey(notif.getBotId()))
                    PulseHelper.getPulseViewTable().get(notif.getBotId()).runCommands(notif.getCommandsData());
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onContactCreated(final Notifications.ContactCreationNotification ccn) {
        Log.d("Aseman", "Received contact notification");

        Entities.Complex complex = ccn.getContact().getComplex();
        Entities.Room room = complex.getRooms().get(0);
        room.setComplex(complex);
        Entities.User user = ccn.getContact().getUser();
        Entities.User peer = ccn.getContact().getPeer();

        DatabaseHelper.notifyComplexCreated(complex);
        DatabaseHelper.notifyRoomCreated(room);
        DatabaseHelper.notifyUserCreated(user);
        DatabaseHelper.notifyUserCreated(peer);
        DatabaseHelper.notifyContactCreated(ccn.getContact());

        Core.getInstance().bus().post(new ComplexCreated(complex));
        Core.getInstance().bus().post(new RoomCreated(complex.getComplexId(), room));
        Core.getInstance().bus().post(new ContactCreated(ccn.getContact()));

        notifyServerNotifReceived(ccn.getNotificationId());
    }

    private void onTextMessage(final Notifications.TextMessageNotification mcn) {
        Log.d("Aseman", "Received text message notification");
        final Entities.TextMessage message = mcn.getMessage();
        message.setSeenByMe(false);
        if (DatabaseHelper.notifyTextMessageReceived(message)) {
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            Core.getInstance().bus().post(new MessageReceived(message, messageLocal));
            showMessageNotification(message, message.getText());
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onPhotoMessage(final Notifications.PhotoMessageNotification mcn) {
        Log.d("Aseman", "Received photo message notification");
        final Entities.PhotoMessage message = mcn.getMessage();
        message.setSeenByMe(false);
        if (DatabaseHelper.notifyPhotoMessageReceived(message)) {
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getPhoto().getFileId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            Core.getInstance().bus().post(new FileReceived(DocTypes.Photo, message.getPhoto(), fileLocal));
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            Core.getInstance().bus().post(new MessageReceived(message, messageLocal));
            showMessageNotification(message, "Photo");
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onAudioMessage(final Notifications.AudioMessageNotification mcn) {
        Log.d("Aseman", "Received audio message notification");
        final Entities.AudioMessage message = mcn.getMessage();
        message.setSeenByMe(false);
        if (DatabaseHelper.notifyAudioMessageReceived(message)) {
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getAudio().getFileId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            Core.getInstance().bus().post(new FileReceived(DocTypes.Audio, message.getAudio(), fileLocal));
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            Core.getInstance().bus().post(new MessageReceived(message, messageLocal));
            showMessageNotification(message, "Audio");
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onVideoMessage(final Notifications.VideoMessageNotification mcn) {
        Log.d("Aseman", "Received video message notification");
        final Entities.VideoMessage message = mcn.getMessage();
        message.setSeenByMe(false);
        if (DatabaseHelper.notifyVideoMessageReceived(message)) {
            Entities.FileLocal fileLocal = new Entities.FileLocal();
            fileLocal.setFileId(message.getVideo().getFileId());
            fileLocal.setPath("");
            fileLocal.setProgress(0);
            fileLocal.setTransferring(false);
            Core.getInstance().bus().post(new FileReceived(DocTypes.Video, message.getVideo(), fileLocal));
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            Core.getInstance().bus().post(new MessageReceived(message, messageLocal));
            showMessageNotification(message, "Video");
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onServiceMessage(final Notifications.ServiceMessageNotification mcn) {
        Log.d("Aseman", "Received service message notification");
        final Entities.ServiceMessage message = mcn.getMessage();
        message.setSeenByMe(false);
        if (DatabaseHelper.notifyServiceMessageReceived(message)) {
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            Core.getInstance().bus().post(new MessageReceived(message, messageLocal));
            showMessageNotification(message, message.getText());
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void showMessageNotification(Entities.Message message, String text) {
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

    private void notifyServerNotifReceived(String notifId) {
        Packet packet = new Packet();
        Notifications.Notification notification = new Notifications.Notification();
        notification.setNotificationId(notifId);
        packet.setNotif(notification);
        Call<Packet> call = NetworkHelper.getRetrofit().create(NotifHandler.class).notifyNotifReceived(packet);
        NetworkHelper.requestServer(call, new ServerCallback() {
            @Override
            public void onRequestSuccess(Packet packet) { }
            @Override
            public void onServerFailure() { }
            @Override
            public void onConnectionFailure() { }
        });
    }
}