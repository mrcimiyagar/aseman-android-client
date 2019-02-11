package kasper.android.pulseframework.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import kasper.android.pulseframework.engines.UiInitiatorEngine;
import kasper.android.pulseframework.locks.Locks;
import kasper.android.pulseframework.models.Controls;
import kasper.android.pulseframework.models.Tuple;

public class RecyclerAdapter extends RecyclerView.Adapter {

    private Context context;
    private UiInitiatorEngine uiInitiatorEngine;
    private Hashtable<String, Pair<Controls.Control, View>> idTable;
    private List<Controls.Control> items;
    private RecyclerView.RecycledViewPool viewPool;

    public RecyclerAdapter(Context context,
                           UiInitiatorEngine uiInitiatorEngine,
                           Hashtable<String, Pair<Controls.Control, View>> idTable,
                           List<Controls.Control> items) {
        this.context = context;
        this.uiInitiatorEngine = uiInitiatorEngine;
        this.idTable = idTable;
        this.items = items;
        this.viewPool = new RecyclerView.RecycledViewPool();
    }

    public void addItem(int position, Controls.Control control) {
        this.items.add(position, control);
        this.notifyItemInserted(position);
    }

    public void removeItem(int position) {
        this.items.remove(position);
        this.notifyItemRemoved(position);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        RelativeLayout itemContainer = new RelativeLayout(context);
        itemContainer.setLayoutParams(
                new RecyclerView.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        return new ItemHolder(itemContainer);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int i) {
        Tuple<View, List<Pair<Controls.Control, View>>,
                Hashtable<String, Pair<Controls.Control, View>>> pair =
                uiInitiatorEngine.buildViewTree(
                        Controls.PanelCtrl.LayoutType.RELATIVE,
                        items.get(i));
        View itemView = pair.getItem1();
        for (Pair<Controls.Control, View> entry : pair.getItem2())
            if (entry.second instanceof RecyclerView)
                ((RecyclerView) entry.second).setRecycledViewPool(viewPool);
        uiInitiatorEngine.initFingerprint(pair.getItem2(), i);
        Locks.runSafeOnIdTable(() -> {
            for (Map.Entry<String, Pair<Controls.Control, View>> entry : pair.getItem3().entrySet()) {
                idTable.remove(entry.getKey());
                idTable.put(entry.getKey(), entry.getValue());
            }
            ((ItemHolder) holder).containerIds = new HashSet<>(idTable.keySet());
        });
        ((ViewGroup)((ItemHolder) holder).itemView).addView(itemView);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        Locks.runSafeOnIdTable(() -> {
            for (String id : ((ItemHolder) holder).containerIds) {
                Pair<Controls.Control, View> value = idTable.remove(id);
                value = new Pair<>(value.first, null);
                idTable.put(id, value);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {
        private HashSet<String> containerIds;

        ItemHolder(View itemView) {
            super(itemView);
        }
    }
}
