package kasper.android.pulse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends BaseFragment {

    private int currentComplex = 0;
    private List<Entities.Complex> complexes;
    private List<Entities.BaseRoom> currComplexRooms;

    public ExploreFragment() {

    }

    public static ExploreFragment instantiate() {
        return new ExploreFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_complexes, container, false);

        complexes = DatabaseHelper.getComplexes();
        currComplexRooms = complexes.get(currentComplex).getAllRooms();

        return contentView;
    }

    public void dispose() {

    }
}
