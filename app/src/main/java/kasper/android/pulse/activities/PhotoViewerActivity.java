package kasper.android.pulse.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;

import java.io.File;

import kasper.android.pulse.R;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;

public class PhotoViewerActivity extends AppCompatActivity {

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_viewer);

        long fileId = 0;

        if (getIntent().getExtras() != null) {
            fileId = getIntent().getExtras().getLong("fileId");
        }

        final ImageView photoIV = findViewById(R.id.activity_photo_viewer_image_view);
        photoIV.setOnTouchListener(new ImageMatrixTouchHandler(this));
        Entities.FileLocal fileLocal = DatabaseHelper.getFileLocalByFileId(fileId);
        if (fileLocal != null) {
            if (fileLocal.getPath() != null && fileLocal.getPath().length() > 0) {
                GlideApp.with(this).load(fileLocal.getPath()).into(photoIV);
            } else {
                File file = new File(DatabaseHelper.getFilePath(fileId));
                if (!file.exists()) {
                    GlideApp.with(this).load(NetworkHelper.createFileLink(fileId)).into(photoIV);
                } else {
                    GlideApp.with(this).load(file.getPath()).into(photoIV);
                }
            }
        } else {
            File file = new File(DatabaseHelper.getFilePath(fileId));
            if (!file.exists()) {
                GlideApp.with(this).load(NetworkHelper.createFileLink(fileId)).into(photoIV);
            } else {
                GlideApp.with(this).load(file.getPath()).into(photoIV);
            }
        }
    }

    public void onCloseBtnClicked(View view) {
        this.onBackPressed();
    }
}
