package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import eightbitlab.com.blurview.BlurView;
import eightbitlab.com.blurview.RenderScriptBlur;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.MusicPlayerActivity;
import kasper.android.pulse.activities.PhotoViewerActivity;
import kasper.android.pulse.activities.VideoPlayerActivity;
import kasper.android.pulse.callbacks.network.OnFileDownloadListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.rxbus.notifications.FileDownloadCancelled;
import kasper.android.pulse.rxbus.notifications.FileDownloaded;
import kasper.android.pulse.rxbus.notifications.FileReceived;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploadCancelled;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.FileUploading;
import kasper.android.pulse.rxbus.notifications.MessageDeleted;
import kasper.android.pulse.rxbus.notifications.MessageReceived;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.MessageSent;

import static kasper.android.pulse.models.extras.DocTypes.Audio;
import static kasper.android.pulse.models.extras.DocTypes.Photo;
import static kasper.android.pulse.models.extras.DocTypes.Video;

/**
 * Created by keyhan1376 on 3/4/2018
 */

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private final List<Entities.Message> messages;
    private Hashtable<Long, Entities.MessageLocal> messageLocals;
    private Hashtable<Long, Entities.FileLocal> fileLocals;
    private long myId;

    public MessagesAdapter(AppCompatActivity activity
            , List<Entities.Message> ms
            , Hashtable<Long, Entities.MessageLocal> mls
            , Hashtable<Long, Entities.FileLocal> fls) {
        this.activity = activity;
        this.messages = ms;
        this.messageLocals = mls;
        this.fileLocals = fls;
        Entities.Session session = DatabaseHelper.getSingleSession();
        if (session != null)
            this.myId = session.getBaseUserId();
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onFileReceived(FileReceived fileReceived) {
        fileLocals.put(fileReceived.getFileLocal().getFileId(), fileReceived.getFileLocal().clone());
    }

    @Subscribe
    public void onFileTransferProgressed(FileTransferProgressed progressed) {
        int progress = progressed.getProgress();
        long fileId = progressed.getFileId();
        int counter = 0;
        for (Entities.Message message : messages) {
            if (message instanceof Entities.PhotoMessage) {
                if (((Entities.PhotoMessage) message).getPhoto().getFileId() == fileId) {
                    Entities.FileLocal fileLocal = fileLocals.get(((Entities.PhotoMessage) message).getPhoto().getFileId());
                    fileLocal.setProgress(progress);
                    notifyItemChanged(counter);
                }
            } else if (message instanceof Entities.AudioMessage) {
                if (((Entities.AudioMessage) message).getAudio().getFileId() == fileId) {
                    Entities.FileLocal fileLocal = fileLocals.get(((Entities.AudioMessage) message).getAudio().getFileId());
                    fileLocal.setProgress(progress);
                    notifyItemChanged(counter);
                }
            } else if (message instanceof Entities.VideoMessage) {
                if (((Entities.VideoMessage) message).getVideo().getFileId() == fileId) {
                    Entities.FileLocal fileLocal = fileLocals.get(((Entities.VideoMessage) message).getVideo().getFileId());
                    fileLocal.setProgress(progress);
                    notifyItemChanged(counter);
                }
            }
            counter++;
        }
    }

    @Subscribe
    public void onFileDownloadCancelled(FileDownloadCancelled cancelled) {
        long fileId = cancelled.getFileId();
        int counter = 0;
        for (Entities.Message message : messages) {
            if (message instanceof Entities.PhotoMessage) {
                if (((Entities.PhotoMessage) message).getPhoto().getFileId() == fileId) {
                    fileLocals.get(((Entities.PhotoMessage) message).getPhoto().getFileId()).setTransferring(false);
                    notifyItemChanged(counter);
                }
            } else if (message instanceof Entities.AudioMessage) {
                if (((Entities.AudioMessage) message).getAudio().getFileId() == fileId) {
                    fileLocals.get(((Entities.AudioMessage) message).getAudio().getFileId()).setTransferring(false);
                    notifyItemChanged(counter);
                }
            } else if (message instanceof Entities.VideoMessage) {
                if (((Entities.VideoMessage) message).getVideo().getFileId() == fileId) {
                    fileLocals.get(((Entities.VideoMessage) message).getVideo().getFileId()).setTransferring(false);
                    notifyItemChanged(counter);
                }
            }
            counter++;
        }
    }

    @Subscribe
    public void onFileDownloaded(FileDownloaded downloaded) {
        long localFileId = downloaded.getFileId();
        int counter = 0;
        for (Entities.Message message : messages) {
            if (message instanceof Entities.PhotoMessage) {
                if (((Entities.PhotoMessage) message).getPhoto().getFileId() == localFileId) {
                    Entities.FileLocal fileLocal = fileLocals.get(((Entities.PhotoMessage) message).getPhoto().getFileId());
                    fileLocal.setTransferring(false);
                    notifyItemChanged(counter);
                }
            } else if (message instanceof Entities.AudioMessage) {
                if (((Entities.AudioMessage) message).getAudio().getFileId() == localFileId) {
                    Entities.FileLocal fileLocal = fileLocals.get(((Entities.AudioMessage) message).getAudio().getFileId());
                    fileLocal.setTransferring(false);
                    notifyItemChanged(counter);
                }
            } else if (message instanceof Entities.VideoMessage) {
                if (((Entities.VideoMessage) message).getVideo().getFileId() == localFileId) {
                    Entities.FileLocal fileLocal = fileLocals.get(((Entities.VideoMessage) message).getVideo().getFileId());
                    fileLocal.setTransferring(false);
                    notifyItemChanged(counter);
                }
            }
            counter++;
        }
    }

    @Subscribe
    public void onFileUploading(FileUploading uploading) {
        fileLocals.put(uploading.getFileLocal().getFileId(), uploading.getFileLocal().clone());
    }

    @Subscribe
    public void onFileUploaded(FileUploaded uploaded) {
        long onlineFileId = uploaded.getOnlineFileId();
        long localFileId = uploaded.getLocalFileId();
        Entities.FileLocal fileLocal = fileLocals.get(localFileId);
        fileLocal.setFileId(onlineFileId);
        fileLocal.setTransferring(false);
        fileLocals.put(onlineFileId, fileLocal);
        fileLocals.remove(localFileId);
        int counter = 0;
        for (Entities.Message message : messages) {
            if (message instanceof Entities.PhotoMessage && ((Entities.PhotoMessage) message).getPhoto().getFileId() == localFileId) {
                ((Entities.PhotoMessage) message).getPhoto().setFileId(onlineFileId);
                notifyItemChanged(counter);
            } else if (message instanceof Entities.AudioMessage && ((Entities.AudioMessage) message).getAudio().getFileId() == localFileId) {
                ((Entities.AudioMessage) message).getAudio().setFileId(onlineFileId);
                notifyItemChanged(counter);
            } else if (message instanceof Entities.VideoMessage && ((Entities.VideoMessage) message).getVideo().getFileId() == localFileId) {
                ((Entities.VideoMessage) message).getVideo().setFileId(onlineFileId);
                notifyItemChanged(counter);
            }
            counter++;
        }
    }

    @Subscribe
    public void onFileUploadCancelled(FileUploadCancelled cancelled) {
        MessagesAdapter.this.fileLocals.remove(cancelled.getLocalFileId());
    }

    @Subscribe
    public void onMessageReceived(MessageReceived messageReceived) {
        Entities.Message message = messageReceived.getMessage();
        Entities.MessageLocal messageLocal = messageReceived.getMessageLocal();
        messages.add(message);
        messageLocals.put(messageLocal.getMessageId(), messageLocal);
        notifyItemInserted(messages.size() - 1);
        if (messages.size() >= 2) {
            notifyItemChanged(messages.size() - 2);
        }
    }

    @Subscribe
    public void onMessageDeleted(MessageDeleted messageDeleted) {
        Entities.Message message = messageDeleted.getMessage();
        for (int counter = 0; counter < messages.size(); counter++) {
            if (messages.get(counter).getMessageId() == message.getMessageId()) {
                messages.remove(counter);
                notifyItemRemoved(counter);
                break;
            }
        }
    }

    @Subscribe
    public void onMessageSending(MessageSending messageSending) {
        Entities.Message message = messageSending.getMessage();
        Entities.MessageLocal messageLocal = messageSending.getMessageLocal();
        messages.add(message);
        messageLocals.put(messageLocal.getMessageId(), messageLocal);
        notifyItemInserted(messages.size() - 1);
        if (messages.size() >= 2) {
            notifyItemChanged(messages.size() - 2);
        }
    }

    @Subscribe
    public void onMessageSent(MessageSent messageSent) {
        long localMessageId = messageSent.getLocalMessageId();
        long onlineMessageId = messageSent.getOnlineMessageId();
        Entities.MessageLocal messageLocal = messageLocals.get(localMessageId);
        messageLocal.setMessageId(onlineMessageId);
        messageLocal.setSent(true);
        messageLocals.put(onlineMessageId, messageLocal);
        messageLocals.remove(localMessageId);
        int counter = 0;
        for (Entities.Message message : messages) {
            if (message.getMessageId() == localMessageId || message.getMessageId() == onlineMessageId) {
                message.setMessageId(onlineMessageId);
                notifyItemChanged(counter);
                break;
            }
            counter++;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case 1: {
                return new TextMessageVH(1, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_text, parent, false));
            }
            case 6: {
                return new TextMessageVH(0, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_text, parent, false));
            }
            case 11: {
                return new TextMessageVH(2, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_text, parent, false));
            }
            case 2: {
                return new PhotoMessageVH(1, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_photo, parent, false));
            }
            case 7: {
                return new PhotoMessageVH(0, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_photo, parent, false));
            }
            case 3: {
                return new AudioMessageVH(1, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_audio, parent, false));
            }
            case 8: {
                return new AudioMessageVH(0, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_audio, parent, false));
            }
            case 4: {
                return new VideoMessageVH(1, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_video, parent, false));
            }
            case 9: {
                return new VideoMessageVH(0, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_video, parent, false));
            }
            default: {
                return new TextMessageVH(1, LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_messages_text, parent, false));
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        Entities.Message message = this.messages.get(position);
        if (message instanceof Entities.TextMessage) {
            if (message.getAuthorId() == myId) {
                return 1;
            } else{
                return 6;
            }
        } else if (message instanceof Entities.PhotoMessage) {
            if (message.getAuthorId() == myId) {
                return 2;
            } else {
                return 7;
            }
        } else if (message instanceof Entities.AudioMessage) {
            if (message.getAuthorId() == myId) {
                return 3;
            } else {
                return 8;
            }
        } else if (message instanceof Entities.VideoMessage) {
            if (message.getAuthorId() == myId) {
                return 4;
            } else {
                return 9;
            }
        } else if (message instanceof Entities.ServiceMessage) {
            return 11;
        } else {
            if (message.getAuthorId() == myId) {
                return 5;
            } else {
                return 10;
            }
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder rawHolder, int position) {
        final Entities.Message rawMessage = this.messages.get(position);
        int viewType = getItemViewType(position);
        if (viewType == 1 || viewType == 6) {
            Entities.TextMessage message = (Entities.TextMessage) rawMessage;
            TextMessageVH holder = (TextMessageVH) rawHolder;
            loadAvatarImage(message.getAuthorId(), holder);
            holder.textTV.setText(message.getText());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(message.getTime());
            holder.timeTV.setText((calendar.get(Calendar.HOUR) > 9 ? calendar.get(Calendar.HOUR) : "0"
                    + calendar.get(Calendar.HOUR)) + ":" + (calendar.get(Calendar.MINUTE) > 9 ?
                    calendar.get(Calendar.MINUTE) : "0" + calendar.get(Calendar.MINUTE)));
            if (viewType == 1) {
                Entities.MessageLocal messageLocal = this.messageLocals.get(message.getMessageId());
                if (messageLocal != null)
                    holder.signIV.setVisibility(messageLocal.isSent() ? View.VISIBLE : View.GONE);
            }
            if (position == this.messages.size() - 1) {
                holder.avatarIV.setVisibility(View.VISIBLE);
            } else {
                Entities.Message nextMessage = this.messages.get(position + 1);
                if (nextMessage.getAuthorId() != message.getAuthorId()) {
                    holder.avatarIV.setVisibility(View.VISIBLE);
                } else {
                    holder.avatarIV.setVisibility(View.GONE);
                }
            }

        } else if (viewType == 2 || viewType == 7) {
            final Entities.PhotoMessage message = (Entities.PhotoMessage) rawMessage;
            final PhotoMessageVH holder = (PhotoMessageVH) rawHolder;
            if (message.getPhoto().getWidth() > getScreenWidth()- dpToPx(144)) {
                int width = Math.max((getScreenWidth() - dpToPx(144)), dpToPx(200));
                int height = message.getPhoto().getHeight() * width
                        / message.getPhoto().getWidth();
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                if (viewType == 2){
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.setMargins(dpToPx(16), dpToPx(4), dpToPx(72), dpToPx(4));
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    params.setMargins(dpToPx(72), dpToPx(4), dpToPx(16), dpToPx(4));
                }
                holder.container.setLayoutParams(params);
            } else if (message.getPhoto().getHeight() > getScreenHeight() - dpToPx(144)) {
                int height = getScreenHeight() - dpToPx(144);
                int width = Math.max(message.getPhoto().getWidth() * height / message.getPhoto()
                        .getHeight(), dpToPx(200));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                if (viewType == 2){
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.setMargins(dpToPx(16), dpToPx(4), dpToPx(72), dpToPx(4));
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    params.setMargins(dpToPx(72), dpToPx(4), dpToPx(16), dpToPx(4));
                }
                holder.container.setLayoutParams(params);
            } else {
                int width = Math.max(message.getPhoto().getWidth(), dpToPx(200));
                int height = Math.max(message.getPhoto().getHeight(), dpToPx(200));
                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
                if (viewType == 2){
                    params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    params.setMargins(dpToPx(16), dpToPx(4), dpToPx(72), dpToPx(4));
                } else {
                    params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    params.setMargins(dpToPx(72), dpToPx(4), dpToPx(16), dpToPx(4));
                }
                holder.container.setLayoutParams(params);
            }
            View decorView = activity.getWindow().getDecorView();
            ViewGroup rootView = decorView.findViewById(android.R.id.content);
            Drawable windowBackground = decorView.getBackground();
            holder.blurView.setupWith(rootView)
                    .windowBackground(windowBackground)
                    .blurAlgorithm(new RenderScriptBlur(activity))
                    .blurRadius(20);
            loadAvatarImage(message.getAuthorId(), holder);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(message.getTime());
            holder.timeTV.setText((calendar.get(Calendar.HOUR) > 9 ? calendar.get(Calendar.HOUR) : "0"
                    + calendar.get(Calendar.HOUR)) + ":" + (calendar.get(Calendar.MINUTE) > 9 ?
                    calendar.get(Calendar.MINUTE) : "0" + calendar.get(Calendar.MINUTE)));
            if (viewType == 2) {
                Entities.MessageLocal messageLocal = this.messageLocals.get(message.getMessageId());
                if (messageLocal != null)
                    holder.signIV.setVisibility(messageLocal.isSent() ? View.VISIBLE : View.GONE);
            }
            final Entities.FileLocal fileLocal = this.fileLocals.get(message.getPhoto().getFileId());
            if (fileLocal != null) {
                if (fileLocal.isTransferring()) {
                    holder.loadingContainer.setVisibility(View.VISIBLE);
                    holder.blurView.setVisibility(View.VISIBLE);
                    holder.downloadBTN.setVisibility(View.GONE);
                    holder.loadingPB.setProgress(fileLocal.getProgress());
                    String filePath;
                    if (fileLocal.getPath().length() > 0) {
                        filePath = fileLocal.getPath();
                    } else {
                        filePath = NetworkHelper.createFileLink(message.getPhoto().getFileId());
                    }
                    GlideApp.with(activity).load(filePath).into(holder.imageIV);
                    holder.loadingCancelBTN.setOnClickListener(v -> {
                        if (fileLocal.getPath().length() > 0) {
                            NetworkHelper.cancelUploadFile(message.getPhoto().getFileId());
                            DatabaseHelper.deletePhotoMessage(message.getMessageId());
                            Core.getInstance().bus().post(new MessageDeleted(message));
                        } else {
                            NetworkHelper.cancelDownloadFile(message.getPhoto().getFileId());
                            DatabaseHelper.notifyFileDownloadCancelled(message.getPhoto().getFileId());
                            Core.getInstance().bus().post(new FileDownloadCancelled(Photo, message.getPhoto().getFileId()));
                        }
                    });
                } else {
                    holder.loadingContainer.setVisibility(View.GONE);
                    final String filePath;
                    if (fileLocal.getPath().length() > 0) {
                        filePath = fileLocal.getPath();
                    } else {
                        filePath = DatabaseHelper.getFilePath(message.getPhoto().getFileId());
                    }
                    if (!new File(filePath).exists()) {
                        holder.blurView.setVisibility(View.VISIBLE);
                        holder.downloadBTN.setVisibility(View.VISIBLE);
                        holder.downloadBTN.setOnClickListener(v -> {
                            DatabaseHelper.notifyFileDownloading(message.getPhoto().getFileId());
                            fileLocal.setTransferring(true);
                            notifyItemChanged(holder.getAdapterPosition());
                            NetworkHelper.downloadFile(message.getPhoto(), message.getRoomId()
                                    , progress -> {
                                        DatabaseHelper.notifyFileTransferProgressed(message.getPhoto().getFileId(), progress);
                                        activity.runOnUiThread(() -> Core.getInstance().bus().post(new FileTransferProgressed(Photo, message.getPhoto().getFileId(), progress)));
                                    }, new OnFileDownloadListener() {
                                        @Override
                                        public void fileDownloaded() {
                                            DatabaseHelper.notifyFileDownloaded(message.getPhoto().getFileId());
                                            activity.runOnUiThread(() -> Core.getInstance().bus().post(new FileDownloaded(Photo, message.getPhoto().getFileId())));
                                        }

                                        @Override
                                        public void downloadFailed() {

                                        }
                                    });
                        });
                        GlideApp.with(activity).load(NetworkHelper.createFileLink(message.getPhoto()
                                .getFileId())).into(holder.imageIV);
                    } else {
                        holder.blurView.setVisibility(View.GONE);
                        holder.downloadBTN.setVisibility(View.GONE);
                        GlideApp.with(activity).load(filePath).into(holder.imageIV);
                        holder.imageIV.setOnClickListener(v -> {
                            Pair<View, String> picture = Pair.create(holder.imageIV, "photo");
                            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(activity, picture);
                            Intent intent = new Intent(activity, PhotoViewerActivity.class);
                            intent.putExtra("fileId", message.getPhoto().getFileId());
                            activity.startActivity(intent, options.toBundle());
                        });
                    }
                }
            }
            if (position == this.messages.size() - 1) {
                holder.avatarIV.setVisibility(View.VISIBLE);
            } else {
                Entities.Message nextMessage = this.messages.get(position + 1);
                if (nextMessage.getAuthorId() != message.getAuthorId()) {
                    holder.avatarIV.setVisibility(View.VISIBLE);
                } else {
                    holder.avatarIV.setVisibility(View.GONE);
                }
            }

        } else if (viewType == 3 || viewType == 8) {
            final Entities.AudioMessage message = (Entities.AudioMessage) rawMessage;
            final AudioMessageVH holder = (AudioMessageVH) rawHolder;
            loadAvatarImage(message.getAuthorId(), holder);
            holder.captionTV.setText(message.getAudio().getTitle());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(message.getTime());
            holder.timeTV.setText((calendar.get(Calendar.HOUR) > 9 ? calendar.get(Calendar.HOUR) : "0"
                    + calendar.get(Calendar.HOUR)) + ":" + (calendar.get(Calendar.MINUTE) > 9 ?
                    calendar.get(Calendar.MINUTE) : "0" + calendar.get(Calendar.MINUTE)));
            if (viewType == 3) {
                Entities.MessageLocal messageLocal = this.messageLocals.get(message.getMessageId());
                if (messageLocal != null)
                    holder.signIV.setVisibility(messageLocal.isSent() ? View.VISIBLE : View.GONE);
            }
            final Entities.FileLocal fileLocal = this.fileLocals.get(message.getAudio().getFileId());
            if (fileLocal != null) {
                if (fileLocal.isTransferring()) {
                    holder.loadingContainer.setVisibility(View.VISIBLE);
                    holder.downloadBTN.setVisibility(View.GONE);
                    holder.playBTN.setVisibility(View.GONE);
                    holder.loadingPB.setProgress(fileLocal.getProgress());
                    holder.loadingCancelBTN.setOnClickListener(v -> {
                        if (fileLocal.getPath().length() > 0) {
                            NetworkHelper.cancelUploadFile(message.getAudio().getFileId());
                            DatabaseHelper.deleteAudioMessage(message.getMessageId());
                            Core.getInstance().bus().post(new MessageDeleted(message));
                        } else {
                            NetworkHelper.cancelDownloadFile(message.getAudio().getFileId());
                            DatabaseHelper.notifyFileDownloadCancelled(message.getAudio().getFileId());
                            Core.getInstance().bus().post(new FileDownloadCancelled(Audio, message.getAudio().getFileId()));
                        }
                    });
                } else {
                    holder.loadingContainer.setVisibility(View.GONE);
                    String filePath;
                    if (fileLocal.getPath().length() > 0) {
                        filePath = fileLocal.getPath();
                    } else {
                        filePath = DatabaseHelper.getFilePath(message.getAudio().getFileId());
                    }
                    if (!new File(filePath).exists()) {
                        holder.downloadBTN.setVisibility(View.VISIBLE);
                        holder.playBTN.setVisibility(View.GONE);
                        holder.downloadBTN.setOnClickListener(v -> {
                            DatabaseHelper.notifyFileDownloading(message.getAudio().getFileId());
                            fileLocal.setTransferring(true);
                            notifyItemChanged(holder.getAdapterPosition());
                            DatabaseHelper.notifyFileDownloading(message.getAudio().getFileId());
                            fileLocal.setTransferring(true);
                            notifyItemChanged(holder.getAdapterPosition());
                            NetworkHelper.downloadFile(message.getAudio(), message.getRoomId()
                                    , progress -> {
                                        DatabaseHelper.notifyFileTransferProgressed(message.getAudio().getFileId(), progress);
                                        activity.runOnUiThread(() -> Core.getInstance().bus().post(new FileTransferProgressed(Audio, message.getAudio().getFileId(), progress)));
                                    }, new OnFileDownloadListener() {
                                        @Override
                                        public void fileDownloaded() {
                                            DatabaseHelper.notifyFileDownloaded(message.getAudio().getFileId());
                                            activity.runOnUiThread(() -> Core.getInstance().bus().post(new FileDownloaded(Audio, message.getAudio().getFileId())));
                                        }

                                        @Override
                                        public void downloadFailed() {

                                        }
                                    });
                        });
                    } else {
                        holder.downloadBTN.setVisibility(View.GONE);
                        holder.playBTN.setVisibility(View.VISIBLE);
                        holder.playBTN.setOnClickListener(v ->
                                activity.startActivity(new Intent(activity, MusicPlayerActivity.class)
                                .putExtra("audio", message.getAudio())
                                .putExtra("room-id", message.getRoomId())));
                    }
                }
            }
            if (position == this.messages.size() - 1) {
                holder.avatarIV.setVisibility(View.VISIBLE);
            } else {
                Entities.Message nextMessage = this.messages.get(position + 1);
                if (nextMessage.getAuthorId() != message.getAuthorId()) {
                    holder.avatarIV.setVisibility(View.VISIBLE);
                } else {
                    holder.avatarIV.setVisibility(View.GONE);
                }
            }

        } else if (viewType == 4 || viewType == 9) {
            final Entities.VideoMessage message = (Entities.VideoMessage) rawMessage;
            final VideoMessageVH holder = (VideoMessageVH) rawHolder;
            View decorView = activity.getWindow().getDecorView();
            ViewGroup rootView = decorView.findViewById(android.R.id.content);
            Drawable windowBackground = decorView.getBackground();
            holder.blurView.setupWith(rootView)
                    .windowBackground(windowBackground)
                    .blurAlgorithm(new RenderScriptBlur(activity))
                    .blurRadius(20);
            loadAvatarImage(message.getAuthorId(), holder);
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(message.getTime());
            holder.timeTV.setText((calendar.get(Calendar.HOUR) > 9 ? calendar.get(Calendar.HOUR) : "0"
                    + calendar.get(Calendar.HOUR)) + ":" + (calendar.get(Calendar.MINUTE) > 9 ?
                    calendar.get(Calendar.MINUTE) : "0" + calendar.get(Calendar.MINUTE)));
            if (viewType == 4) {
                Entities.MessageLocal messageLocal = this.messageLocals.get(message.getMessageId());
                if (messageLocal != null)
                    holder.signIV.setVisibility(messageLocal.isSent() ? View.VISIBLE : View.GONE);
            }
            final Entities.FileLocal fileLocal = this.fileLocals.get(message.getVideo().getFileId());
            if (fileLocal != null) {
                if (fileLocal.isTransferring()) {
                    holder.loadingContainer.setVisibility(View.VISIBLE);
                    holder.blurView.setVisibility(View.VISIBLE);
                    holder.downloadBTN.setVisibility(View.GONE);
                    holder.playBTN.setVisibility(View.GONE);
                    holder.loadingPB.setProgress(fileLocal.getProgress());
                    String filePath;
                    if (fileLocal.getPath().length() > 0) {
                        filePath = fileLocal.getPath();
                    } else {
                        filePath = NetworkHelper.createFileLink(message.getVideo().getFileId());
                    }
                    GlideApp.with(activity).load(filePath).into(holder.imageIV);
                    holder.loadingCancelBTN.setOnClickListener(v -> {
                        if (fileLocal.getPath().length() > 0) {
                            NetworkHelper.cancelUploadFile(message.getVideo().getFileId());
                            DatabaseHelper.deleteVideoMessage(message.getMessageId());
                            Core.getInstance().bus().post(new MessageDeleted(message));
                        } else {
                            NetworkHelper.cancelDownloadFile(message.getVideo().getFileId());
                            DatabaseHelper.notifyFileDownloadCancelled(message.getVideo().getFileId());
                            Core.getInstance().bus().post(new FileDownloadCancelled(Video, message.getVideo().getFileId()));
                        }
                    });
                } else {
                    holder.loadingContainer.setVisibility(View.GONE);
                    final String filePath;
                    if (fileLocal.getPath().length() > 0) {
                        filePath = fileLocal.getPath();
                    } else {
                        filePath = DatabaseHelper.getFilePath(message.getVideo().getFileId());
                    }
                    if (!new File(filePath).exists()) {
                        holder.blurView.setVisibility(View.VISIBLE);
                        holder.downloadBTN.setVisibility(View.VISIBLE);
                        holder.playBTN.setVisibility(View.GONE);
                        GlideApp.with(activity).load(NetworkHelper
                                .createFileLink(message.getVideo().getFileId())).into(holder.imageIV);
                        holder.downloadBTN.setOnClickListener(v -> {
                            DatabaseHelper.notifyFileDownloading(message.getVideo().getFileId());
                            fileLocal.setTransferring(true);
                            notifyItemChanged(holder.getAdapterPosition());
                            NetworkHelper.downloadFile(message.getVideo(), message.getRoomId()
                                    , progress -> {
                                        DatabaseHelper.notifyFileTransferProgressed(message.getVideo().getFileId(), progress);
                                        activity.runOnUiThread(() -> Core.getInstance().bus().post(new FileTransferProgressed(Video, message.getVideo().getFileId(), progress)));
                                    }, new OnFileDownloadListener() {
                                        @Override
                                        public void fileDownloaded() {
                                            DatabaseHelper.notifyFileDownloaded(message.getVideo().getFileId());
                                            activity.runOnUiThread(() ->
                                                    Core.getInstance().bus().post(new FileDownloaded(Video, message.getVideo().getFileId())));
                                        }
                                        @Override
                                        public void downloadFailed() {

                                        }
                                    });
                        });
                    } else {
                        holder.blurView.setVisibility(View.GONE);
                        holder.downloadBTN.setVisibility(View.GONE);
                        holder.playBTN.setVisibility(View.VISIBLE);
                        GlideApp.with(activity).load(filePath).into(holder.imageIV);
                        holder.playBTN.setOnClickListener(v ->
                                activity.startActivity(new Intent(activity, VideoPlayerActivity.class)
                                .putExtra("video-path", filePath)));
                    }
                }
            }
            if (position == this.messages.size() - 1) {
                holder.avatarIV.setVisibility(View.VISIBLE);
            } else {
                Entities.Message nextMessage = this.messages.get(position + 1);
                if (nextMessage.getAuthorId() != message.getAuthorId()) {
                    holder.avatarIV.setVisibility(View.VISIBLE);
                } else {
                    holder.avatarIV.setVisibility(View.GONE);
                }
            }

        } else if (viewType == 11) {
            Entities.ServiceMessage message = (Entities.ServiceMessage) rawMessage;
            TextMessageVH holder = (TextMessageVH) rawHolder;
            holder.textTV.setText(message.getText());
        }
    }

    private void loadAvatarImage(long userId, final RecyclerView.ViewHolder holder) {
        try {
            Entities.BaseUser user = DatabaseHelper.getBaseUserById(userId);
            if (user != null) {
                if (holder instanceof TextMessageVH) {
                    NetworkHelper.loadUserAvatar(user.getAvatar(), ((TextMessageVH) holder).avatarIV);
                } else if (holder instanceof PhotoMessageVH) {
                    NetworkHelper.loadUserAvatar(user.getAvatar(), ((PhotoMessageVH) holder).avatarIV);
                } else if (holder instanceof AudioMessageVH) {
                    NetworkHelper.loadUserAvatar(user.getAvatar(), ((AudioMessageVH) holder).avatarIV);
                } else if (holder instanceof VideoMessageVH) {
                    NetworkHelper.loadUserAvatar(user.getAvatar(), ((VideoMessageVH) holder).avatarIV);
                }
            }
        } catch (Exception ignored) {

        }
    }

    @Override
    public int getItemCount() {
        return this.messages.size();
    }

    public int findFilePosition(long fileId) {
        int counter = 0;
        for (Entities.Message message : messages) {
            if (message instanceof Entities.PhotoMessage) {
                if (((Entities.PhotoMessage) message).getPhoto().getFileId() == fileId) {
                    return counter;
                }
            } else if (message instanceof Entities.AudioMessage) {
                if (((Entities.AudioMessage) message).getAudio().getFileId() == fileId) {
                    return counter;
                }
            } else if (message instanceof Entities.VideoMessage) {
                if (((Entities.VideoMessage) message).getVideo().getFileId() == fileId) {
                    return counter;
                }
            }
            counter++;
        }
        return messages.size() - 1;
    }

    private int getScreenWidth() {
        return activity.getResources().getDisplayMetrics().widthPixels;
    }

    private int getScreenHeight() {
        return activity.getResources().getDisplayMetrics().heightPixels;
    }

    private int dpToPx(float dp) {
        return (int)(dp * activity.getResources().getDisplayMetrics().density);
    }

    class TextMessageVH extends RecyclerView.ViewHolder {

        private TextView textTV;
        private ImageView avatarIV;
        private TextView timeTV;
        private ImageView signIV;

        TextMessageVH(int side, View itemView) {
            super(itemView);
            RelativeLayout leftContainer = itemView.findViewById(R.id.adapter_messages_text_left_container);
            RelativeLayout rightContainer = itemView.findViewById(R.id.adapter_messages_text_right_container);
            if (side == 0) {
                this.textTV = itemView.findViewById(R.id.adapter_messages_text_left_text_text_view);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_text_left_avatar_image_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_text_left_time_text_view);
                leftContainer.setVisibility(View.VISIBLE);
                rightContainer.setVisibility(View.GONE);
            } else if (side == 1) {
                this.textTV = itemView.findViewById(R.id.adapter_messages_text_right_text_text_view);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_text_right_avatar_image_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_text_right_time_text_view);
                this.signIV = itemView.findViewById(R.id.adapter_messages_text_right_sign_image_view);
                leftContainer.setVisibility(View.GONE);
                rightContainer.setVisibility(View.VISIBLE);
            } else if (side == 2) {
                this.textTV = itemView.findViewById(R.id.adapter_messages_text_center_text_view);
            }
        }
    }

    class PhotoMessageVH extends RecyclerView.ViewHolder {

        private CardView container;
        private ImageView imageIV;
        private ImageView avatarIV;
        private TextView timeTV;
        private ImageView signIV;
        private FrameLayout loadingContainer;
        private CircularProgressBar loadingPB;
        private ImageButton loadingCancelBTN;
        private BlurView blurView;
        private ImageView downloadBTN;

        PhotoMessageVH(int side, View itemView) {
            super(itemView);
            RelativeLayout leftContainer = itemView.findViewById(R.id.adapter_messages_image_left_container);
            RelativeLayout rightContainer = itemView.findViewById(R.id.adapter_messages_image_right_container);
            if (side == 0) {
                this.container = itemView.findViewById(R.id.adapter_messages_image_left_content_container);
                this.imageIV = itemView.findViewById(R.id.adapter_messages_image_left_image_image_view);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_image_left_avatar_image_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_image_left_time_text_view);
                this.loadingContainer = itemView.findViewById(R.id.adapter_messages_image_left_loading_container);
                this.loadingPB = itemView.findViewById(R.id.adapter_messages_image_left_loading_progress_bar);
                this.loadingCancelBTN = itemView.findViewById(R.id.adapter_messages_image_left_loading_cancel_image_button);
                this.blurView = itemView.findViewById(R.id.adapter_messages_image_left_blur_view);
                this.downloadBTN = itemView.findViewById(R.id.adapter_messages_image_left_download_image_button);
                leftContainer.setVisibility(View.VISIBLE);
                rightContainer.setVisibility(View.GONE);
            } else if (side == 1) {
                this.container = itemView.findViewById(R.id.adapter_messages_image_right_content_container);
                this.imageIV = itemView.findViewById(R.id.adapter_messages_image_right_image_image_view);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_image_right_avatar_image_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_image_right_time_text_view);
                this.signIV = itemView.findViewById(R.id.adapter_messages_image_right_sign_image_view);
                this.loadingContainer = itemView.findViewById(R.id.adapter_messages_image_right_loading_container);
                this.loadingPB = itemView.findViewById(R.id.adapter_messages_image_right_loading_progress_bar);
                this.loadingCancelBTN = itemView.findViewById(R.id.adapter_messages_image_right_loading_cancel_image_button);
                this.blurView = itemView.findViewById(R.id.adapter_messages_image_right_blur_view);
                this.downloadBTN = itemView.findViewById(R.id.adapter_messages_image_right_download_image_button);
                leftContainer.setVisibility(View.GONE);
                rightContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    class AudioMessageVH extends RecyclerView.ViewHolder {

        private ImageView playBTN;
        private ImageView avatarIV;
        private TextView captionTV;
        private ImageView signIV;
        private TextView timeTV;
        private CircularProgressBar loadingPB;
        private ImageButton loadingCancelBTN;
        private FrameLayout loadingContainer;
        private ImageView downloadBTN;

        AudioMessageVH(int side, View itemView) {
            super(itemView);
            RelativeLayout leftContainer = itemView.findViewById(R.id.adapter_messages_audio_left_container);
            RelativeLayout rightContainer = itemView.findViewById(R.id.adapter_messages_audio_right_container);
            if (side == 0) {
                this.playBTN = itemView.findViewById(R.id.adapter_messages_audio_left_play_image_button);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_audio_left_avatar_image_view);
                this.captionTV = itemView.findViewById(R.id.adapter_messages_audio_left_caption_text_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_audio_left_time_text_view);
                this.loadingContainer = itemView.findViewById(R.id.adapter_messages_audio_left_loading_container);
                this.loadingPB = itemView.findViewById(R.id.adapter_messages_audio_left_loading_progress_bar);
                this.loadingCancelBTN = itemView.findViewById(R.id.adapter_messages_audio_left_loading_cancel_image_button);
                this.downloadBTN = itemView.findViewById(R.id.adapter_messages_audio_left_download_image_view);
                leftContainer.setVisibility(View.VISIBLE);
                rightContainer.setVisibility(View.GONE);
            } else if (side == 1) {
                this.playBTN = itemView.findViewById(R.id.adapter_messages_audio_right_play_image_button);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_audio_right_avatar_image_view);
                this.captionTV = itemView.findViewById(R.id.adapter_messages_audio_right_caption_text_view);
                this.signIV = itemView.findViewById(R.id.adapter_messages_audio_right_sign_image_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_audio_right_time_text_view);
                this.loadingContainer = itemView.findViewById(R.id.adapter_messages_audio_right_loading_container);
                this.loadingPB = itemView.findViewById(R.id.adapter_messages_audio_right_loading_progress_bar);
                this.loadingCancelBTN = itemView.findViewById(R.id.adapter_messages_audio_right_loading_cancel_image_button);
                this.downloadBTN = itemView.findViewById(R.id.adapter_messages_audio_right_download_image_view);
                leftContainer.setVisibility(View.GONE);
                rightContainer.setVisibility(View.VISIBLE);
            }
        }
    }

    class VideoMessageVH extends RecyclerView.ViewHolder {

        private ImageView imageIV;
        private ImageButton playBTN;
        private ImageView avatarIV;
        private TextView timeTV;
        private ImageView signIV;
        private FrameLayout loadingContainer;
        private CircularProgressBar loadingPB;
        private ImageButton loadingCancelBTN;
        private BlurView blurView;
        private ImageView downloadBTN;

        VideoMessageVH(int side, View itemView) {
            super(itemView);
            RelativeLayout leftContainer = itemView.findViewById(R.id.adapter_messages_video_left_container);
            RelativeLayout rightContainer = itemView.findViewById(R.id.adapter_messages_video_right_container);
            if (side == 0) {
                this.imageIV = itemView.findViewById(R.id.adapter_messages_video_left_image_image_view);
                this.playBTN = itemView.findViewById(R.id.adapter_messages_video_left_play_image_button);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_video_left_avatar_image_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_video_left_time_text_view);
                this.loadingContainer = itemView.findViewById(R.id.adapter_messages_video_left_loading_container);
                this.loadingPB = itemView.findViewById(R.id.adapter_messages_video_left_loading_progress_bar);
                this.loadingCancelBTN = itemView.findViewById(R.id.adapter_messages_video_left_loading_cancel_image_button);
                this.blurView = itemView.findViewById(R.id.adapter_messages_video_left_blur_view);
                this.downloadBTN = itemView.findViewById(R.id.adapter_messages_video_left_download_image_button);
                leftContainer.setVisibility(View.VISIBLE);
                rightContainer.setVisibility(View.GONE);
            } else if (side == 1) {
                this.imageIV = itemView.findViewById(R.id.adapter_messages_video_right_image_image_view);
                this.playBTN = itemView.findViewById(R.id.adapter_messages_video_right_play_image_button);
                this.avatarIV = itemView.findViewById(R.id.adapter_messages_video_right_avatar_image_view);
                this.timeTV = itemView.findViewById(R.id.adapter_messages_video_right_time_text_view);
                this.signIV = itemView.findViewById(R.id.adapter_messages_video_right_sign_image_view);
                this.loadingContainer = itemView.findViewById(R.id.adapter_messages_video_right_loading_container);
                this.loadingPB = itemView.findViewById(R.id.adapter_messages_video_right_loading_progress_bar);
                this.loadingCancelBTN = itemView.findViewById(R.id.adapter_messages_video_right_loading_cancel_image_button);
                this.blurView = itemView.findViewById(R.id.adapter_messages_video_right_blur_view);
                this.downloadBTN = itemView.findViewById(R.id.adapter_messages_video_right_download_image_button);
                leftContainer.setVisibility(View.GONE);
                rightContainer.setVisibility(View.VISIBLE);
            }
        }
    }
}