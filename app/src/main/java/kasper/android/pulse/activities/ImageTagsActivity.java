package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ImageTagsAdapter;
import kasper.android.pulse.callbacks.ui.OnImageTagSelected;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.extras.YoloBoundingBox;

import static kasper.android.pulse.helpers.GraphicHelper.dpToPx;
import static kasper.android.pulse.helpers.GraphicHelper.getScreenHeight;
import static kasper.android.pulse.helpers.GraphicHelper.getScreenWidth;

public class ImageTagsActivity extends AppCompatActivity {

    private ImageView image;
    private RecyclerView tagsList;

    private long imageId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_tags);

        image = findViewById(R.id.image);
        tagsList = findViewById(R.id.tagsRV);

        imageId = getIntent().getExtras().getLong("image-id");

        String filePath = DatabaseHelper.getFilePath(imageId);
        if (!new File(filePath).exists())
            filePath = DatabaseHelper.getFileLocalByFileId(imageId).getPath();

        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
        image.setImageBitmap(mutableBitmap);

        Entities.Photo photo = (Entities.Photo) DatabaseHelper.getFileById(imageId);
        if (photo.getWidth() > getScreenWidth()- dpToPx(144)) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) image.getLayoutParams();
            int width = Math.max((getScreenWidth() - dpToPx(144)), dpToPx(200));
            params.width = width;
            params.height = photo.getHeight() * width / photo.getWidth();
            image.setLayoutParams(params);
        } else if (photo.getHeight() > getScreenHeight() - dpToPx(144)) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) image.getLayoutParams();
            int height = getScreenHeight() - dpToPx(144);
            params.height = height;
            params.width = Math.max(photo.getWidth() * height / photo.getHeight(), dpToPx(200));
            image.setLayoutParams(params);
        } else {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) image.getLayoutParams();
            params.height = Math.max(photo.getHeight(), dpToPx(200));
            params.width = Math.max(photo.getWidth(), dpToPx(200));
            image.setLayoutParams(params);
        }

        List<YoloBoundingBox> boxes = DatabaseHelper.getImageYoloBoundingBoxes(imageId);
        tagsList.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        tagsList.setAdapter(new ImageTagsAdapter(this, boxes, new OnImageTagSelected() {
            @Override
            public void imageTagSelected(List<YoloBoundingBox> boxes) {
                highlightBoxes(boxes);
            }
        }));
    }

    public void highlightBoxes(List<YoloBoundingBox> boxes) {
        String filePath = DatabaseHelper.getFilePath(imageId);
        if (!new File(filePath).exists())
            filePath = DatabaseHelper.getFileLocalByFileId(imageId).getPath();
        Bitmap bmp = BitmapFactory.decodeFile(filePath);
        Bitmap mutableBitmap = bmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paint = new Paint();
        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.STROKE);
        for (YoloBoundingBox box : boxes) {
            canvas.drawRect(box.getX(), box.getY(), (box.getX() + box.getWidth()), (box.getY() + box.getHeight()), paint);
        }
        image.setImageBitmap(mutableBitmap);
    }
}
