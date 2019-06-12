package kasper.android.pulse.adapters;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.icu.util.MeasureUnit;
import android.text.format.DateFormat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import kasper.android.pulse.R;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.GlideApp;

public class PostSectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private AppCompatActivity activity;
    private Entities.Post post;

    public PostSectionAdapter(AppCompatActivity activity, Entities.Post post) {
        this.activity = activity;
        this.post = post;
        this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0) {
            FrameLayout container = new FrameLayout(activity);
            RecyclerView.LayoutParams cParams = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    GraphicHelper.dpToPx(250));
            container.setLayoutParams(cParams);

            ImageView imageView = new ImageView(activity);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(
                    0,
                    GraphicHelper.dpToPx(8),
                    0,
                    GraphicHelper.dpToPx(8));
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            container.addView(imageView, 0);

            ImageButton backBTN = new ImageButton(activity);
            backBTN.setLayoutParams(new FrameLayout.LayoutParams(GraphicHelper.dpToPx(56), GraphicHelper.dpToPx(56)));
            backBTN.setBackgroundColor(Color.TRANSPARENT);
            backBTN.setImageResource(R.drawable.ic_back);
            backBTN.setColorFilter(Color.WHITE);
            //backBTN.setPadding(GraphicHelper.dpToPx(12), GraphicHelper.dpToPx(12), GraphicHelper.dpToPx(12), GraphicHelper.dpToPx(12));
            //backBTN.setScaleType(ImageView.ScaleType.FIT_XY);
            container.addView(backBTN, 1);

            return new SecItem(container);
        } else if (viewType == 1) {
            TextView textView = new TextView(activity);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT);
            params.setMargins(
                    GraphicHelper.dpToPx(16),
                    GraphicHelper.dpToPx(8),
                    GraphicHelper.dpToPx(16),
                    GraphicHelper.dpToPx(8));
            textView.setLayoutParams(params);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            return new SecItem(textView);
        } else if (viewType == 2) {
            TextView textView = new TextView(activity);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    RecyclerView.LayoutParams.WRAP_CONTENT);
            params.setMargins(
                    GraphicHelper.dpToPx(16),
                    GraphicHelper.dpToPx(8),
                    GraphicHelper.dpToPx(16),
                    GraphicHelper.dpToPx(8));
            textView.setLayoutParams(params);
            textView.setTextColor(Color.WHITE);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            return new SecItem(textView);
        } else if (viewType == 3) {
            ImageView imageView = new ImageView(activity);
            RecyclerView.LayoutParams params = new RecyclerView.LayoutParams(
                    RecyclerView.LayoutParams.MATCH_PARENT,
                    GraphicHelper.dpToPx(250));
            params.setMargins(
                    0,
                    GraphicHelper.dpToPx(8),
                    0,
                    GraphicHelper.dpToPx(8));
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return new SecItem(imageView);
        } else {
            return null;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return 0;
        } else if (position == 1) {
            return 1;
        } else {
            Entities.PostSection section = this.post.getSections().get(position - 2);
            if (section instanceof Entities.PostTextSection) {
                return 2;
            } else if (section instanceof Entities.PostImageSection) {
                return 3;
            } else {
                return 4;
            }
        }
    }

    private static String getDate(long milliSeconds)
    {
        return DateFormat.format("dd/MM/yyyy hh:mm:ss", milliSeconds).toString();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        int viewType = getItemViewType(position);

        if (viewType == 0) {
            GlideApp.with(activity).load(this.post.getImageUrl()).fitCenter().into(((ImageView) ((ViewGroup) holder.itemView).getChildAt(0)));
            ((ImageButton) ((ViewGroup) holder.itemView).getChildAt(1)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.onBackPressed();
                }
            });
        } else if (viewType == 1) {
            ((TextView) holder.itemView).setText(this.post.getTitle());
        } else if (viewType == 2) {
            Entities.PostTextSection textSection = (Entities.PostTextSection) this.post.getSections().get(position - 2);
            ((TextView) holder.itemView).setText(textSection.getText());
        } else if (viewType == 3) {
            Entities.PostImageSection imageSection = (Entities.PostImageSection) this.post.getSections().get(position - 2);
            GlideApp.with(activity).load(imageSection.getImageUrl()).fitCenter().into(((ImageView) holder.itemView));
        }
    }

    @Override
    public int getItemCount() {
        return this.post.getSections().size() + 2;
    }

    class SecItem extends RecyclerView.ViewHolder {

        SecItem(View itemView) {
            super(itemView);
        }
    }
}
