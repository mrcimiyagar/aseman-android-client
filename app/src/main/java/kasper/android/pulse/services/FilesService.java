package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.FileRequestBody;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.retrofit.FileHandler;
import kasper.android.pulse.rxbus.notifications.UiThreadRequested;
import okhttp3.Call;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class FilesService extends IntentService {

    private static Entities.File currentUploadingFile;
    private static Call currentUploadingCall;

    private static Entities.File currentDownloadingFile;

    private static BlockingQueue<Uploading> uploadingFiles = new LinkedBlockingQueue<>();
    private static Thread uploaderThread;

    private static OkHttpClient client;

    private static BlockingQueue<Downloading> downloadingFiles = new LinkedBlockingQueue<>();
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
            client = new OkHttpClient.Builder()
                    .connectTimeout(20, TimeUnit.DAYS)
                    .writeTimeout(20, TimeUnit.DAYS)
                    .readTimeout(20, TimeUnit.DAYS)
                    .build();
            uploaderThread = new Thread(() -> {
                try {
                    while (true) {
                        Uploading uploading = uploadingFiles.take();
                        Entities.File file = uploading.getFile();
                        currentUploadingFile = file;
                        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM)
                                .addFormDataPart("file", "File"
                                        , new FileRequestBody(new File(uploading.getPath())
                                        , "*/*", uploading.getProgressListener()));
                        Entities.Session session = DatabaseHelper.getSingleSession();
                        if (session != null) {
                            String authorization = session.getSessionId() + " " + session.getToken();
                            builder.addFormDataPart("ComplexId", uploading.getComplexId() + "");
                            builder.addFormDataPart("RoomId", uploading.getRoomId() + "");
                            if (file instanceof Entities.Photo) {
                                Entities.Photo photo = (Entities.Photo) file;
                                builder.addFormDataPart("Width", photo.getWidth() + "");
                                builder.addFormDataPart("Height", photo.getHeight() + "");
                            } else if (file instanceof Entities.Audio) {
                                Entities.Audio audio = (Entities.Audio) file;
                                builder.addFormDataPart("Title", audio.getTitle());
                                builder.addFormDataPart("Duration", audio.getDuration() + "");
                            } else if (file instanceof Entities.Video) {
                                Entities.Video video = (Entities.Video) file;
                                builder.addFormDataPart("Title", video.getTitle());
                                builder.addFormDataPart("Duration", video.getDuration() + "");
                            }
                            RequestBody requestBody = builder.build();
                            Request request = new Request.Builder()
                                    .url(NetworkHelper.API_PATH + ((file instanceof Entities.Photo) ? "file/upload_photo"
                                            : (file instanceof Entities.Audio) ? "file/upload_audio" : "file/upload_video"))
                                    .addHeader("Authorization", authorization)
                                    .post(requestBody)
                                    .build();
                            try {
                                Call call = client.newCall(request);
                                currentUploadingCall = call;
                                Response response = call.execute();
                                if (response.body() != null) {
                                    String result = response.body().string();
                                    Log.d("Aseman", "File Uploaded : " + result);
                                    JSONObject mainJO = new JSONObject(result);
                                    if (mainJO.getString("status").equals("success")) {
                                        Core.getInstance().bus().post(new UiThreadRequested(() -> {
                                            try {
                                                uploading.getUploadListener().fileUploaded(
                                                        mainJO.getJSONObject("file").getLong("fileId"),
                                                        mainJO.has("fileUsage") ? mainJO.getJSONObject
                                                                ("fileUsage").getLong("fileUsageId") : -1);
                                            } catch (Exception ex) {
                                                ex.printStackTrace();
                                            }
                                        }));
                                    }
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    }
                } catch (Exception ignored) {

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
        return START_STICKY;
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