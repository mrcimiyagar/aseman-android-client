package kasper.android.pulse.tasks;

import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kasper.android.pulse.callbacks.ui.OnDocsLoadedListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.models.extras.Doc;
import kasper.android.pulse.models.extras.DocTypes;

/**
 * Created by keyhan1376 on 12/15/2017.
 */

public class DocsLoadTask extends AsyncTask<Void, Void, List<Doc>> {

    private String docType;
    private OnDocsLoadedListener callback;

    public DocsLoadTask(String docType, OnDocsLoadedListener callback) {
        this.docType = docType;
        this.callback = callback;
    }

    @Override
    protected List<Doc> doInBackground(Void... voids) {
        if (docType != null) {
            final List<Doc> docs = new ArrayList<>();
            switch (docType) {
                case "PHOTO":
                    try {
                        String PathOfImage;
                        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                        String[] projection = {MediaStore.MediaColumns.DATA};
                        Cursor cursor = Core.getInstance().getContentResolver().query(uri, projection, null, null, null);
                        assert cursor != null;
                        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        while (cursor.moveToNext()) {
                            PathOfImage = cursor.getString(column_index_data);
                            if (new File(PathOfImage).exists()) {
                                Doc doc = new Doc(PathOfImage, -1, DocTypes.Photo);
                                docs.add(doc);
                            }
                        }
                        cursor.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "AUDIO":
                    try {
                        String PathOfMusic;
                        long AlbumId;
                        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        String[] projection = {MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.DATA};
                        Cursor cursor = Core.getInstance().getContentResolver().query(uri, projection, null, null, null);
                        assert cursor != null;
                        int column_index_id = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                        while (cursor.moveToNext()) {
                            AlbumId = cursor.getLong(column_index_id);
                            PathOfMusic = cursor.getString(column_index_data);
                            if (new File(PathOfMusic).exists()) {
                                Doc doc = new Doc(PathOfMusic, AlbumId, DocTypes.Audio);
                                docs.add(doc);
                            }
                        }
                        cursor.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                case "VIDEO":
                    try {
                        String PathOfVideo;
                        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        String[] projection = {MediaStore.Video.Media.DATA};
                        Cursor cursor = Core.getInstance().getContentResolver().query(uri, projection, null, null, null);
                        assert cursor != null;
                        int column_index_data = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                        while (cursor.moveToNext()) {
                            PathOfVideo = cursor.getString(column_index_data);
                            if (new File(PathOfVideo).exists()) {
                                Doc doc = new Doc(PathOfVideo, -1, DocTypes.Video);
                                docs.add(doc);
                            }
                        }
                        cursor.close();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
            }
            Collections.reverse(docs);
            return docs;
        }
        return null;
    }

    @Override
    protected void onPostExecute(List<Doc> docs) {
        super.onPostExecute(docs);
        callback.docsLoaded(docs);
    }
}
