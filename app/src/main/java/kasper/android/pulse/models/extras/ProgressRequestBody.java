package kasper.android.pulse.models.extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.FileHandler;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import retrofit2.Call;

public class ProgressRequestBody extends RequestBody {
    private long fileId;
    private DocTypes docType;
    private File mFile;
    private String content_type;
    private boolean cancel = false;

    private static final int DEFAULT_BUFFER_SIZE = 2048;

    public ProgressRequestBody(long fileId, DocTypes docType, final File file, String content_type) {
        this.fileId = fileId;
        this.docType = docType;
        this.content_type = content_type;
        this.mFile = file;
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(content_type+"/*");
    }

    @Override
    public long contentLength() {
        return mFile.length();
    }

    private int notifiedProgress = 0;

    public void cancelStream(){
        cancel = true;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        try {
            Packet packet = new Packet();
            Entities.File f = new Entities.File();
            f.setFileId(fileId);
            packet.setFile(f);
            Call<Packet> fileSizeCall = NetworkHelper.getRetrofit().create(FileHandler.class).getFileSize(packet);
            if (cancel) return;
            Packet res = fileSizeCall.execute().body();
            long writePos = 0;
            if (res != null)
                writePos = res.getFile().getSize();
            long fileLength = mFile.length();
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            if (cancel) return;
            try (FileInputStream in = new FileInputStream(mFile)) {
                in.skip(writePos);
                long uploaded = writePos;
                int read;
                while ((read = in.read(buffer)) != -1 && !cancel) {
                    int progress = (int) (100 * uploaded / fileLength);
                    if (progress - notifiedProgress > 0) {
                        DatabaseHelper.notifyFileTransferProgressed(fileId, progress);
                        Core.getInstance().bus().post(new FileTransferProgressed(docType, fileId, progress));
                        notifiedProgress = progress;
                    }
                    uploaded += read;
                    try {
                        sink.write(buffer, 0, read);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}