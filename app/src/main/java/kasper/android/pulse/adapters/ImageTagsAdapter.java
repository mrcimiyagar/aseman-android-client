package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.ui.OnImageTagSelected;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;
import kasper.android.pulse.models.extras.YoloBoundingBox;

public class ImageTagsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private List<String> tags;
    private Hashtable<String, List<YoloBoundingBox>> boxes;
    private OnImageTagSelected tagSelection;

    public ImageTagsAdapter(AppCompatActivity activity, List<YoloBoundingBox> boxes, OnImageTagSelected tagSelection) {
        this.activity = activity;
        this.tagSelection = tagSelection;
        this.tags = new ArrayList<>();
        this.boxes = new Hashtable<>();
        for (YoloBoundingBox box : boxes) {
            if (this.tags.contains(box.getLabel())) {
                this.boxes.get(box.getLabel()).add(box);
            } else {
                List<YoloBoundingBox> section = new ArrayList<>();
                section.add(box);
                this.boxes.put(box.getLabel(), section);
                this.tags.add(box.getLabel());
            }
        }
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        TextView textView = new TextView(parent.getContext());
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        textView.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
        RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, GraphicHelper.dpToPx(56));
        params.setMargins(GraphicHelper.dpToPx(16), 0, GraphicHelper.dpToPx(16), 0);
        textView.setLayoutParams(params);
        return new TagItem(textView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ((TextView) holder.itemView).setText("#" + tags.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<YoloBoundingBox> result = boxes.get(tags.get(position));
                tagSelection.imageTagSelected(result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return this.tags.size();
    }

    class TagItem extends RecyclerView.ViewHolder {

        TagItem(View itemView) {
            super(itemView);
        }
    }
}
