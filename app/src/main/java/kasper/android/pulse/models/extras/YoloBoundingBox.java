package kasper.android.pulse.models.extras;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class YoloBoundingBox {

    @PrimaryKey(autoGenerate = true)
    private long yoloBoundingBoxId;
    private long imageId;
    private String label;
    private float x;
    private float y;
    private float width;
    private float height;
    private float confidence;

    public long getYoloBoundingBoxId() {
        return yoloBoundingBoxId;
    }

    public void setYoloBoundingBoxId(long yoloBoundingBoxId) {
        this.yoloBoundingBoxId = yoloBoundingBoxId;
    }

    public long getImageId() {
        return imageId;
    }

    public void setImageId(long imageId) {
        this.imageId = imageId;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }
}
