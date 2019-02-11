package kasper.android.pulse.models.extras;

import java.io.Serializable;

public class Doc implements Serializable {

    private String title;
    public String getTitle() { return this.title; }

    private String path;
    public String getPath() { return this.path; }

    private DocTypes docType;
    public DocTypes getDocType() { return this.docType; }

    private long tag;
    public long getTag() { return this.tag; }

    public Doc(String path, long tag, DocTypes docType) {

        if (path.length() > 0) {
            if (path.charAt(path.length() - 1) != '/') {
                this.title = path.substring(path.lastIndexOf("/") + 1);
            } else {
                this.title = "untitled";
            }
        } else {
            this.title = "untitled";
        }
        this.path = path;
        this.tag = tag;
        this.docType = docType;
    }

    @Override
    public boolean equals(Object obj) {

        return obj instanceof Doc && this.path.equals(((Doc)obj).getPath());
    }
}
