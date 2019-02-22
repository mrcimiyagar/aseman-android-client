package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.util.Pair;

import com.anadeainc.rxbus.Subscribe;

import java.util.concurrent.LinkedBlockingDeque;

import androidx.annotation.Nullable;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.FileMessageSending;
import kasper.android.pulse.models.extras.TextMessageSending;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.MessageHandler;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import retrofit2.Call;

public class MessagesService extends IntentService {

    private static Thread txtMsgSenderThread;
    private static Thread fileMsgSenderThread;
    private static boolean alive = false;
    private static LinkedBlockingDeque<TextMessageSending> txtMsgQueue = new LinkedBlockingDeque<>();
    private static LinkedBlockingDeque<FileMessageSending> fileMsgQueue = new LinkedBlockingDeque<>();

    public MessagesService() {
        super("MessagesService");
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
        LogHelper.log("Aseman", "Messages service started");
        Core.getInstance().bus().register(this);
        alive = true;
        if (txtMsgSenderThread == null) {
            txtMsgSenderThread = new Thread(() -> {
                try {
                    while (alive) {
                        TextMessageSending sending = txtMsgQueue.take();
                        try {
                            final Pair<Entities.Message, Entities.MessageLocal> pair = DatabaseHelper.notifyTextMessageSending(sending.getRoomId(), sending.getText());
                            final Entities.Message message = pair.first;
                            final Entities.MessageLocal messageLocal = pair.second;
                            final long messageLocalId = message.getMessageId();

                            Core.getInstance().bus().post(new MessageSending(message, messageLocal));

                            final Packet packet = new Packet();
                            Entities.Complex complex = new Entities.Complex();
                            complex.setComplexId(sending.getComplexId());
                            packet.setComplex(complex);
                            Entities.Room room = new Entities.Room();
                            room.setRoomId(sending.getRoomId());
                            packet.setRoom(room);
                            packet.setTextMessage((Entities.TextMessage) message);
                            MessageHandler messageHandler = NetworkHelper.getRetrofit().create(MessageHandler.class);
                            Call<Packet> call = messageHandler.createTextMessage(packet);
                            NetworkHelper.requestServer(call, new ServerCallback() {
                                @Override
                                public void onRequestSuccess(Packet packet) {
                                    final Entities.TextMessage msg = packet.getTextMessage();
                                    DatabaseHelper.notifyTextMessageSent(messageLocalId, msg.getMessageId(), msg.getTime());
                                    if (DatabaseHelper.getComplexById(sending.getComplexId()).getMode() == 1) {
                                        msg.setSeenCount(1);
                                        DatabaseHelper.notifyMessageUpdated(msg);
                                    }
                                    Core.getInstance().bus().post(new MessageSent(messageLocalId, msg.getMessageId()));
                                }

                                @Override
                                public void onServerFailure() {
                                    Core.getInstance().bus().post(new ShowToast("Message delivery failure"));
                                    txtMsgQueue.offer(sending);
                                }

                                @Override
                                public void onConnectionFailure() {
                                    Core.getInstance().bus().post(new ShowToast("Message delivery failure"));
                                    txtMsgQueue.offerFirst(sending);
                                }
                            });
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            txtMsgQueue.offerFirst(sending);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!txtMsgSenderThread.isAlive())
            txtMsgSenderThread.start();

        if (fileMsgSenderThread == null) {
            fileMsgSenderThread = new Thread(() -> {
                try {
                    while (alive) {
                        FileMessageSending sending = fileMsgQueue.take();
                        try {
                            FilesService.uploadFile(new Uploading(sending.getDocType(), sending.getPath()
                                    , sending.getComplexId(), sending.getRoomId(), false, true));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            fileMsgQueue.offerFirst(sending);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!fileMsgSenderThread.isAlive())
            fileMsgSenderThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        alive = false;
        Core.getInstance().bus().unregister(this);
        LogHelper.log("Aseman", "Messages service destroyed");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("Aseman", "Messages service task removed");
    }

    @Subscribe
    public void onFileUploaded(FileUploaded fileUploaded) {
        DocTypes docType = fileUploaded.getDocType();
        long onlineFileId = fileUploaded.getOnlineFileId();
        Entities.File finalFile = fileUploaded.getFile();
        Entities.Message finalMessage = fileUploaded.getMessage();
        if (finalMessage != null && finalFile != null) {
            Packet packet = new Packet();
            Entities.Complex complex = new Entities.Complex();
            complex.setComplexId(fileUploaded.getComplexId());
            packet.setComplex(complex);
            Entities.Room room = new Entities.Room();
            room.setRoomId(fileUploaded.getRoomId());
            packet.setRoom(room);
            finalFile.setFileId(onlineFileId);
            packet.setFile(finalFile);
            MessageHandler messageHandler = NetworkHelper.getRetrofit().create(MessageHandler.class);
            Call<Packet> call = messageHandler.createFileMessage(packet);
            NetworkHelper.requestServer(call, new ServerCallback() {
                @Override
                public void onRequestSuccess(Packet packet) {
                    long messageId = -1;
                    long time;
                    Entities.Message msg;
                    if (docType == DocTypes.Photo) {
                        msg = packet.getPhotoMessage();
                        messageId = msg.getMessageId();
                        time = packet.getPhotoMessage().getTime();
                        DatabaseHelper.notifyPhotoMessageSent(finalMessage.getMessageId(), messageId, time);
                    } else if (docType == DocTypes.Audio) {
                        msg = packet.getAudioMessage();
                        messageId = msg.getMessageId();
                        time = packet.getAudioMessage().getTime();
                        DatabaseHelper.notifyAudioMessageSent(finalMessage.getMessageId(), messageId, time);
                    } else if (docType == DocTypes.Video) {
                        msg = packet.getVideoMessage();
                        messageId = msg.getMessageId();
                        time = packet.getVideoMessage().getTime();
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
                    Core.getInstance().bus().post(new MessageSent(finalMessage.getMessageId(), messageId));
                }
                @Override
                public void onServerFailure() {
                    Core.getInstance().bus().post(new ShowToast("Message delivery failure"));
                }
                @Override
                public void onConnectionFailure() {
                    Core.getInstance().bus().post(new ShowToast("Message delivery failure"));
                }
            });
        }
    }

    public static void enqueueMessage(TextMessageSending sending) {
        txtMsgQueue.offer(sending);
    }

    public static void enqueueMessage(FileMessageSending sending) {
        fileMsgQueue.offer(sending);
    }
}