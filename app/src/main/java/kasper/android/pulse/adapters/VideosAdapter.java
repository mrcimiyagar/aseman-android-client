package kasper.android.pulse.adapters;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import eightbitlab.com.blurview.BlurView;
import kasper.android.pulse.R;
import kasper.android.pulse.activities.VideoPlayerActivity;
import kasper.android.pulse.callbacks.network.OnFileDownloadListener;
import kasper.android.pulse.callbacks.ui.FileListener;
import kasper.android.pulse.callbacks.ui.OnDocSelectListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.rxbus.notifications.FileDownloadCancelled;
import kasper.android.pulse.rxbus.notifications.FileDownloaded;
import kasper.android.pulse.rxbus.notifications.FileDownloading;
import kasper.android.pulse.rxbus.notifications.FileReceived;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploadCancelled;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.FileUploading;

import static kasper.android.pulse.models.extras.DocTypes.Video;
import static kasper.android.pulse.models.extras.DocTypes.Video;

public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.Holder> {

    private AppCompatActivity activity;
    private final List<Entities.Video> docs;
    private Hashtable<Long, Entities.FileLocal> fileLocals;
    private long roomId;
    private int blockSize;
    private OnDocSelectListener selectCallback;

    public VideosAdapter(AppCompatActivity activity, List<Entities.Video> ds, Hashtable<Long, Entities.FileLocal> fs
            , long roomId, int blockSize, OnDocSelectListener selectCallback) {
        this.activity = activity;
        this.docs = ds;
        this.fileLocals = fs;
        this.roomId = roomId;
        this.blockSize = blockSize;
        this.selectCallback = selectCallback;
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onFileReceived(FileReceived fileReceived) {
        if (fileReceived.getDocType() == Video) {
            docs.add((Entities.Video) fileReceived.getFile().clone());
            fileLocals.put(fileReceived.getFileLocal().getFileId(), fileReceived.getFileLocal().clone());
            notifyItemInserted(fileLocals.size() - 1);
        }
    }

    @Subscribe
    public void onFileTransferProgressed(FileTransferProgressed progressed) {
        if (progressed.getDocType() == Video) {
            int counter = 0;
            for (Entities.File doc : docs) {
                if (doc.getFileId() == progressed.getFileId()) {
                    Entities.FileLocal fileLocal = fileLocals.get(doc.getFileId());
                    fileLocal.setProgress(progressed.getProgress());
                    notifyItemChanged(counter);
                    break;
                }
                counter++;
            }
        }
    }

    @Subscribe
    public void onFileDownloadCancelled(FileDownloadCancelled cancelled) {
        if (cancelled.getDocType() == Video) {
            int counter = 0;
            for (Entities.File doc : docs) {
                if (doc.getFileId() == cancelled.getFileId()) {
                    fileLocals.get(doc.getFileId()).setTransferring(false);
                    notifyItemChanged(counter);
                    break;
                }
                counter++;
            }
        }
    }

    @Subscribe
    public void onFileDownloaded(FileDownloaded downloaded) {
        if (downloaded.getDocType() == Video) {
            int counter = 0;
            for (Entities.File doc : docs) {
                if (doc.getFileId() == downloaded.getFileId()) {
                    Entities.FileLocal fileLocal = fileLocals.get(doc.getFileId());
                    fileLocal.setTransferring(false);
                    notifyItemChanged(counter);
                    break;
                }
                counter++;
            }
        }
    }

    @Subscribe
    public void onFileUploading(FileUploading uploading) {
        if (uploading.getDocType() == Video) {
            docs.add((Entities.Video) uploading.getFile().clone());
            fileLocals.put(uploading.getFileLocal().getFileId(), uploading.getFileLocal().clone());
            notifyItemInserted(docs.size() - 1);
        }
    }

    @Subscribe
    public void onFileUploaded(FileUploaded uploaded) {
        if (uploaded.getDocType() == Video) {
            Entities.FileLocal fileLocal = fileLocals.remove(uploaded.getLocalFileId());
            fileLocal.setFileId(uploaded.getOnlineFileId());
            fileLocal.setTransferring(false);
            fileLocals.put(uploaded.getOnlineFileId(), fileLocal);
            int counter = 0;
            for (Entities.File f : docs) {
                if (f.getFileId() == uploaded.getLocalFileId()) {
                    f.setFileId(uploaded.getOnlineFileId());
                    notifyItemChanged(counter);
                }
                counter++;
            }
        }
    }

    public void onFileUploadCancelled(FileUploadCancelled cancelled) {
        if (cancelled.getDocType() == Video) {
            synchronized (docs) {
                int counter = 0;
                for (Entities.File doc : docs) {
                    if (doc.getFileId() == cancelled.getLocalFileId()) {
                        break;
                    }
                    counter++;
                }
                docs.remove(counter);
                notifyItemRemoved(counter);
            }
        }
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_videos, parent, false);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(blockSize, blockSize));
        return new Holder(itemView);
    }

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        holder.cleanup();
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {
        final Entities.Video doc = docs.get(position);
        holder.titleTV.setText(doc.getTitle());
        String path = fileLocals.get(doc.getFileId()).getPath();
        if (!new File(path).exists()) {
            path = DatabaseHelper.getFilePath(doc.getFileId());
        }
        if (!new File(path).exists()) {
            path = NetworkHelper.createFileLink(doc.getFileId());
        }
        GlideApp.with(Core.getInstance()).load(path).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                imageNotLoaded(holder);
                return true;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                imageLoaded(holder, resource);
                return true;
            }
        }).into(holder.iconIV);
        final String finalPath = path;
        final Entities.FileLocal fileLocal = fileLocals.get(doc.getFileId());
        if (fileLocal.isTransferring()) {
            holder.downloadBTN.setVisibility(View.GONE);
            holder.playBTN.setVisibility(View.GONE);
            holder.loadingContainer.setVisibility(View.VISIBLE);
            holder.loadingPB.setProgress(fileLocal.getProgress());
        } else {
            if (!new File(path).exists()) {
                holder.downloadBTN.setVisibility(View.VISIBLE);
                holder.playBTN.setVisibility(View.GONE);
                holder.loadingContainer.setVisibility(View.GONE);
                holder.downloadBTN.setOnClickListener(v ->
                        Dexter.withActivity(activity)
                                .withPermissions(
                                        Manifest.permission.READ_EXTERNAL_STORAGE,
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                .withListener(new MultiplePermissionsListener() {
                                    @Override
                                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                                        if (report.areAllPermissionsGranted()) {
                                            DatabaseHelper.notifyFileDownloading(doc.getFileId());
                                            Core.getInstance().bus().post(new FileDownloading(Video, doc));
                                            NetworkHelper.downloadFile(doc, roomId
                                                    , progress -> {
                                                        DatabaseHelper.notifyFileTransferProgressed(doc.getFileId(), progress);
                                                        activity.runOnUiThread(() ->
                                                                Core.getInstance().bus().post(new FileTransferProgressed(Video, doc.getFileId(), progress)));
                                                    }, new OnFileDownloadListener() {
                                                        @Override
                                                        public void fileDownloaded() {
                                                            DatabaseHelper.notifyFileDownloaded(doc.getFileId());
                                                            activity.runOnUiThread(() ->
                                                                    Core.getInstance().bus().post(new FileDownloaded(Video, doc.getFileId())));
                                                        }

                                                        @Override
                                                        public void downloadFailed() {

                                                        }
                                                    });
                                        } else {
                                            activity.finish();
                                        }
                                    }

                                    @Override
                                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                                        token.continuePermissionRequest();
                                    }
                                })
                                .onSameThread()
                                .check());
            } else {
                holder.downloadBTN.setVisibility(View.GONE);
                holder.playBTN.setVisibility(View.VISIBLE);
                holder.loadingContainer.setVisibility(View.GONE);
                holder.itemView.setOnClickListener(v ->
                        activity.startActivity(new Intent(activity, VideoPlayerActivity.class)
                                .putExtra("video-path", finalPath)));
            }
        }

        holder.itemView.setOnLongClickListener(v -> {
            selectCallback.docLongClicked(doc);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return this.docs.size();
    }

    private static void imageLoaded(Holder holder, Drawable drawable) {
        holder.iconIV.setVisibility(View.VISIBLE);
        holder.playBTN.setVisibility(View.GONE);
        holder.iconIV.setImageDrawable(drawable);
    }

    private static void imageNotLoaded(Holder holder) {
        holder.iconIV.setVisibility(View.GONE);
        holder.playBTN.setVisibility(View.VISIBLE);
        holder.playBTN.setImageResource(R.drawable.ic_video);
    }

    class Holder extends RecyclerView.ViewHolder {

        RelativeLayout mainLayout;
        TextView titleTV;
        ImageView iconIV;
        BlurView blurView;
        ImageView downloadBTN;
        FrameLayout loadingContainer;
        CircularProgressBar loadingPB;
        ImageView playBTN;

        Holder(View itemView) {

            super(itemView);
            mainLayout = itemView.findViewById(R.id.adapter_videos_main_layout);
            iconIV = itemView.findViewById(R.id.adapter_videos_icon_image_view);
            blurView = itemView.findViewById(R.id.adapter_videos_blur_view);
            downloadBTN = itemView.findViewById(R.id.adapter_videos_download_image_button);
            loadingContainer = itemView.findViewById(R.id.adapter_videos_loading_container);
            loadingPB = itemView.findViewById(R.id.adapter_videos_loading_progress_bar);
            playBTN = itemView.findViewById(R.id.adapter_videos_play_image_view);
            titleTV = itemView.findViewById(R.id.adapter_videos_title_text_view);
        }

        void cleanup() {
            GlideApp.with(Core.getInstance()).clear(this.iconIV);
        }
    }
}