package kasper.android.pulse.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import kasper.android.pulse.R;

/**
 * Created by keyhan1376 on 1/27/2018.
 */

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ImageListVH> {

    private String[] paths;

    public ImageListAdapter(String[] paths) {
        this.paths = paths;
        this.notifyDataSetChanged();
    }

    @Override
    public ImageListVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ImageListVH(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_image_list, parent, false));
    }

    @Override
    public void onBindViewHolder(ImageListVH holder, int position) {
        Picasso.with(holder.itemView.getContext()).load(paths[position]).into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return paths.length;
    }

    class ImageListVH extends RecyclerView.ViewHolder {

        ImageView imageView;

        ImageListVH(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.adapter_image_list_image_view);
        }
    }
}
