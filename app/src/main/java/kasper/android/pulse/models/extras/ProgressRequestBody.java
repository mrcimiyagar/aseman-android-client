package kasper.android.pulse.models.extras;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import androidx.annotation.NonNull;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class ProgressRequestBody extends RequestBody {
    private long fileId;
    private DocTypes docType;
    private File mFile;
    private String content_type;

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

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        long fileLength = mFile.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

        try (FileInputStream in = new FileInputStream(mFile)) {
            long uploaded = 0;
            int read;
            while ((read = in.read(buffer)) != -1) {
                int progress = (int) (100 * uploaded / fileLength);
                Core.getInstance().bus().post(new FileTransferProgressed(docType, fileId, progress));
                uploaded += read;
                sink.write(buffer, 0, read);
            }
        }
    }
}