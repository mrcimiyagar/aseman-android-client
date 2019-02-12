package kasper.android.pulse.models.extras;

import java.io.File;
import java.io.IOException;

import androidx.annotation.NonNull;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class FileRequestBody extends RequestBody {

    private static final int SEGMENT_SIZE = 2048;

    private final File file;
    private final ProgressListener listener;
    private final String contentType;
    private long fileSize;

    public FileRequestBody(File file, String contentType, ProgressListener listener) {
        this.file = file;
        this.contentType = contentType;
        this.listener = listener;
        this.fileSize = file.length();
    }

    @Override
    public long contentLength() {
        return file.length();
    }

    @Override
    public MediaType contentType() {
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;

            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                long finalTotal = total;
                Core.getInstance().bus().post(new UiThreadRequested(() ->
                        listener.transferred((int)(finalTotal * 100 / fileSize))));
            }
        } finally {
            Util.closeQuietly(source);
        }
    }
}
