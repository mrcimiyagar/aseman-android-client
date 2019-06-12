package kasper.android.pulse.adapters;

import android.content.ContentUris;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.callbacks.ui.OnFileSelectListener;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.models.extras.Doc;
import kasper.android.pulse.models.extras.DocTypes;
import kasper.android.pulse.models.extras.GlideApp;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.Holder> implements SectionTitleProvider {

    private final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");

    private List<Doc> docs;
    private int blockSize;
    private OnFileSelectListener selectCallback;

    public FilesAdapter(List<Doc> docs, int blockSize, OnFileSelectListener selectCallback) {
        this.docs = new ArrayList<>(docs);
        this.blockSize = blockSize;
        this.selectCallback = selectCallback;
        //this.notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_doc, parent, false);
        itemView.setLayoutParams(new RecyclerView.LayoutParams(blockSize, blockSize));
        return new Holder(itemView);
    }

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        holder.cleanup();
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position) {

        final Doc doc = docs.get(position);

        if (doc.getDocType() == DocTypes.Photo) {
            GlideApp.with(Core.getInstance()).load(new File(doc.getPath())).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    imageNotLoaded(holder, DocTypes.Photo);
                    return true;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    imageLoaded(holder, resource);
                    return true;
                }
            }).into(holder.iconIV);
        }
        else if (doc.getDocType() == DocTypes.Audio) {
            GlideApp.with(Core.getInstance()).load(getCoverPath(doc.getTag())).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    imageNotLoaded(holder, DocTypes.Audio);
                    return true;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    imageLoaded(holder, resource);
                    return true;
                }
            }).into(holder.iconIV);
        }
        else if (doc.getDocType() == DocTypes.Video) {
            GlideApp.with(Core.getInstance()).load(doc.getPath()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    imageNotLoaded(holder, DocTypes.Audio);
                    return true;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    imageLoaded(holder, resource);
                    return true;
                }
            }).into(holder.iconIV);
        }

        holder.titleTV.setText(doc.getTitle());

        holder.itemView.setOnClickListener(v -> {
            if (selectCallback != null) {
                selectCallback.fileSelected(doc.getPath(), doc.getDocType());
            }
        });
    }

    @Override
    public String getSectionTitle(int position) {
        Doc doc = docs.get(position);
        String title = "";
        int index = doc.getPath().lastIndexOf("/");
        if (index > 0 && index < doc.getPath().length() - 1) {
            title = doc.getPath().substring(index + 1);
        }
        return title.length() > 0 ? title.substring(0, 1).toUpperCase() : "";
    }

    @Override
    public int getItemCount() {
        return this.docs.size();
    }

    private Uri getCoverPath(long albumId) {
        return ContentUris.withAppendedId(sArtworkUri, albumId);
    }

    private static void imageLoaded(Holder holder, Drawable drawable) {
        holder.iconIV.setVisibility(View.VISIBLE);
        holder.iconSignIV.setVisibility(View.GONE);
        holder.iconIV.setImageDrawable(drawable);
    }

    private static void imageNotLoaded(Holder holder, DocTypes docType) {
        holder.iconIV.setVisibility(View.GONE);
        holder.iconSignIV.setVisibility(View.VISIBLE);
        holder.iconSignIV.setImageResource(docType == DocTypes.Photo ? R.drawable.ic_photo : (docType
                == DocTypes.Audio ? R.drawable.ic_audio : R.drawable.audio_dark));
    }

    class Holder extends RecyclerView.ViewHolder {

        RelativeLayout mainLayout;
        TextView titleTV;
        ImageView iconIV;
        ImageView iconSignIV;

        Holder(View itemView) {

            super(itemView);
            mainLayout = itemView.findViewById(R.id.adapter_doc_main_layout);
            iconIV = itemView.findViewById(R.id.adapter_doc_icon_image_view);
            iconSignIV = itemView.findViewById(R.id.adapter_doc_icon_sign_image_view);
            titleTV = itemView.findViewById(R.id.adapter_doc_title_text_view);
        }

        void cleanup() {
            GlideApp.with(Core.getInstance()).clear(this.iconIV);
        }
    }
}