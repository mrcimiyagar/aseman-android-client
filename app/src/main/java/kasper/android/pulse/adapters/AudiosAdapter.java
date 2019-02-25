package kasper.android.pulse.adapters;

import android.Manifest;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.anadeainc.rxbus.Subscribe;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import java.io.File;
import java.util.Hashtable;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.ui.OnDocSelectListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.rxbus.notifications.FileDownloadCancelled;
import kasper.android.pulse.rxbus.notifications.FileDownloaded;
import kasper.android.pulse.rxbus.notifications.FileDownloading;
import kasper.android.pulse.rxbus.notifications.FileReceived;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploadCancelled;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.FileUploading;
import kasper.android.pulse.services.FilesService;
import kasper.android.pulse.services.MusicsService;

import static kasper.android.pulse.models.extras.DocTypes.Audio;

public class AudiosAdapter extends RecyclerView.Adapter<AudiosAdapter.Holder> {

    private AppCompatActivity activity;
    private final List<Entities.Audio> docs;
    private Hashtable<Long, Entities.FileLocal> fileLocals;
    private long roomId;
    private OnDocSelectListener selectCallback;

    public AudiosAdapter(AppCompatActivity activity, List<Entities.Audio> ds, Hashtable<Long
            , Entities.FileLocal> fs, long roomId, OnDocSelectListener selectCallback) {
        this.activity = activity;
        this.docs = ds;
        this.fileLocals = fs;
        this.roomId = roomId;
        this.selectCallback = selectCallback;
        Core.getInstance().bus().register(this);
        this.notifyDataSetChanged();
    }

    public void dispose() {
        Core.getInstance().bus().unregister(this);
    }

    @Subscribe
    public void onFileReceived(FileReceived fileReceived) {
        if (fileReceived.getDocType() == Audio) {
            docs.add((Entities.Audio) fileReceived.getFile().clone());
            fileLocals.put(fileReceived.getFileLocal().getFileId(), fileReceived.getFileLocal().clone());
            notifyItemInserted(fileLocals.size() - 1);
        }
    }

    @Subscribe
    public void onFileTransferProgressed(FileTransferProgressed progressed) {
        if (progressed.getDocType() == Audio) {
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
        if (cancelled.getDocType() == Audio) {
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
        if (downloaded.getDocType() == Audio) {
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
        if (uploading.getDocType() == Audio) {
            docs.add((Entities.Audio) uploading.getFile().clone());
            fileLocals.put(uploading.getFileLocal().getFileId(), uploading.getFileLocal().clone());
            notifyItemInserted(docs.size() - 1);
        }
    }

    @Subscribe
    public void onFileUploaded(FileUploaded uploaded) {
        if (uploaded.getDocType() == Audio) {
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
        if (cancelled.getDocType() == Audio) {
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_audios, parent, false);
        return new Holder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        final Entities.Audio doc = docs.get(position);

        holder.titleTV.setText(doc.getTitle());

        String path = fileLocals.get(doc.getFileId()).getPath();

        if (!new File(path).exists()) {
            path = DatabaseHelper.getFilePath(doc.getFileId());
        }

        if (!new File(path).exists()) {
            holder.playBTN.setImageResource(R.drawable.ic_download);
        }
        else {
            holder.playBTN.setImageResource(R.drawable.ic_play);
        }

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
                                            FilesService.downloadFile(new Downloading(doc.getFileId(), roomId));
                                            Core.getInstance().bus().post(new FileDownloading(Audio, doc));
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
                        activity.startService(new Intent(activity, MusicsService.class)
                                .putExtra("command", "play")
                                .putExtra("path", fileLocal.getPath())
                ));
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

    class Holder extends RecyclerView.ViewHolder {

        TextView titleTV;
        ImageView playBTN;
        ImageView downloadBTN;
        FrameLayout loadingContainer;
        CircularProgressBar loadingPB;

        Holder(View itemView) {

            super(itemView);
            playBTN = itemView.findViewById(R.id.adapter_audios_play_image_view);
            titleTV = itemView.findViewById(R.id.adapter_audios_title_text_view);
            downloadBTN = itemView.findViewById(R.id.adapter_audios_download_image_button);
            loadingContainer = itemView.findViewById(R.id.adapter_audios_loading_container);
            loadingPB = itemView.findViewById(R.id.adapter_audios_loading_progress_bar);
        }
    }
}