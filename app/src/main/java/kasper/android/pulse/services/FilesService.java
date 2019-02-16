package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.FileRequestBody;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.FileHandler;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Callback;
import retrofit2.Response;

public class FilesService extends IntentService {

    private static Entities.File currentUploadingFile;
    private static Call currentUploadingCall;

    private static Entities.File currentDownloadingFile;

    private static BlockingQueue<Uploading> uploadingFiles = new LinkedBlockingQueue<>();
    private static Thread uploaderThread;

    private static final BlockingQueue<Downloading> downloadingFiles = new LinkedBlockingQueue<>();
    private static Thread downloaderThread;

    private static boolean skipDownload = false;

    public FilesService() {
        super("FileService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

    }

    public static void uploadFile(Uploading uploading) {
        uploadingFiles.offer(uploading);
    }

    public static void downloadFile(Downloading downloading) {
        downloadingFiles.offer(downloading);
    }

    public static void cancelUpload(long fileId) {
        synchronized (uploadingFiles) {
            if (currentUploadingFile != null && currentUploadingFile.getFileId() == fileId) {
                currentUploadingCall.cancel();
            } else {
                for (Uploading uploading : uploadingFiles) {
                    if (uploading.getFile().getFileId() == fileId) {
                        uploadingFiles.remove(uploading);
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
            } else {
                for (Downloading downloading : downloadingFiles) {
                    if (downloading.getFile().getFileId() == fileId) {
                        downloadingFiles.remove(downloading);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("KasperLogger", "File service started");
        if (uploaderThread == null) {
            uploaderThread = new Thread(() -> {
                try {
                    while (true) {
                        Uploading uploading = uploadingFiles.take();
                        Entities.File fileEntity = uploading.getFile();
                        currentUploadingFile = fileEntity;
                        retrofit2.Call<Packet> call = null;
                        if (fileEntity instanceof Entities.Photo) {
                            Entities.Photo photo = (Entities.Photo) fileEntity;
                            if (uploading.isAvatarUsage())
                                compressImage(uploading.getPath());
                            Map<String, RequestBody> parts = new HashMap<>();
                            parts.put("ComplexId", createRequestBody(uploading.getComplexId()));
                            parts.put("RoomId", createRequestBody(uploading.getRoomId()));
                            parts.put("Width", createRequestBody(photo.getWidth()));
                            parts.put("Height", createRequestBody(photo.getHeight()));
                            parts.put("IsAvatar", createRequestBody(uploading.isAvatarUsage()));
                            MultipartBody.Part filePart = createFileBody(uploading.isAvatarUsage() ? new File(
                                    new File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir)
                                    , "uploadTemp").getPath() : uploading.getPath());
                            call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadPhoto(parts, filePart);
                        } else if (fileEntity instanceof Entities.Audio) {
                            Entities.Audio audio = (Entities.Audio) fileEntity;
                            Map<String, RequestBody> parts = new HashMap<>();
                            parts.put("ComplexId", createRequestBody(uploading.getComplexId()));
                            parts.put("RoomId", createRequestBody(uploading.getRoomId()));
                            parts.put("Width", createRequestBody(audio.getTitle()));
                            parts.put("Height", createRequestBody(audio.getDuration()));
                            MultipartBody.Part filePart = createFileBody(uploading.getPath());
                            call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadAudio(parts, filePart);
                        } else if (fileEntity instanceof Entities.Video) {
                            Entities.Video video = (Entities.Video) fileEntity;
                            Map<String, RequestBody> parts = new HashMap<>();
                            parts.put("ComplexId", createRequestBody(uploading.getComplexId()));
                            parts.put("RoomId", createRequestBody(uploading.getRoomId()));
                            parts.put("Width", createRequestBody(video.getTitle()));
                            parts.put("Height", createRequestBody(video.getDuration()));
                            MultipartBody.Part filePart = createFileBody(uploading.getPath());
                            call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadVideo(parts, filePart);
                        }
                        if (call != null) {
                            NetworkHelper.requestServer(call, new ServerCallback() {
                                @Override
                                public void onRequestSuccess(Packet packet) {
                                    Entities.File uploadedFile = packet.getFile();
                                    Entities.FileUsage createdFileUsage = packet.getFileUsage();
                                    try {
                                        uploading.getUploadListener().fileUploaded(
                                                uploadedFile.getFileId(),
                                                createdFileUsage != null ? createdFileUsage.getFileUsageId() : -1);
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                }
                                @Override
                                public void onServerFailure() {
                                    Core.getInstance().bus().post(new ShowToast("File upload failure"));
                                }
                                @Override
                                public void onConnectionFailure() {
                                    Core.getInstance().bus().post(new ShowToast("File upload failure"));
                                }
                            });
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            downloaderThread = new Thread(() -> {
                try {
                    while (true) {
                        Downloading downloading = downloadingFiles.take();
                        currentDownloadingFile = downloading.getFile();
                        skipDownload = false;
                        FileHandler fileHandler = NetworkHelper.getRetrofit().create(FileHandler.class);
                        retrofit2.Call<ResponseBody> call = fileHandler.downloadFile(downloading.getFile().getFileId());
                        ResponseBody body = call.execute().body();
                        try {
                            if (body != null) {
                                writeResponseBodyToDisk(body, downloading.getFile().getFileId(), downloading);
                            }
                            Core.getInstance().bus().post(new UiThreadRequested(() ->
                                    downloading.getDownloadListener().fileDownloaded()));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            Core.getInstance().bus().post(new UiThreadRequested(() ->
                                    downloading.getDownloadListener().downloadFailed()));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            uploaderThread.start();
            downloaderThread.start();
        }
        return START_NOT_STICKY;
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

    private MultipartBody.Part createFileBody(String filePath) {
        File file = new File(filePath);
        return MultipartBody.Part.createFormData("File", file.getName()
                , RequestBody.create(
                        MediaType.parse("multipart/form-data"),
                        file
                ));
    }

    private RequestBody createRequestBody(Object param) {
        return RequestBody.create(MediaType.parse("multipart/form-data"), param.toString());
    }

    private void writeResponseBodyToDisk(ResponseBody body, long fileId, Downloading downloading) {
        try {
            File futureStudioIconFile = new File(new File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir), fileId + "");
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1)
                        break;
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    downloading.getProgressListener().transferred((int)(fileSizeDownloaded * 100 / fileSize));
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

    @Override
    public void onDestroy() {
        Log.d("KasperLogger", "File service destroyed");
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("KasperLogger", "File service task removed");
    }
}