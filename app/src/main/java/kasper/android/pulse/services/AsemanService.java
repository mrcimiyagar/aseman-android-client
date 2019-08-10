package kasper.android.pulse.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.microsoft.signalr.GsonCore;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.JsonConverterType;
import com.microsoft.signalr.JsonHelper;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.ComplexProfileActivity;
import kasper.android.pulse.activities.RoomActivity;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.AsemanDB;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.helpers.PulseHelper;
import kasper.android.pulse.models.Tuple;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.BotProfileUpdating;
import kasper.android.pulse.models.extras.ComplexProfileUpdating;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.FileMessageSending;
import kasper.android.pulse.models.extras.ProgressRequestBody;
import kasper.android.pulse.models.extras.RoomProfileUpdating;
import kasper.android.pulse.models.extras.TextMessageSending;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.extras.UserProfileUpdating;
import kasper.android.pulse.models.extras.YoloBoundingBox;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.models.notifications.Notifications;
import kasper.android.pulse.retrofit.ComplexHandler;
import kasper.android.pulse.retrofit.FileHandler;
import kasper.android.pulse.retrofit.MessageHandler;
import kasper.android.pulse.retrofit.NotifHandler;
import kasper.android.pulse.retrofit.RobotHandler;
import kasper.android.pulse.retrofit.RoomHandler;
import kasper.android.pulse.retrofit.UserHandler;
import kasper.android.pulse.rxbus.notifications.BotProfileUpdated;
import kasper.android.pulse.rxbus.notifications.ComplexCreated;
import kasper.android.pulse.rxbus.notifications.ComplexProfileUpdated;
import kasper.android.pulse.rxbus.notifications.ComplexRemoved;
import kasper.android.pulse.rxbus.notifications.ConnectionStateChanged;
import kasper.android.pulse.rxbus.notifications.ContactCreated;
import kasper.android.pulse.rxbus.notifications.FileDownloaded;
import kasper.android.pulse.rxbus.notifications.FileReceived;
import kasper.android.pulse.rxbus.notifications.FileRegistered;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.FileUploading;
import kasper.android.pulse.rxbus.notifications.InviteResolved;
import kasper.android.pulse.rxbus.notifications.InviteCancelled;
import kasper.android.pulse.rxbus.notifications.InviteCreated;
import kasper.android.pulse.rxbus.notifications.MemberAccessUpdated;
import kasper.android.pulse.rxbus.notifications.MembershipCreated;
import kasper.android.pulse.rxbus.notifications.MessageDeleted;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSeen;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.rxbus.notifications.RoomCreated;
import kasper.android.pulse.rxbus.notifications.RoomProfileUpdated;
import kasper.android.pulse.rxbus.notifications.RoomRemoved;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import kasper.android.pulse.rxbus.notifications.UserProfileUpdated;
import kasper.android.pulse.rxbus.notifications.WorkerAdded;
import kasper.android.pulse.rxbus.notifications.WorkerRemoved;
import kasper.android.pulse.rxbus.notifications.WorkerUpdated;
import kasper.android.pulseframework.components.PulseView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class AsemanService extends IntentService {

    private static String connectionState;
    public static String getConnectionState() {
        return connectionState;
    }

    private HubConnection connection = HubConnectionBuilder.create(NetworkHelper.SERVER_IP + "NotificationsHub/")
            .withJsonConverter(JsonConverterType.JACKSON).useLogger().build();
    Timer timer = new Timer();
    Timer timer2 = new Timer();

    private static Entities.File currentUploadingFile;
    private static Call currentUploadingCall;
    private static Uploading currentUploading;

    private static Entities.File currentDownloadingFile;
    private static Call currentDownloadingCall;
    private static Downloading currentDownloading;

    private static final LinkedBlockingDeque<Tuple<Entities.File, Entities.FileLocal, Entities.Message
            , Entities.MessageLocal, Uploading>> uploadingFiles = new LinkedBlockingDeque<>();
    private static final Hashtable<Long, ProgressRequestBody> uploadingFileParts = new Hashtable<>();
    private static Thread uploaderThread;

    private static final LinkedBlockingDeque<Downloading> downloadingFiles = new LinkedBlockingDeque<>();
    private static Thread downloaderThread;

    private static boolean skipDownload = false;
    private static boolean skipUpload = false;

    private static Thread txtMsgSenderThread;
    private static Call<Packet> currentTextCall;
    private static Call<Packet> currentFileCall;
    private static long currentTextMessageId = 0;
    private static long currentFileMessageId = 0;
    private static TextMessageSending txtMsgSending;
    private static FileMessageSending fileMsgSending;
    private static final LinkedBlockingDeque<TextMessageSending> txtMsgQueue = new LinkedBlockingDeque<>();
    private static final LinkedBlockingDeque<FileMessageSending> fileMsgQueue = new LinkedBlockingDeque<>();

    private static LinkedBlockingDeque<UserProfileUpdating> userQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<BotProfileUpdating> botQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<ComplexProfileUpdating> complexQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<RoomProfileUpdating> roomQueue = new LinkedBlockingDeque<>();

    private static Thread userThread, complexThread, roomThread, botThread;
    private static Hashtable<Long, Object> profileObjectsMap = new Hashtable<>();

    private final static Object TXT_SENDING_LOCK = new Object(), FILE_SENDING_LOCK = new Object();

    private boolean alive = false;
    private boolean started = false;

    public AsemanService() {
        super("AsemanService");
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
        connection.on("NotifyMemberAccessUpdated", this::onMemberAccessUpdated, Notifications.MemberAccessUpdatedNotification.class);
        connection.on("NotifyBotPropertiesChanged", this::onBotPropertiesChanged, Notifications.BotPropertiesChangedNotification.class);
        connection.on("NotifyRoomCreated", this::onRoomCreated, Notifications.RoomCreationNotification.class);
        connection.on("NotifyImageAnalyzed", this::onImageAnalyzed, Notifications.ImageAnalyzedNotification.class);
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

        if (started && intent.getExtras() != null && intent.getExtras().containsKey("reset") && intent.getExtras().getBoolean("reset")) {
            uploadingFiles.clear();
            List<Uploading> uploadings = DatabaseHelper.fetchUploadings();
            for (Uploading uploading : uploadings)
                DatabaseHelper.deleteUploadingById(uploading.getUploadingId());
            downloadingFiles.clear();
            List<Downloading> downloadings = DatabaseHelper.fetchDownloadings();
            for (Downloading downloading : downloadings)
                DatabaseHelper.deleteDownloadingById(downloading.getDownloadingId());
            txtMsgQueue.clear();
            for (TextMessageSending sending : DatabaseHelper.getTextMessageSendings())
                DatabaseHelper.notifyTextMessageSendingDeleted(sending.getSendingId());
            fileMsgQueue.clear();
            for (FileMessageSending sending : DatabaseHelper.getFileMessageSendings())
                DatabaseHelper.notifyFileMessageSendingDeleted(sending.getSendingId());
            new Thread(() -> {
                connectionState = "Connecting";
                Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Connecting));
                try {
                    connection.stop().blockingAwait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    connection.start().blockingAwait();
                    loginToHub();
                    connectionState = "Online";
                    Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Connected));
                }
            }).start();
        }

        if (!started) {

            started = true;
            alive = true;

            LogHelper.log(getClass().getName(), "Background service started.");

            Core.getInstance().bus().register(this);

            uploadingFiles.clear();
            List<Uploading> uploadings = DatabaseHelper.fetchUploadings();
            for (Uploading uploading : uploadings) {
                Entities.File file = DatabaseHelper.getFileById(uploading.getFileId());
                Entities.FileLocal fileLocal = DatabaseHelper.getFileLocalByFileId(uploading.getFileId());
                if (file != null && fileLocal != null) {
                    uploadingFiles.offer(new Tuple<>(file, fileLocal,
                            DatabaseHelper.getMessageById(uploading.getMessageId()),
                            DatabaseHelper.getMessageLocalById(uploading.getMessageId()),
                            uploading));
                } else {
                    DatabaseHelper.deleteUploadingById(uploading.getUploadingId());
                }
            }

            if (uploaderThread == null) {
                uploaderThread = new Thread(() -> {
                    try {
                        while (alive) {
                            try {
                                Thread.sleep(5000);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            Tuple<Entities.File, Entities.FileLocal, Entities.Message
                                    , Entities.MessageLocal, Uploading> tuple = uploadingFiles.take();
                            uploadingFiles.offerFirst(tuple);
                            Entities.File fileEntity = tuple.first;
                            Entities.FileLocal fileLocalEntity = tuple.second;
                            Entities.Message messageEntity = tuple.third;
                            currentUploading = tuple.fifth;
                            currentUploadingFile = fileEntity;
                            try {
                                Call<Packet> call = null;
                                if (fileEntity.getFileId() < 0) {
                                    if (fileEntity instanceof Entities.Photo) {
                                        Entities.Photo photo = (Entities.Photo) fileEntity;
                                        if (currentUploading.isCompress())
                                            compressImage(currentUploading.getPath());
                                        Map<String, RequestBody> parts = new HashMap<>();
                                        parts.put("ComplexId", createRequestBody(currentUploading.getComplexId()));
                                        parts.put("RoomId", createRequestBody(currentUploading.getRoomId()));
                                        parts.put("Width", createRequestBody(photo.getWidth()));
                                        parts.put("Height", createRequestBody(photo.getHeight()));
                                        parts.put("IsAvatar", createRequestBody(currentUploading.isCompress()));
                                        File file = currentUploading.isCompress() ? new File(
                                                new File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir)
                                                , "uploadTemp") : new File(currentUploading.getPath());
                                        if (file.exists()) {
                                            call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadPhoto(parts);
                                        } else
                                            Core.getInstance().bus().post(new FileUploaded(DocTypes.Photo
                                                    , 0, -1
                                                    , currentUploading.getComplexId(), currentUploading.getRoomId(), fileEntity, messageEntity));
                                    } else if (fileEntity instanceof Entities.Audio) {
                                        Entities.Audio audio = (Entities.Audio) fileEntity;
                                        Map<String, RequestBody> parts = new HashMap<>();
                                        parts.put("ComplexId", createRequestBody(currentUploading.getComplexId()));
                                        parts.put("RoomId", createRequestBody(currentUploading.getRoomId()));
                                        parts.put("Title", createRequestBody(audio.getTitle()));
                                        parts.put("Duration", createRequestBody(audio.getDuration()));
                                        File file = new File(currentUploading.getPath());
                                        if (file.exists()) {
                                            call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadAudio(parts);
                                        } else
                                            Core.getInstance().bus().post(new FileUploaded(DocTypes.Photo
                                                    , 0, -1
                                                    , currentUploading.getComplexId(), currentUploading.getRoomId(), fileEntity, messageEntity));
                                    } else if (fileEntity instanceof Entities.Video) {
                                        Entities.Video video = (Entities.Video) fileEntity;
                                        Map<String, RequestBody> parts = new HashMap<>();
                                        parts.put("ComplexId", createRequestBody(currentUploading.getComplexId()));
                                        parts.put("RoomId", createRequestBody(currentUploading.getRoomId()));
                                        parts.put("Title", createRequestBody(video.getTitle()));
                                        parts.put("Duration", createRequestBody(video.getDuration()));
                                        File file = new File(currentUploading.getPath());
                                        if (file.exists()) {
                                            call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadVideo(parts);
                                        } else
                                            Core.getInstance().bus().post(new FileUploaded(DocTypes.Photo
                                                    , 0, -1
                                                    , currentUploading.getComplexId(), currentUploading.getRoomId(), fileEntity, messageEntity));
                                    }
                                    currentUploadingCall = call;
                                }
                                Entities.File uploadedFile = null;
                                Entities.FileUsage createdFileUsage = null;
                                final long localFileId = fileEntity.getFileId();
                                if (call != null) {
                                    Packet packet = call.execute().body();
                                    if (packet != null) {
                                        if (packet.getStatus().equals("success")) {
                                            uploadedFile = packet.getFile();
                                            createdFileUsage = packet.getFileUsage();
                                            fileEntity.setFileId(uploadedFile.getFileId());
                                            DatabaseHelper.notifyFileRegistered(localFileId, uploadedFile.getFileId());
                                            fileRegisteredInternal(localFileId, uploadedFile.getFileId());
                                            Core.getInstance().bus().post(new FileRegistered(uploadedFile instanceof Entities.Photo
                                                    ? DocTypes.Photo : uploadedFile instanceof Entities.Audio ? DocTypes.Audio
                                                    : DocTypes.Video, localFileId, uploadedFile.getFileId()));
                                            if (currentUploading.isAttachToMessage() && messageEntity != null) {
                                                DatabaseHelper.notifyUpdateMessageAfterFileUpload(messageEntity.getMessageId()
                                                        , uploadedFile.getFileId(), createdFileUsage.getFileUsageId());
                                            }
                                        }
                                    }
                                } else {
                                    uploadedFile = fileEntity;
                                    List<Entities.FileUsage> fileUsages = DatabaseHelper.getFileUsages(uploadedFile.getFileId());
                                    createdFileUsage = fileUsages.size() > 0 ? fileUsages.get(0) : null;
                                }
                                currentUploadingFile = uploadedFile;

                                if (uploadedFile != null) {

                                    if (skipUpload) {
                                        DatabaseHelper.deleteUploadingById(currentUploading.getUploadingId());
                                        uploadingFileParts.remove(currentUploading.getUploadingId());
                                        uploadingFiles.take();
                                        continue;
                                    }

                                    currentUploading.setFileId(uploadedFile.getFileId());
                                    DatabaseHelper.notifyUploadingUpdated(currentUploading);

                                    Map<String, RequestBody> partMap = new HashMap<>();
                                    partMap.put("FileId", createRequestBody(uploadedFile.getFileId()));
                                    Pair<MultipartBody.Part, ProgressRequestBody> filePartPair = createFileBody(
                                            currentUploadingFile.getFileId(), currentUploadingFile instanceof Entities.Photo ?
                                                    DocTypes.Photo : currentUploadingFile instanceof Entities.Audio ?
                                                    DocTypes.Audio : DocTypes.Video, fileLocalEntity.getPath());
                                    MultipartBody.Part filePart = filePartPair.first;
                                    ProgressRequestBody prb = filePartPair.second;

                                    uploadingFileParts.put(currentUploading.getUploadingId(), prb);

                                    Call<Packet> writeCall = NetworkHelper.getRetrofit().create(FileHandler.class)
                                            .writeToFile(partMap, filePart);

                                    currentUploadingCall = writeCall;

                                    Packet writeResPacket = null;

                                    try {
                                        writeResPacket = writeCall.execute().body();
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }

                                    if (writeResPacket != null) {
                                        if (writeResPacket.getStatus().equals("success")) {
                                            DatabaseHelper.notifyFileUploaded(uploadedFile.getFileId());
                                            fileUploadedInternal(uploadedFile instanceof
                                                    Entities.Photo ? DocTypes.Photo : uploadedFile instanceof Entities.Audio
                                                    ? DocTypes.Audio : DocTypes.Video, uploadedFile.getFileId(), messageEntity
                                                    , uploadedFile, currentUploading.getComplexId(), currentUploading.getRoomId());
                                            Core.getInstance().bus().post(new FileUploaded(uploadedFile instanceof
                                                    Entities.Photo ? DocTypes.Photo : uploadedFile instanceof Entities.Audio
                                                    ? DocTypes.Audio : DocTypes.Video, uploadedFile.getFileId()
                                                    , createdFileUsage == null ? -1 : createdFileUsage.getFileUsageId()
                                                    , currentUploading.getComplexId(), currentUploading.getRoomId(), uploadedFile, messageEntity));
                                            DatabaseHelper.deleteUploadingById(currentUploading.getUploadingId());
                                            uploadingFileParts.remove(currentUploading.getUploadingId());
                                            uploadingFiles.take();
                                        }
                                    } else {
                                        DatabaseHelper.deleteUploadingById(currentUploading.getUploadingId());
                                        uploadingFileParts.remove(currentUploading.getUploadingId());
                                        uploadingFiles.take();
                                    }
                                } else {
                                    DatabaseHelper.deleteUploadingById(currentUploading.getUploadingId());
                                    uploadingFileParts.remove(currentUploading.getUploadingId());
                                    uploadingFiles.take();
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            if (!uploaderThread.isAlive())
                uploaderThread.start();

            downloadingFiles.clear();
            List<Downloading> downloadings = DatabaseHelper.fetchDownloadings();
            for (Downloading downloading : downloadings)
                downloadingFiles.offer(downloading);

            if (downloaderThread == null) {
                downloaderThread = new Thread(() -> {
                    try {
                        while (alive) {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            currentDownloading = downloadingFiles.take();
                            downloadingFiles.offerFirst(currentDownloading);
                            Entities.File df = DatabaseHelper.getFileById(currentDownloading.getFileId());
                            currentDownloadingFile = df;
                            skipDownload = false;
                            FileHandler fileHandler = NetworkHelper.getRetrofit().create(FileHandler.class);

                            long offset = 0;
                            File file = new File(DatabaseHelper.getFilePath(currentDownloading.getFileId()));
                            if (file.exists()) offset = file.length();

                            Call<ResponseBody> call = fileHandler.downloadFile(currentDownloading.getFileId(), offset);
                            currentDownloadingCall = call;
                            ResponseBody body = null;

                            try {
                                body = call.execute().body();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            try {
                                if (body != null) {
                                    writeResponseBodyToDisk(body, currentDownloading.getFileId(), currentDownloading);
                                }
                                if (!skipDownload) {
                                    DatabaseHelper.notifyFileDownloaded(currentDownloading.getFileId());
                                    Core.getInstance().bus().post(new FileDownloaded(df
                                            instanceof Entities.Photo ? DocTypes.Photo : df
                                            instanceof Entities.Audio ? DocTypes.Audio : DocTypes.Video, currentDownloading.getFileId()));
                                } else
                                    skipDownload = false;

                                DatabaseHelper.deleteDownloadingById(currentDownloading.getDownloadingId());

                                downloadingFiles.take();

                            } catch (Exception ex) {
                                ex.printStackTrace();
                                Core.getInstance().bus().post(new ShowToast("File download failed."));
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            if (!downloaderThread.isAlive())
                downloaderThread.start();

            Core.getInstance().bus().register(this);
            alive = true;
            if (txtMsgSenderThread == null) {

                txtMsgQueue.clear();
                for (TextMessageSending s : DatabaseHelper.getTextMessageSendings())
                    txtMsgQueue.offer(s);

                txtMsgSenderThread = new Thread(() -> {
                    try {
                        while (alive) {
                            txtMsgSending = txtMsgQueue.take();
                            txtMsgQueue.offerFirst(txtMsgSending);
                            synchronized (TXT_SENDING_LOCK) {
                                try {
                                    final Entities.Message message = DatabaseHelper.getMessageById(txtMsgSending.getMessageId());
                                    final long messageLocalId = txtMsgSending.getMessageId();
                                    currentTextMessageId = messageLocalId;

                                    if (message != null) {
                                        final Packet packet = new Packet();
                                        Entities.Complex complex = new Entities.Complex();
                                        complex.setComplexId(txtMsgSending.getComplexId());
                                        packet.setComplex(complex);
                                        Entities.Room room = new Entities.Room();
                                        room.setRoomId(txtMsgSending.getRoomId());
                                        packet.setBaseRoom(room);
                                        packet.setTextMessage((Entities.TextMessage) message);
                                        MessageHandler messageHandler = NetworkHelper.getRetrofit().create(MessageHandler.class);
                                        currentTextCall = messageHandler.createTextMessage(packet);
                                        Packet p = null;

                                        try {
                                            p = currentTextCall.execute().body();
                                        } catch (Exception ex) {
                                            ex.printStackTrace();
                                        }

                                        if (p != null) {
                                            final Entities.TextMessage msg = p.getTextMessage();
                                            if (p.getTextMessage() != null) {
                                                DatabaseHelper.notifyTextMessageSent(messageLocalId, msg.getMessageId(), msg.getTime());
                                                if (DatabaseHelper.getComplexById(txtMsgSending.getComplexId()).getMode() == 1) {
                                                    msg.setSeenCount(1);
                                                    DatabaseHelper.notifyMessageUpdated(msg);
                                                }
                                                Core.getInstance().bus().post(new MessageSent(messageLocalId, msg.getMessageId()));
                                            }

                                            DatabaseHelper.notifyTextMessageSendingDeleted(txtMsgSending.getSendingId());
                                            txtMsgQueue.take();
                                        }
                                    } else {
                                        DatabaseHelper.notifyTextMessageSendingDeleted(txtMsgSending.getSendingId());
                                        txtMsgQueue.take();
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            if (!txtMsgSenderThread.isAlive())
                txtMsgSenderThread.start();

            fileMsgQueue.clear();
            for (FileMessageSending s : DatabaseHelper.getFileMessageSendings())
                fileMsgQueue.offer(s);

            if (userThread == null) {
                userThread = new Thread(() -> {
                    try {
                        while (alive) {
                            UserProfileUpdating updating = userQueue.take();
                            userQueue.offerFirst(updating);
                            try {
                                Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData =
                                        uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                                , -1, -1, true, false));
                                profileObjectsMap.put(uploadData.first.getFileId(), updating.getUser());

                                userQueue.take();

                            } catch (Exception ex) {
                                ex.printStackTrace();
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
                            botQueue.offerFirst(updating);
                            try {
                                Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData =
                                        uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                                , -1, -1, true, false));
                                profileObjectsMap.put(uploadData.first.getFileId(), updating.getBot());

                                botQueue.take();

                            } catch (Exception ex) {
                                ex.printStackTrace();
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
                            complexQueue.offerFirst(updating);
                            try {
                                Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData =
                                        uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                                , -1, -1, true, false));
                                profileObjectsMap.put(uploadData.first.getFileId(), updating.getComplex());

                                complexQueue.take();

                            } catch (Exception ex) {
                                ex.printStackTrace();
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
                            roomQueue.offerFirst(updating);
                            try {
                                Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData =
                                        uploadFile(new Uploading(DocTypes.Photo, updating.getPath()
                                        , -1, -1, true, false));
                                profileObjectsMap.put(uploadData.first.getFileId(), updating.getRoom());

                                roomQueue.take();

                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
            }

            if (!roomThread.isAlive())
                roomThread.start();

            connectionState = "Preparing";
            started = true;
            LogHelper.log("KasperLogger", "Notification service started");

            connectionState = "Connecting";
            Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Connecting));

            new Thread(() -> {
                try {
                    connection.start().blockingAwait();
                    loginToHub();
                    connectionState = "Online";
                    Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Connected));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (connection.getConnectionState() == HubConnectionState.DISCONNECTED) {
                            connectionState = "Connecting";
                            Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Connecting));
                            try {
                                connection.start().blockingAwait();
                                loginToHub();
                                connectionState = "Online";
                                Core.getInstance().bus().post(new ConnectionStateChanged(ConnectionStateChanged.State.Connected));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }, 1000, 10000);
                timer2.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (connection.getConnectionState() == HubConnectionState.CONNECTED) {
                            try {
                                LogHelper.log("Aseman", "sending keep-alive");
                                String authResult1 = connection.invoke(String.class
                                        , "KeepAlive").blockingGet();
                                LogHelper.log("Aseman", "received keep-alive answer : " + authResult1);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }, 1000, 10000);
            }).start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.log(getClass().getName(), "Background service destroyed.");
        Core.getInstance().bus().unregister(this);
        super.onDestroy();
    }

    private void loginToHub() {
        Entities.Session session = DatabaseHelper.getSingleSession();
        if (session != null) {
            LogHelper.log(getClass().getName(), "Logging in to hub " + session.getSessionId() + " " + session.getToken());
            String result = connection.invoke(String.class, "Login", session.getSessionId(), session.getToken())
                    .doOnError(Throwable::printStackTrace).blockingGet();
            LogHelper.log(getClass().getName(), result.equals("success") ? "Logged in to hub successfully." : "Hub login attempt failure.");
        }
    }

    public static Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadFile(Uploading uploading) {
        Entities.File fileEntity;
        Entities.FileLocal fileLocalEntity;
        Entities.Message messageEntity;
        Entities.MessageLocal messageLocalEntity;
        if (uploading.getDocTypeEnum() == DocTypes.Photo) {
            try {
                FileInputStream inputStream = new FileInputStream(new File(uploading.getPath()));
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
                int imageWidth = bitmapOptions.outWidth;
                int imageHeight = bitmapOptions.outHeight;
                inputStream.close();
                Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper
                        .notifyPhotoUploading(false, uploading.getPath()
                                , imageWidth, imageHeight);
                fileEntity = filePair.first;
                fileLocalEntity = filePair.second;
                currentUploadingFile = fileEntity;
                Core.getInstance().bus().post(new FileUploading(DocTypes.Photo, fileEntity, fileLocalEntity));
                if (uploading.isAttachToMessage()) {
                    Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper
                            .notifyPhotoMessageSending(uploading.getRoomId(), fileEntity.getFileId());
                    messageEntity = msgPair.first;
                    messageLocalEntity = msgPair.second;
                } else {
                    messageEntity = null;
                    messageLocalEntity = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                fileEntity = null;
                fileLocalEntity = null;
                messageEntity = null;
                messageLocalEntity = null;
            }
        } else if (uploading.getDocTypeEnum() == DocTypes.Audio) {
            Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper
                    .notifyAudioUploading(false, uploading.getPath()
                            , new File(uploading.getPath()).getName(), 60000);
            fileEntity = filePair.first;
            fileLocalEntity = filePair.second;
            currentUploadingFile = fileEntity;
            Core.getInstance().bus().post(new FileUploading(DocTypes.Audio, fileEntity, fileLocalEntity));
            if (uploading.isAttachToMessage()) {
                Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper
                        .notifyAudioMessageSending(uploading.getRoomId(), fileEntity.getFileId());
                messageEntity = msgPair.first;
                messageLocalEntity = msgPair.second;
            } else {
                messageEntity = null;
                messageLocalEntity = null;
            }
        } else if (uploading.getDocTypeEnum() == DocTypes.Video) {
            Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper
                    .notifyVideoUploading(false, uploading.getPath()
                            , new File(uploading.getPath()).getName(), 60000);
            fileEntity = filePair.first;
            fileLocalEntity = filePair.second;
            currentUploadingFile = fileEntity;
            Core.getInstance().bus().post(new FileUploading(DocTypes.Video, fileEntity, fileLocalEntity));
            if (uploading.isAttachToMessage()) {
                Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper
                        .notifyVideoMessageSending(uploading.getRoomId(), fileEntity.getFileId());
                messageEntity = msgPair.first;
                messageLocalEntity = msgPair.second;
            } else {
                messageEntity = null;
                messageLocalEntity = null;
            }
        } else {
            fileEntity = null;
            fileLocalEntity = null;
            messageEntity = null;
            messageLocalEntity = null;
        }
        if (messageEntity != null)
            uploading.setMessageId(messageEntity.getMessageId());
        if (fileEntity != null) {
            uploading.setFileId(fileEntity.getFileId());
            DatabaseHelper.notifyUploadingCreated(uploading);
            uploadingFiles.offer(new Tuple<>(fileEntity, fileLocalEntity, messageEntity, messageLocalEntity, uploading));
        }
        return new Tuple<>(fileEntity, fileLocalEntity, messageEntity, messageLocalEntity, uploading);
    }

    public static void downloadFile(Downloading downloading) {
        DatabaseHelper.notifyFileDownloading(downloading.getFileId());
        DatabaseHelper.notifyDownloadingCreated(downloading);
        downloadingFiles.offer(downloading);
    }

    public static void cancelUpload(long fileId) {
        synchronized (uploadingFiles) {
            if (currentUploadingFile != null && currentUploadingFile.getFileId() == fileId) {
                try {
                    for (Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData : uploadingFiles)
                        if (uploadData.fifth.getUploadingId() == currentUploading.getUploadingId()) {
                            uploadingFiles.remove(uploadData);
                            break;
                        }
                    DatabaseHelper.deleteUploadingById(currentUploading.getUploadingId());
                    currentUploadingCall.cancel();
                    ProgressRequestBody prb = uploadingFileParts.get(currentUploading.getUploadingId());
                    if (prb != null) prb.cancelStream();
                } catch (NullPointerException ex) {
                    ex.printStackTrace();
                }
            } else {
                for (Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> tuple : uploadingFiles) {
                    if (tuple.first.getFileId() == fileId) {
                        DatabaseHelper.deleteUploadingById(tuple.fifth.getUploadingId());
                        uploadingFiles.remove(tuple);
                        break;
                    }
                }
            }
        }
    }

    public static void cancelDownload(long fileId) {
        synchronized (downloadingFiles) {
            if (currentDownloadingFile != null && currentDownloadingFile.getFileId() == fileId) {
                skipDownload = true;
                downloadingFiles.remove(currentDownloading);
                DatabaseHelper.deleteDownloadingById(currentDownloading.getDownloadingId());
                currentDownloadingCall.cancel();
            } else {
                for (Downloading downloading : downloadingFiles) {
                    if (downloading.getFileId() == fileId) {
                        DatabaseHelper.deleteDownloadingById(downloading.getDownloadingId());
                        downloadingFiles.remove(downloading);
                        break;
                    }
                }
            }
        }
    }

    public static void cancelTextMessage(long messageId) {
        new Thread(() -> {
            boolean done = false;
            if (currentTextCall != null) {
                synchronized (TXT_SENDING_LOCK) {
                    if (currentTextMessageId == messageId) {
                        txtMsgQueue.remove(txtMsgSending);
                        currentTextCall.cancel();
                        DatabaseHelper.notifyTextMessageSendingDeleted(txtMsgSending.getSendingId());
                        done = true;
                    }
                }
            }
            if (!done) {
                synchronized (txtMsgQueue) {
                    for (TextMessageSending tms : txtMsgQueue) {
                        if (tms.getMessageId() == messageId) {
                            DatabaseHelper.notifyTextMessageSendingDeleted(tms.getSendingId());
                            txtMsgQueue.remove(tms);
                        }
                    }
                }
            }
        }).start();
    }

    public static void cancelFileMessage(long messageId) {
        Entities.Message msg = DatabaseHelper.getMessageById(messageId);
        if (msg instanceof Entities.PhotoMessage)
            DatabaseHelper.deletePhotoMessage(messageId);
        else if (msg instanceof Entities.AudioMessage)
            DatabaseHelper.deleteAudioMessage(messageId);
        else if (msg instanceof Entities.VideoMessage)
            DatabaseHelper.deleteVideoMessage(messageId);
        new Thread(() -> {
            boolean done = false;
            if (currentFileCall != null) {
                synchronized (FILE_SENDING_LOCK) {
                    if (currentFileMessageId == messageId) {
                        fileMsgQueue.remove(fileMsgSending);
                        currentFileCall.cancel();
                        DatabaseHelper.notifyFileMessageSendingDeleted(fileMsgSending.getSendingId());
                        done = true;
                    }
                }
            }
            if (!done) {
                synchronized (fileMsgQueue) {
                    for (FileMessageSending fms : fileMsgQueue) {
                        if (fms.getMessageId() == messageId) {
                            DatabaseHelper.notifyFileMessageSendingDeleted(fms.getSendingId());
                            fileMsgQueue.remove(fms);
                        }
                    }
                }
            }
        }).start();
        Core.getInstance().bus().post(new MessageDeleted(msg));
    }

    public static void enqueueMessage(TextMessageSending sending) {
        Pair<Entities.Message, Entities.MessageLocal> pair = DatabaseHelper
                .notifyTextMessageSending(sending.getRoomId(), sending.getText());
        sending.setMessageId(pair.first.getMessageId());
        DatabaseHelper.notifyMessageSendingCreated(sending);
        txtMsgQueue.offer(sending);
        Core.getInstance().bus().post(new MessageSending(pair.first, pair.second));
    }

    public static void enqueueMessage(FileMessageSending sending) {
        DatabaseHelper.notifyMessageSendingCreated(sending);
        fileMsgQueue.offer(sending);
        Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> uploadData =
                uploadFile(new Uploading(sending.getDocTypeEnum(), sending.getPath()
                , sending.getComplexId(), sending.getRoomId(), false, true));
        Core.getInstance().bus().post(new MessageSending(uploadData.third, uploadData.fourth));
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

    private void fileRegisteredInternal(long localFileId, long onlineFileId) {
        Object obj = profileObjectsMap.get(localFileId);
        if (obj != null) {
            profileObjectsMap.put(onlineFileId, obj);
            profileObjectsMap.remove(localFileId);
        }
    }

    private void fileUploadedInternal(DocTypes docType, long onlineFileId, Entities.Message finalMessage
            , Entities.File finalFile, long complexId, long roomId) {
        new Thread(() -> {
            try {
                if (finalMessage != null && finalFile != null) {
                    fileMsgSending = fileMsgQueue.peek();
                    synchronized (FILE_SENDING_LOCK) {
                        currentFileMessageId = finalMessage.getMessageId();
                        Packet packet = new Packet();
                        Entities.Complex complex = new Entities.Complex();
                        complex.setComplexId(complexId);
                        packet.setComplex(complex);
                        Entities.Room room = new Entities.Room();
                        room.setRoomId(roomId);
                        packet.setBaseRoom(room);
                        finalFile.setFileId(onlineFileId);
                        packet.setFile(finalFile);
                        MessageHandler messageHandler = NetworkHelper.getRetrofit().create(MessageHandler.class);
                        currentFileCall = messageHandler.createFileMessage(packet);
                        Packet p = currentFileCall.execute().body();
                        if (p != null) {
                            long messageId = -1;
                            long time;
                            Entities.Message msg;
                            if (docType == DocTypes.Photo) {
                                msg = p.getPhotoMessage();
                                messageId = msg.getMessageId();
                                time = p.getPhotoMessage().getTime();
                                DatabaseHelper.notifyPhotoMessageSent(finalMessage.getMessageId(), messageId, time);
                            } else if (docType == DocTypes.Audio) {
                                msg = p.getAudioMessage();
                                messageId = msg.getMessageId();
                                time = p.getAudioMessage().getTime();
                                DatabaseHelper.notifyAudioMessageSent(finalMessage.getMessageId(), messageId, time);
                            } else if (docType == DocTypes.Video) {
                                msg = p.getVideoMessage();
                                messageId = msg.getMessageId();
                                time = p.getVideoMessage().getTime();
                                DatabaseHelper.notifyVideoMessageSent(finalMessage.getMessageId(), messageId, time);
                            }
                            if (DatabaseHelper.getComplexById(complex.getComplexId()).getMode() == 1) {
                                finalMessage.setSeenCount(1);
                                if (finalMessage instanceof Entities.TextMessage)
                                    DatabaseHelper.notifyTextMessageSeen(finalMessage.getMessageId(), finalMessage.getSeenCount());
                                else if (finalMessage instanceof Entities.PhotoMessage)
                                    DatabaseHelper.notifyPhotoMessageSeen(finalMessage.getMessageId(), finalMessage.getSeenCount());
                                else if (finalMessage instanceof Entities.AudioMessage)
                                    DatabaseHelper.notifyAudioMessageSeen(finalMessage.getMessageId(), finalMessage.getSeenCount());
                                else if (finalMessage instanceof Entities.VideoMessage)
                                    DatabaseHelper.notifyVideoMessageSeen(finalMessage.getMessageId(), finalMessage.getSeenCount());
                                else if (finalMessage instanceof Entities.ServiceMessage)
                                    DatabaseHelper.notifyServiceMessageSeen(finalMessage.getMessageId(), finalMessage.getSeenCount());
                            }
                            try {
                                FileMessageSending sending = fileMsgQueue.take();
                                DatabaseHelper.notifyFileMessageSendingDeleted(sending.getSendingId());
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                            Core.getInstance().bus().post(new MessageSent(finalMessage.getMessageId(), messageId));
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();

        if (profileObjectsMap.containsKey(onlineFileId)) {
            Object object = profileObjectsMap.get(onlineFileId);
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
                packet.setBaseRoom(room);
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

    private void compressImage(String photoPath) {
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        float width, height;
        if (photoW > photoH)
        {
            width = photoW > 256 ? 256 : photoW;
            height = (float)photoH / (float)photoW * width;
        }
        else
        {
            height = photoH > 256 ? 256 : photoH;
            width = (float)photoW / (float)photoH * height;
        }

        int scaleFactor = 1;

        if ((width > 0) || (height > 0)) {
            scaleFactor = Math.min(photoW/(int)width, photoH/(int)height);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        Bitmap result = BitmapFactory.decodeFile(photoPath, bmOptions);

        File uploadTemp = new File(
                new File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir)
                , "uploadTemp");

        boolean safe = false;
        if (uploadTemp.exists()) {
            if (uploadTemp.delete()) {
                safe = true;
            }
        } else {
            safe = true;
        }

        if (safe) {
            try (FileOutputStream out = new FileOutputStream(uploadTemp)) {
                result.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Pair<MultipartBody.Part, ProgressRequestBody> createFileBody(long fileId, DocTypes docType, String filePath) {
        File file = new File(filePath);
        ProgressRequestBody prb = new ProgressRequestBody(fileId, docType, file, "*/*");
        return new Pair<>(MultipartBody.Part.createFormData("File", file.getName(), prb), prb);
    }

    private RequestBody createRequestBody(Object param) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), param.toString());
    }

    private void writeResponseBodyToDisk(ResponseBody body, long fileId, Downloading downloading) {
        try {
            File destFile = new File(new File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir), fileId + "");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSizeDownloaded = destFile.length();
                long fileSize = fileSizeDownloaded + body.contentLength();
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(destFile, true);
                long notifiedProgress = 0;
                Entities.File file = DatabaseHelper.getFileById(downloading.getFileId());
                while (!skipDownload) {
                    int read = inputStream.read(fileReader);
                    if (read == -1)
                        break;
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    int progress = (int) (fileSizeDownloaded * 100 / fileSize);
                    if (progress - notifiedProgress > 0) {
                        DatabaseHelper.notifyFileTransferProgressed(downloading.getFileId(), progress);
                        Core.getInstance().bus().post(new FileTransferProgressed(file
                                instanceof Entities.Photo ? DocTypes.Photo : file
                                instanceof Entities.Audio ? DocTypes.Audio : DocTypes.Video,
                                downloading.getFileId(), progress));
                        notifiedProgress = progress;
                    }
                }
                outputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onImageAnalyzed(Notifications.ImageAnalyzedNotification notif) {
        LogHelper.log("Aseman", "Received Image Analyzed notification");

        long fileId = notif.getFileId();
        List<YoloBoundingBox> boxes = notif.getBoxes();

        for (YoloBoundingBox box : boxes) {
            box.setImageId(fileId);
            DatabaseHelper.notifyYoloBoundingBoxCreated(box);
        }

        notifyServerNotifReceived(notif.getNotificationId());
    }

    public void onRoomCreated(Notifications.RoomCreationNotification notif) {
        LogHelper.log("Aseman", "Received Room Creation notification");

        if (notif.getSingleRoom() != null) {
            Entities.User me = DatabaseHelper.getMe();
            if (me != null) {
                if (notif.getSingleRoom().getUser1().getBaseUserId() != me.getBaseUserId()) {
                    Log.d("KasperLogger", "test 1");
                    if (notif.getSingleRoom().getUser1() instanceof Entities.User) {
                        Log.d("KasperLogger", "test 2");
                        DatabaseHelper.notifyUserCreated((Entities.User)(notif.getSingleRoom().getUser1()));
                    } else if (notif.getSingleRoom().getUser1() instanceof Entities.Bot) {
                        Log.d("KasperLogger", "test 3");
                        DatabaseHelper.notifyBotCreated((Entities.Bot)(notif.getSingleRoom().getUser1()), null);
                    }
                } else if (notif.getSingleRoom().getUser2().getBaseUserId() != me.getBaseUserId()) {
                    Log.d("KasperLogger", "test 4");
                    if (notif.getSingleRoom().getUser2() instanceof Entities.User) {
                        Log.d("KasperLogger", "test 5");
                        DatabaseHelper.notifyUserCreated((Entities.User)(notif.getSingleRoom().getUser2()));
                    } else if (notif.getSingleRoom().getUser2() instanceof Entities.Bot) {
                        Log.d("KasperLogger", "test 6");
                        DatabaseHelper.notifyBotCreated((Entities.Bot)(notif.getSingleRoom().getUser2()), null);
                    }
                }
            }
            DatabaseHelper.notifyRoomCreated(notif.getSingleRoom());
            Core.getInstance().bus().post(new RoomCreated(notif.getSingleRoom().getComplex().getComplexId(), notif.getSingleRoom()));
        } else {
            DatabaseHelper.notifyRoomCreated(notif.getRoom());
            Core.getInstance().bus().post(new RoomCreated(notif.getRoom().getComplex().getComplexId(), notif.getRoom()));
        }

        DatabaseHelper.notifyServiceMessageReceived(notif.getMessage());
        Entities.MessageLocal ml = new Entities.MessageLocal();
        ml.setMessageId(notif.getMessage().getMessageId());
        ml.setSent(true);
        Core.getInstance().bus().post(new MessageReceived(true, notif.getMessage(), ml));

        showMessageNotification(notif.getMessage(), notif.getMessage().getText());

        notifyServerNotifReceived(notif.getNotificationId());
    }

    public void onBotPropertiesChanged(Notifications.BotPropertiesChangedNotification notif) {
        LogHelper.log("Aseman", "Received Bot Properties Changed notification");

        Core.getInstance().bus().post(new WorkerUpdated(notif.getWorkership()));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    public void onMemberAccessUpdated(Notifications.MemberAccessUpdatedNotification notif) {
        LogHelper.log("Aseman", "Received Member Access Updated notification");

        DatabaseHelper.notifyMemberAccessCreated(notif.getMemberAccess());

        if (notif.getMemberAccess().isCanModifyAccess()) {
            Packet packet = new Packet();
            Entities.Complex complex = new Entities.Complex();
            complex.setComplexId(notif.getMemberAccess().getMembership().getComplex().getComplexId());
            packet.setComplex(complex);
            NetworkHelper.requestServer(NetworkHelper.getRetrofit().create(ComplexHandler.class).getComplexAccesses(packet)
                    , new ServerCallback() {
                        @Override
                        public void onRequestSuccess(Packet packet) {
                            for (Entities.MemberAccess ma : packet.getMemberAccesses())
                                DatabaseHelper.notifyMemberAccessCreated(ma);
                            Core.getInstance().bus().post(new MemberAccessUpdated(notif.getMemberAccess()));
                            notifyServerNotifReceived(notif.getNotificationId());
                        }
                        @Override
                        public void onServerFailure() {

                        }
                        @Override
                        public void onConnectionFailure() { }
                    });
        } else {
            Core.getInstance().bus().post(new MemberAccessUpdated(notif.getMemberAccess()));
            notifyServerNotifReceived(notif.getNotificationId());
        }
    }

    private void onBotAddedToRoom(Notifications.BotAddedToRoomNotification notif) {
        LogHelper.log("Aseman", "Received Bot Added notification");

        if (notif.getBot() != null)
            DatabaseHelper.notifyBotCreated(notif.getBot(), null);

        Core.getInstance().bus().post(new WorkerAdded(notif.getWorkership()));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotRemovedFromRoom(Notifications.BotRemovedFromRoomNotification notif) {
        LogHelper.log("Aseman", "Received Bot Removed notification");

        Core.getInstance().bus().post(new WorkerRemoved(notif.getWorkership()));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onMessageSeen(final Notifications.MessageSeenNotification msn) {
        LogHelper.log("Aseman", "Received Message Seen notification");
        Entities.Message message = DatabaseHelper.getMessageById(msn.getMessageId());
        if (message != null) {
            message.setSeenCount(msn.getMessageSeenCount());
            if (message instanceof Entities.TextMessage)
                DatabaseHelper.notifyTextMessageSeen(message.getMessageId(), message.getSeenCount());
            else if (message instanceof Entities.PhotoMessage)
                DatabaseHelper.notifyPhotoMessageSeen(message.getMessageId(), message.getSeenCount());
            else if (message instanceof Entities.AudioMessage)
                DatabaseHelper.notifyAudioMessageSeen(message.getMessageId(), message.getSeenCount());
            else if (message instanceof Entities.VideoMessage)
                DatabaseHelper.notifyVideoMessageSeen(message.getMessageId(), message.getSeenCount());
            else if (message instanceof Entities.ServiceMessage)
                DatabaseHelper.notifyServiceMessageSeen(message.getMessageId(), message.getSeenCount());
            Core.getInstance().bus().post(new MessageSeen(message));
        }

        notifyServerNotifReceived(msn.getNotificationId());
    }

    private void onInviteCreated(final Notifications.InviteCreationNotification icn) {
        LogHelper.log("Aseman", "Received Invite Creation notification");

        Entities.Invite invite = icn.getInvite();

        DatabaseHelper.notifyComplexCreated(invite.getComplex());
        DatabaseHelper.notifyUserCreated(invite.getUser());
        DatabaseHelper.notifyInviteReceived(invite);

        Core.getInstance().bus().post(new InviteCreated(invite));

        showInviteNotification(invite);

        notifyServerNotifReceived(icn.getNotificationId());
    }

    private void onInviteCancelled(final Notifications.InviteCancellationNotification icn) {
        LogHelper.log("Aseman", "Received Invite Cancellation notification");

        DatabaseHelper.notifyInviteResolved(icn.getInvite());

        Core.getInstance().bus().post(new InviteCancelled(icn.getInvite()));

        showNotification("Invite cancelled.",
                "Invite from [" + icn.getInvite().getComplex().getTitle() + "] complex cancelled.",
                null);

        notifyServerNotifReceived(icn.getNotificationId());
    }

    private void onInviteAccepted(final Notifications.InviteAcceptanceNotification ian) {
        LogHelper.log("Aseman", "Received Invite Acceptance notification");

        DatabaseHelper.notifyInviteResolved(ian.getInvite());
        Core.getInstance().bus().post(new InviteResolved(ian.getInvite()));

        notifyServerNotifReceived(ian.getNotificationId());
    }

    private void onInviteIgnored(final Notifications.InviteIgnoranceNotification iin) {
        LogHelper.log("Aseman", "Received Invite Ignorance notification");

        DatabaseHelper.notifyInviteResolved(iin.getInvite());
        Core.getInstance().bus().post(new InviteResolved(iin.getInvite()));

        notifyServerNotifReceived(iin.getNotificationId());
    }

    private void onUserJointComplex(final Notifications.UserJointComplexNotification ujcn) {
        LogHelper.log("Aseman", "Received User Joint Complex notification");

        Entities.Membership mem = ujcn.getMembership();

        DatabaseHelper.notifyUserCreated(mem.getUser());
        DatabaseHelper.notifyMembershipCreated(mem);
        DatabaseHelper.notifyServiceMessageReceived(ujcn.getMessage());
        if (mem.getMemberAccess() != null)
            DatabaseHelper.notifyMemberAccessCreated(mem.getMemberAccess());

        Core.getInstance().bus().post(new MembershipCreated(mem));
        Entities.MessageLocal ml = new Entities.MessageLocal();
        ml.setMessageId(ujcn.getMessage().getMessageId());
        ml.setSent(true);
        Core.getInstance().bus().post(new MessageReceived(true, ujcn.getMessage(), ml));

        showMessageNotification(ujcn.getMessage(), ujcn.getMessage().getText());

        notifyServerNotifReceived(ujcn.getNotificationId());
    }

    private void onComplexDeletion(final Notifications.ComplexDeletionNotification cdn) {
        LogHelper.log("Aseman", "Received Complex Deletion notification");

        showNotification("Complex deleted", "Complex [" +
                DatabaseHelper.getComplexById(cdn.getComplexId()).getTitle() + "] deleted.",
                null);

        DatabaseHelper.notifyComplexRemoved(cdn.getComplexId());
        Core.getInstance().bus().post(new ComplexRemoved(cdn.getComplexId()));

        notifyServerNotifReceived(cdn.getNotificationId());
    }

    private void onRoomDeletion(final Notifications.RoomDeletionNotification rdn) {
        LogHelper.log("Aseman", "Received Room Deletion notification");

        showNotification("Room deleted", "room [" +
                DatabaseHelper.getComplexById(rdn.getComplexId()).getTitle() + " : " +
                DatabaseHelper.getRoomById(rdn.getRoomId())+ "] deleted.", null);

        Entities.BaseRoom baseRoom = DatabaseHelper.getRoomById(rdn.getRoomId());
        DatabaseHelper.notifyRoomRemoved(rdn.getRoomId());
        Core.getInstance().bus().post(new RoomRemoved(baseRoom));

        notifyServerNotifReceived(rdn.getNotificationId());
    }

    private void onBotSentBotView(final Notifications.BotSentBotViewNotification notif) {
        LogHelper.log("Aseman", "Received BotView Init notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                PulseView pulseView = PulseHelper.getPulseViewTable().get(notif.getBotId());
                if (pulseView != null)
                    pulseView.buildUi(notif.getViewData());
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotUpdatedBotView(final Notifications.BotUpdatedBotViewNotification notif) {
        LogHelper.log("Aseman", "Received BotView Update notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                PulseView pulseView = PulseHelper.getPulseViewTable().get(notif.getBotId());
                if (pulseView != null) {
                    if (notif.isBatchData())
                        pulseView.updateBatchUi(notif.getUpdateData());
                    else
                        pulseView.updateUi(notif.getUpdateData());
                }
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotAnimatedBotView(final Notifications.BotAnimatedBotViewNotification notif) {
        LogHelper.log("Aseman", "Received BotView Animation notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                PulseView pulseView = PulseHelper.getPulseViewTable().get(notif.getBotId());
                if (pulseView != null) {
                    if (notif.isBatchData())
                        pulseView.animateBatchUi(notif.getAnimData());
                    else
                        pulseView.animateUi(notif.getAnimData());
                }
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onBotRanCommandsOnBotView(final Notifications.BotRanCommandsOnBotViewNotification notif) {
        LogHelper.log("Aseman", "Received BotView RunCommands notification");
        if (PulseHelper.getCurrentComplexId() == notif.getComplexId()
                && PulseHelper.getCurrentRoomId() == notif.getRoomId())
            Core.getInstance().bus().post(new UiThreadRequested(() -> {
                PulseView pulseView = PulseHelper.getPulseViewTable().get(notif.getBotId());
                if (pulseView != null) {
                    if (notif.isBatchData())
                        pulseView.runCommands(notif.getCommandsData());
                    else
                        pulseView.runCommand(notif.getCommandsData());
                }
            }));

        notifyServerNotifReceived(notif.getNotificationId());
    }

    private void onContactCreated(final Notifications.ContactCreationNotification ccn) {
        LogHelper.log("Aseman", "Received contact notification");

        Entities.Complex complex = ccn.getContact().getComplex();
        Entities.ComplexSecret complexSecret = ccn.getComplexSecret();
        Entities.BaseRoom room = complex.getAllRooms().get(0);
        room.setComplex(complex);
        Entities.User user = ccn.getContact().getUser();
        Entities.User peer = ccn.getContact().getPeer();

        DatabaseHelper.notifyComplexCreated(complex);
        DatabaseHelper.notifyComplexSecretCreated(complexSecret);
        DatabaseHelper.notifyRoomCreated(room);
        DatabaseHelper.notifyUserCreated(user);
        DatabaseHelper.notifyUserCreated(peer);
        for (Entities.Membership mem : complex.getMembers()) {
            DatabaseHelper.notifyUserCreated(mem.getUser());
            DatabaseHelper.notifyMembershipCreated(mem);
            if (mem.getMemberAccess() != null)
                DatabaseHelper.notifyMemberAccessCreated(mem.getMemberAccess());
        }
        DatabaseHelper.notifyContactCreated(ccn.getContact());

        Core.getInstance().bus().post(new ComplexCreated(complex));
        Core.getInstance().bus().post(new RoomCreated(complex.getComplexId(), room));
        Core.getInstance().bus().post(new ContactCreated(ccn.getContact()));

        handleServiceMessage(ccn.getMessage());

        notifyServerNotifReceived(ccn.getNotificationId());
    }

    private void onTextMessage(final Notifications.TextMessageNotification mcn) {
        LogHelper.log("Aseman", "Received text message notification");
        final Entities.TextMessage message = mcn.getMessage();
        message.setSeenByMe(false);
        if (DatabaseHelper.notifyTextMessageReceived(message)) {
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            Core.getInstance().bus().post(new MessageReceived(true, message, messageLocal));
            showMessageNotification(message, message.getText());
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onPhotoMessage(final Notifications.PhotoMessageNotification mcn) {
        LogHelper.log("Aseman", "Received photo message notification");
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
            Core.getInstance().bus().post(new MessageReceived(true, message, messageLocal));
            showMessageNotification(message, "Photo");
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onAudioMessage(final Notifications.AudioMessageNotification mcn) {
        LogHelper.log("Aseman", "Received audio message notification");
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
            Core.getInstance().bus().post(new MessageReceived(true, message, messageLocal));
            showMessageNotification(message, "Audio");
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onVideoMessage(final Notifications.VideoMessageNotification mcn) {
        LogHelper.log("Aseman", "Received video message notification");
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
            Core.getInstance().bus().post(new MessageReceived(true, message, messageLocal));
            showMessageNotification(message, "Video");
        }

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void onServiceMessage(final Notifications.ServiceMessageNotification mcn) {
        handleServiceMessage(mcn.getMessage());

        notifyServerNotifReceived(mcn.getNotificationId());
    }

    private void handleServiceMessage(Entities.ServiceMessage message) {
        LogHelper.log("Aseman", "Received service message notification");
        message.setSeenByMe(false);
        if (DatabaseHelper.notifyServiceMessageReceived(message)) {
            Entities.MessageLocal messageLocal = new Entities.MessageLocal();
            messageLocal.setMessageId(message.getRoomId());
            messageLocal.setSent(true);
            Core.getInstance().bus().post(new MessageReceived(true, message, messageLocal));
            showMessageNotification(message, message.getText());
        }
    }

    private void showNotification(String title, String content, Intent intent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "default")
                .setSmallIcon(R.mipmap.ic_launcher);
        mBuilder.setContentTitle(title);
        mBuilder.setContentText(content)
                .setAutoCancel(true);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);
        if (Build.VERSION.SDK_INT >= 21) {
            mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });
        }
        mBuilder.setLights(getResources().getColor(R.color.colorBlue), 3000, 3000);
        if (intent != null) {
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pIntent);
        }
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.build());
    }

    private void showMessageNotification(Entities.Message message, String text) {
        String title, content;
        if (message.getRoom().getComplex().getMode() == 2) {
            Entities.Contact contact = DatabaseHelper.getContactByComplexId(message.getRoom().getComplex().getComplexId());
            Entities.User user = contact.getPeer();
            title = user.getTitle() + " / " + message.getRoom().getTitle();
        } else
            title = message.getRoom().getComplex().getTitle() + ":" + message.getRoom().getTitle();
        content = (message.getAuthor() != null ? message.getAuthor().getTitle() : "Aseman") + " : " + text;
        Intent intent = new Intent(this, RoomActivity.class);
        intent.putExtra("complex_id", message.getRoom().getComplex().getComplexId());
        intent.putExtra("room_id", message.getRoom().getRoomId());
        showNotification(title, content, intent);
    }

    private void showInviteNotification(Entities.Invite invite) {
        String title = "Invite from " + invite.getComplex().getTitle();
        String content = "You are invited to [" + invite.getComplex().getTitle() + "] complex";
        Intent intent = new Intent(this, ComplexProfileActivity.class);
        intent.putExtra("complex", invite.getComplex());
        showNotification(title, content, intent);
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