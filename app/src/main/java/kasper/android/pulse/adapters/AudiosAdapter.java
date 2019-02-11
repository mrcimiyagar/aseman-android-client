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
import kasper.android.pulse.callbacks.network.OnFileDownloadListener;
import kasper.android.pulse.callbacks.ui.FileListener;
import kasper.android.pulse.callbacks.ui.OnDocSelectListener;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
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

        GraphicHelper.addFileListener(new FileListener() {
            @Override
            public void fileUploaded(DocTypes docType, long localFileId, long onlineFileId) {
                try {
                    if (docType == Audio) {
                        Entities.FileLocal fileLocal = fileLocals.remove(localFileId);
                        fileLocal.setFileId(onlineFileId);
                        fileLocal.setTransferring(false);
                        fileLocals.put(onlineFileId, fileLocal);
                        int counter = 0;
                        for (Entities.File f : docs) {
                            if (f.getFileId() == localFileId) {
                                f.setFileId(onlineFileId);
                                notifyItemChanged(counter);
                            }
                            counter++;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void fileUploading(DocTypes docTypes, Entities.File file, Entities.FileLocal fileLocal) {
                try {
                    if (docTypes == Audio) {
                        docs.add((Entities.Audio) file.clone());
                        fileLocals.put(fileLocal.getFileId(), fileLocal.clone());
                        notifyItemInserted(fileLocals.size() - 1);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void fileUploadCancelled(DocTypes docType, long localFileId) {
                try {
                    if (docType == Audio) {
                        synchronized (docs) {
                            int counter = 0;
                            for (Entities.File doc : docs) {
                                if (doc.getFileId() == localFileId) {
                                    break;
                                }
                                counter++;
                            }
                            docs.remove(counter);
                            notifyItemRemoved(counter);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void fileDownloaded(DocTypes docType, long localFileId) {
                try {
                    if (docType == Audio) {
                        synchronized (docs) {
                            int counter = 0;
                            for (Entities.File doc : docs) {
                                if (doc.getFileId() == localFileId) {
                                    Entities.FileLocal fileLocal = fileLocals.get(doc.getFileId());
                                    fileLocal.setTransferring(false);
                                    notifyItemChanged(counter);
                                    break;
                                }
                                counter++;
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void fileDownloading(DocTypes docType, Entities.File file) {

            }

            @Override
            public void fileDownloadCancelled(DocTypes docType, long fileId) {
                try {
                    if (docType == Audio) {
                        int counter = 0;
                        for (Entities.File doc : docs) {
                            if (doc.getFileId() == fileId) {
                                fileLocals.get(doc.getFileId()).setTransferring(false);
                                notifyItemChanged(counter);
                                break;
                            }
                            counter++;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void fileTransferProgressed(DocTypes docType, long fileId, int progress) {
                try {
                    if (docType == Audio) {
                        synchronized (docs) {
                            int counter = 0;
                            for (Entities.File doc : docs) {
                                if (doc.getFileId() == fileId) {
                                    Entities.FileLocal fileLocal = fileLocals.get(doc.getFileId());
                                    fileLocal.setProgress(progress);
                                    notifyItemChanged(counter);
                                    break;
                                }
                                counter++;
                            }
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            @Override
            public void fileReceived(DocTypes docType, Entities.File file, Entities.FileLocal fileLocal) {
                try {
                    if (docType == Audio) {
                        docs.add((Entities.Audio) file.clone());
                        fileLocals.put(fileLocal.getFileId(), fileLocal.clone());
                        notifyItemInserted(fileLocals.size() - 1);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        this.notifyDataSetChanged();
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
                                            DatabaseHelper.notifyFileDownloading(doc.getFileId());
                                            for (FileListener fileListener : GraphicHelper.getFileListeners()) {
                                                fileListener.fileDownloading(Audio, doc);
                                            }
                                            NetworkHelper.downloadFile(doc, roomId
                                                    , progress -> {
                                                        DatabaseHelper.notifyFileTransferProgressed(doc.getFileId(), progress);
                                                        activity.runOnUiThread(() ->
                                                                GraphicHelper.getFileListener().fileTransferProgressed(Audio, doc.getFileId(), progress));
                                                    }, new OnFileDownloadListener() {
                                                        @Override
                                                        public void fileDownloaded() {
                                                            DatabaseHelper.notifyFileDownloaded(doc.getFileId());
                                                            activity.runOnUiThread(() ->
                                                                    GraphicHelper.getFileListener().fileDownloaded(Audio, doc.getFileId()));
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