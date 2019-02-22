package kasper.android.pulse.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.Tuple;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.Downloading;
import kasper.android.pulse.models.extras.ProgressRequestBody;
import kasper.android.pulse.models.extras.Uploading;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.FileHandler;
import kasper.android.pulse.rxbus.notifications.FileDownloaded;
import kasper.android.pulse.rxbus.notifications.FileTransferProgressed;
import kasper.android.pulse.rxbus.notifications.FileUploaded;
import kasper.android.pulse.rxbus.notifications.FileUploading;
import kasper.android.pulse.rxbus.notifications.MessageSending;
import kasper.android.pulse.rxbus.notifications.ShowToast;
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

    private static final LinkedBlockingDeque<Tuple<Entities.File, Entities.FileLocal, Entities.Message
            , Entities.MessageLocal, Uploading>> uploadingFiles = new LinkedBlockingDeque<>();
    private static Thread uploaderThread;

    private static final LinkedBlockingDeque<Downloading> downloadingFiles = new LinkedBlockingDeque<>();
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
                        Tuple<Entities.File, Entities.FileLocal, Entities.Message
                                , Entities.MessageLocal, Uploading> tuple = uploadingFiles.take();
                        Entities.File fileEntity = tuple.first;
                        Entities.FileLocal fileLocalEntity = tuple.second;
                        Entities.Message messageEntity = tuple.third;
                        Entities.MessageLocal messageLocalEntity = tuple.fourth;
                        Uploading uploading = tuple.fifth;
                        try {
                            retrofit2.Call<Packet> call = null;
                            if (fileEntity instanceof Entities.Photo) {
                                Entities.Photo photo = (Entities.Photo) fileEntity;
                                if (uploading.isCompress())
                                    compressImage(uploading.getPath());
                                Map<String, RequestBody> parts = new HashMap<>();
                                parts.put("ComplexId", createRequestBody(uploading.getComplexId()));
                                parts.put("RoomId", createRequestBody(uploading.getRoomId()));
                                parts.put("Width", createRequestBody(photo.getWidth()));
                                parts.put("Height", createRequestBody(photo.getHeight()));
                                parts.put("IsAvatar", createRequestBody(uploading.isCompress()));
                                File file = uploading.isCompress() ? new File(
                                        new File(Environment.getExternalStorageDirectory(), DatabaseHelper.StorageDir)
                                        , "uploadTemp") : new File(uploading.getPath());
                                if (file.exists()) {
                                    MultipartBody.Part filePart = createFileBody(
                                            currentUploadingFile.getFileId(), DocTypes.Photo, file.getPath());
                                    call = NetworkHelper.getRetrofit().create(FileHandler.class).uploadPhoto(parts, filePart);
                                } else
                                    Core.getInstance().bus().post(new FileUploaded(DocTypes.Photo
                                            , fileEntity.getFileId(), 0, -1
                                            , uploading.getComplexId(), uploading.getRoomId(), fileEntity, messageEntity));
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
                                } else
                                    Core.getInstance().bus().post(new FileUploaded(DocTypes.Photo
                                            , fileEntity.getFileId(), 0, -1
                                            , uploading.getComplexId(), uploading.getRoomId(), fileEntity, messageEntity));
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
                                } else
                                    Core.getInstance().bus().post(new FileUploaded(DocTypes.Photo
                                            , fileEntity.getFileId(), 0, -1
                                            , uploading.getComplexId(), uploading.getRoomId(), fileEntity, messageEntity));
                            }
                            currentUploadingCall = call;
                            if (call != null) {
                                final long localFileId = fileEntity.getFileId();
                                NetworkHelper.requestServer(call, new ServerCallback() {
                                    @Override
                                    public void onRequestSuccess(Packet packet) {
                                        Entities.File uploadedFile = packet.getFile();
                                        Entities.FileUsage createdFileUsage = packet.getFileUsage();
                                        if (fileEntity instanceof Entities.Photo)
                                            DatabaseHelper.notifyPhotoUploaded(localFileId, uploadedFile.getFileId());
                                        else if (fileEntity instanceof Entities.Audio)
                                            DatabaseHelper.notifyAudioUploaded(localFileId, uploadedFile.getFileId());
                                        else if (fileEntity instanceof Entities.Video)
                                            DatabaseHelper.notifyVideoUploaded(localFileId, uploadedFile.getFileId());
                                        DatabaseHelper.notifyUpdateMessageAfterFileUpload(messageEntity.getMessageId()
                                                , uploadedFile.getFileId(), createdFileUsage.getFileUsageId());
                                        Core.getInstance().bus().post(new FileUploaded(fileEntity instanceof
                                                Entities.Photo ? DocTypes.Photo : fileEntity instanceof Entities.Audio
                                                ? DocTypes.Audio : DocTypes.Video, localFileId, uploadedFile.getFileId()
                                                , createdFileUsage == null ? -1 : createdFileUsage.getFileUsageId()
                                                , uploading.getComplexId(), uploading.getRoomId(), fileEntity, messageEntity));
                                    }
                                    @Override
                                    public void onServerFailure() {
                                        Core.getInstance().bus().post(new ShowToast("File upload failure"));
                                        uploadingFiles.offerFirst(tuple);
                                    }
                                    @Override
                                    public void onConnectionFailure() {
                                        Core.getInstance().bus().post(new ShowToast("File upload failure"));
                                        uploadingFiles.offerFirst(tuple);
                                    }
                                });
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            uploadingFiles.offer(tuple);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!uploaderThread.isAlive())
            uploaderThread.start();

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
                            if (body != null) {
                                writeResponseBodyToDisk(body, downloading.getFile().getFileId(), downloading);
                            }
                            if (!skipDownload) {
                                DatabaseHelper.notifyFileDownloaded(downloading.getFile().getFileId());
                                Core.getInstance().bus().post(new FileDownloaded(downloading.getFile()
                                        instanceof Entities.Photo ? DocTypes.Photo : downloading.getFile()
                                        instanceof Entities.Audio ? DocTypes.Audio : DocTypes.Video, downloading.getFile().getFileId()));
                            } else
                                skipDownload = false;
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            downloadingFiles.offerFirst(downloading);
                            Core.getInstance().bus().post(new ShowToast("File download failed."));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        if (!downloaderThread.isAlive())
            downloaderThread.start();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        LogHelper.log("Aseman", "File service destroyed");
        super.onDestroy();
    }

    public static long uploadFile(Uploading uploading) {
        Entities.File fileEntity;
        Entities.FileLocal fileLocalEntity;
        Entities.Message messageEntity;
        Entities.MessageLocal messageLocalEntity;
        if (uploading.getDocType() == DocTypes.Photo) {
            try {
                FileInputStream inputStream = new FileInputStream(new File(uploading.getPath()));
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, bitmapOptions);
                int imageWidth = bitmapOptions.outWidth;
                int imageHeight = bitmapOptions.outHeight;
                inputStream.close();
                Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper
                        .notifyPhotoUploading(false, uploading.getPath()
                                , imageWidth, imageHeight);
                fileEntity = filePair.first;
                fileLocalEntity = filePair.second;
                currentUploadingFile = fileEntity;
                Core.getInstance().bus().post(new FileUploading(DocTypes.Photo, fileEntity, fileLocalEntity));
                if (uploading.isAttachToMessage()) {
                    Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper
                            .notifyPhotoMessageSending(uploading.getRoomId(), fileEntity.getFileId());
                    messageEntity = msgPair.first;
                    messageLocalEntity = msgPair.second;
                    Core.getInstance().bus().post(new MessageSending(messageEntity, messageLocalEntity));
                } else {
                    messageEntity = null;
                    messageLocalEntity = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
                fileEntity = null;
                fileLocalEntity = null;
                messageEntity = null;
                messageLocalEntity = null;
            }
        } else if (uploading.getDocType() == DocTypes.Audio) {
            Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper
                    .notifyAudioUploading(false, uploading.getPath()
                            , new File(uploading.getPath()).getName(), 60000);
            fileEntity = filePair.first;
            fileLocalEntity = filePair.second;
            currentUploadingFile = fileEntity;
            Core.getInstance().bus().post(new FileUploading(DocTypes.Audio, fileEntity, fileLocalEntity));
            Core.getInstance().bus().post(new FileUploading(DocTypes.Audio, fileEntity, fileLocalEntity));
            if (uploading.isAttachToMessage()) {
                Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper
                        .notifyAudioMessageSending(uploading.getRoomId(), fileEntity.getFileId());
                messageEntity = msgPair.first;
                messageLocalEntity = msgPair.second;
                Core.getInstance().bus().post(new MessageSending(messageEntity, messageLocalEntity));
            } else {
                messageEntity = null;
                messageLocalEntity = null;
            }
        } else if (uploading.getDocType() == DocTypes.Video) {
            Pair<Entities.File, Entities.FileLocal> filePair = DatabaseHelper
                    .notifyVideoUploading(false, uploading.getPath()
                            , new File(uploading.getPath()).getName(), 60000);
            fileEntity = filePair.first;
            fileLocalEntity = filePair.second;
            currentUploadingFile = fileEntity;
            Core.getInstance().bus().post(new FileUploading(DocTypes.Video, fileEntity, fileLocalEntity));
            Core.getInstance().bus().post(new FileUploading(DocTypes.Video, fileEntity, fileLocalEntity));
            if (uploading.isAttachToMessage()) {
                Pair<Entities.Message, Entities.MessageLocal> msgPair = DatabaseHelper
                        .notifyVideoMessageSending(uploading.getRoomId(), fileEntity.getFileId());
                messageEntity = msgPair.first;
                messageLocalEntity = msgPair.second;
                Core.getInstance().bus().post(new MessageSending(messageEntity, messageLocalEntity));
            } else {
                messageEntity = null;
                messageLocalEntity = null;
            }
        } else {
            fileEntity = null;
            fileLocalEntity = null;
            messageEntity = null;
            messageLocalEntity = null;
        }
        uploadingFiles.offer(new Tuple<>(fileEntity, fileLocalEntity, messageEntity, messageLocalEntity, uploading));
        return fileEntity != null ? fileEntity.getFileId() : 0;
    }

    public static void downloadFile(Downloading downloading) {
        DatabaseHelper.notifyFileDownloading(downloading.getFile().getFileId());
        downloadingFiles.offer(downloading);
    }

    public static void cancelUpload(long fileId) {
        synchronized (uploadingFiles) {
            if (currentUploadingFile != null && currentUploadingFile.getFileId() == fileId) {
                currentUploadingCall.cancel();
            } else {
                for (Tuple<Entities.File, Entities.FileLocal, Entities.Message, Entities.MessageLocal, Uploading> tuple : uploadingFiles) {
                    if (tuple.first.getFileId() == fileId) {
                        uploadingFiles.remove(tuple);
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
                    int progress = (int)(fileSizeDownloaded * 100 / fileSize);
                    DatabaseHelper.notifyFileTransferProgressed(downloading.getFile().getFileId(), progress);
                    Core.getInstance().bus().post(new FileTransferProgressed(downloading.getFile()
                            instanceof Entities.Photo ? DocTypes.Photo : downloading.getFile()
                            instanceof Entities.Audio ? DocTypes.Audio : DocTypes.Video,
                            downloading.getFile().getFileId(), progress));
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