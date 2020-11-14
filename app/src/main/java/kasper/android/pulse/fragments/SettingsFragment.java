package kasper.android.pulse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import kasper.android.pulse.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends BaseFragment {

    public SettingsFragment() {

    }

    public static SettingsFragment instantiate() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_desktop, container, false);


        return contentView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void dispose() {

    }
}
