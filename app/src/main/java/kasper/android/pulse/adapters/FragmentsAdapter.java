package kasper.android.pulse.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import kasper.android.pulse.fragments.BaseFragment;;

import java.util.List;

public class FragmentsAdapter extends FragmentStatePagerAdapter {

    private List<BaseFragment> fragments;

    public FragmentsAdapter(FragmentManager fm, List<BaseFragment> fragments) {
        super(fm);
        this.fragments = fragments;
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