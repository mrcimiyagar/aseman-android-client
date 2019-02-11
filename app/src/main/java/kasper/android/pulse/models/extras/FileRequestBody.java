package kasper.android.pulse.models.extras;

import android.util.Log;

import java.io.File;
import java.io.IOException;

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
    public void writeTo(BufferedSink sink) throws IOException {
        Source source = null;
        try {
            source = Okio.source(file);
            long total = 0;
            long read;

            while ((read = source.read(sink.buffer(), SEGMENT_SIZE)) != -1) {
                total += read;
                sink.flush();
                this.listener.transferred((int)(total * 100 / fileSize));
            }
        } finally {
            Util.closeQuietly(source);
        }
    }
}
