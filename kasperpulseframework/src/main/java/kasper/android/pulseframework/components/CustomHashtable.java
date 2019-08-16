package kasper.android.pulseframework.components;

import android.util.Log;

import java.util.Hashtable;

public class CustomHashtable<K, V> extends Hashtable<K, V> {

    @Override
    public synchronized V put(K key, V value) {
        return super.put(key, value);
    }
}
