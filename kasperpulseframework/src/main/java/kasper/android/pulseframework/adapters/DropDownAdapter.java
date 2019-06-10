package kasper.android.pulseframework.adapters;

import android.database.DataSetObserver;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;

import java.util.Hashtable;
import java.util.List;

import kasper.android.pulseframework.components.CustomHashtable;
import kasper.android.pulseframework.engines.UiInitiatorEngine;
import kasper.android.pulseframework.models.Controls;
import kasper.android.pulseframework.models.Tuple;

public class DropDownAdapter implements SpinnerAdapter {

    private List<Controls.Control> items;
    private Hashtable<String, Pair<Controls.Control, View>> idTable;
    private UiInitiatorEngine uiInitiatorEngine;

    private DataSetObserver dataSetObserver;

    public DropDownAdapter(List<Controls.Control> items,
                           Hashtable<String, Pair<Controls.Control, View>> idTable,
                           UiInitiatorEngine uiInitiatorEngine) {
        this.items = items;
        this.idTable = idTable;
        this.uiInitiatorEngine = uiInitiatorEngine;
    }

    public void addItem(Controls.Control control) {
        this.items.add(control);
        if (this.dataSetObserver != null)
            this.dataSetObserver.onChanged();
    }

    @Override
    public View getDropDownView(int i, View view, ViewGroup viewGroup) {
        Tuple<View, List<Pair<Controls.Control, View>>,
                CustomHashtable<String, Pair<Controls.Control, View>>> pair = uiInitiatorEngine.buildViewTree(
                Controls.PanelCtrl.LayoutType.LINEAR_VERTICAL,
                items.get(i));
        View itemView = pair.getItem1();
        uiInitiatorEngine.initFingerprint(pair.getItem2(), i);
        return itemView;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        this.dataSetObserver = dataSetObserver;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        this.dataSetObserver = null;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int i) {
        return items.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Tuple<View, List<Pair<Controls.Control, View>>,
                CustomHashtable<String, Pair<Controls.Control, View>>> pair = uiInitiatorEngine.buildViewTree(
                Controls.PanelCtrl.LayoutType.LINEAR_VERTICAL,
                items.get(i));
        View itemView = pair.getItem1();
        uiInitiatorEngine.initFingerprint(pair.getItem2(), i);
        idTable.putAll(pair.getItem3());
        return itemView;
    }

    @Override
    public int getItemViewType(int i) {
        return 1;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return items.size() == 0;
    }
}
