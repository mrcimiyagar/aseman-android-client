package kasper.android.pulse.adapters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import kasper.android.pulse.fragments.BaseFragment;
import kasper.android.pulse.fragments.RoomsFragment;;

import java.util.List;

public class FragmentsAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> fragments;

    public FragmentsAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.fragments = fragments;
    }

    public void dispose() {
        for (BaseFragment fragment : fragments) {
            if (fragment instanceof RoomsFragment) {
                ((RoomsFragment) fragment).dispose();
            }
        }
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return this.fragments.get(position);
    }

    @Override
    public int getCount() {
        return this.fragments.size();
    }
}