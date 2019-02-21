package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.ProgressListener;
import kasper.android.pulse.models.extras.ProgressRequestBody;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.FileHandler;
import kasper.android.pulse.rxbus.notifications.ShowToast;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;

public class FilesService extends IntentService {

    private static Entities.File currentUploadingFile;
    private static Call currentUploadingCall;

    private static Entities.File currentDownloadingFile;
    private static Call currentDownloadingCall;

    private static final BlockingQueue<Uploading> uploadingFiles = new LinkedBlockingQueue<>();
    private static Thread uploaderThread;

    private static final BlockingQueue<Downloading> downloadingFiles = new LinkedBlockingQueue<>();
    private static Thread downloaderThread;

    private static boolean skipDownload = false;
    private boolean alive = false;

    public FilesService() {
        super("FileService");
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

        LogHelper.log("Aseman", "File service started");

        alive = true;

        if (uploaderThread == null) {
            uploaderThread = new Thread(() -> {
                try {
                    while (alive) {
                        Uploading uploading = uploadingFiles.take();
                        try {
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
                                File file = uploading.isAvatarUsage() ? new File(
                                        new File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir)
                                        , "uploadTemp") : new File(uploading.getPath());
                                if (file.exists()) {
                                    MultipartBody.Part filePart = createFileBody(
                                            currentUploadingFile.getFileId(), DocTypes.Photo, file.getPath());
                                    call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadPhoto(parts, filePart);
                                } else {
                                    uploading.getUploadListener().fileUploaded(0, -1);
                                }
                            } else if (fileEntity instanceof Entities.Audio) {
                                Entities.Audio audio = (Entities.Audio) fileEntity;
                                Map<String, RequestBody> parts = new HashMap<>();
                                parts.put("ComplexId", createRequestBody(uploading.getComplexId()));
                                parts.put("RoomId", createRequestBody(uploading.getRoomId()));
                                parts.put("Title", createRequestBody(audio.getTitle()));
                                parts.put("Duration", createRequestBody(audio.getDuration()));
                                File file = new File(uploading.getPath());
                                if (file.exists()) {
                                    MultipartBody.Part filePart = createFileBody(
                                            currentUploadingFile.getFileId(), DocTypes.Audio, file.getPath());
                                    call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadAudio(parts, filePart);
                                } else {
                                    uploading.getUploadListener().fileUploaded(0, -1);
                                }
                            } else if (fileEntity instanceof Entities.Video) {
                                Entities.Video video = (Entities.Video) fileEntity;
                                Map<String, RequestBody> parts = new HashMap<>();
                                parts.put("ComplexId", createRequestBody(uploading.getComplexId()));
                                parts.put("RoomId", createRequestBody(uploading.getRoomId()));
                                parts.put("Title", createRequestBody(video.getTitle()));
                                parts.put("Duration", createRequestBody(video.getDuration()));
                                File file = new File(uploading.getPath());
                                if (file.exists()) {
                                    MultipartBody.Part filePart = createFileBody(
                                            currentUploadingFile.getFileId(), DocTypes.Video, file.getPath());
                                    call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadVideo(parts, filePart);
                                } else {
                                    uploading.getUploadListener().fileUploaded(0, -1);
                                }
                            }
                            currentUploadingCall = call;
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
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            uploadingFiles.offer(uploading);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            uploaderThread.start();
        }

        if (downloaderThread == null) {
            downloaderThread = new Thread(() -> {
                try {
                    while (alive) {
                        Downloading downloading = downloadingFiles.take();
                        currentDownloadingFile = downloading.getFile();
                        skipDownload = false;
                        FileHandler fileHandler = NetworkHelper.getRetrofit().create(FileHandler.class);
                        retrofit2.Call<ResponseBody> call = fileHandler.downloadFile(downloading.getFile().getFileId());
                        currentDownloadingCall = call;
                        ResponseBody body = call.execute().body();
                        try {
                            if (body != null)
                                writeResponseBodyToDisk(body, downloading.getFile().getFileId(), downloading);
                            if (!skipDownload)
                                Core.getInstance().bus().post(new UiThreadRequested(() ->
                                        downloading.getDownloadListener().fileDownloaded()));
                            else
                                skipDownload = false;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            downloadingFiles.offer(downloading);
                            Core.getInstance().bus().post(new UiThreadRequested(() ->
                                    downloading.getDownloadListener().downloadFailed()));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            downloaderThread.start();
        }

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.log("Aseman", "File service destroyed");
        alive = false;
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.e("Aseman", "File service task removed");
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
                currentDownloadingCall.cancel();
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

    private MultipartBody.Part createFileBody(long fileId, DocTypes docType, String filePath) {
        File file = new File(filePath);
        return MultipartBody.Part.createFormData("File", file.getName()
                , new ProgressRequestBody(fileId, docType, file, "*/*"));
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
                while (!skipDownload) {
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
}