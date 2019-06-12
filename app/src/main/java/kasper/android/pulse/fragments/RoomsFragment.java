package kasper.android.pulse.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.HomeAdapter;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.LogHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.extras.RoomTypes;

/**
 * A simple {@link Fragment} subclass.
 */
public class RoomsFragment extends BaseFragment {

    private RecyclerView roomsRV;
    private long complexId;
    private RoomTypes roomType;

    public RoomsFragment() {

    }

    public static RoomsFragment instantiate(long complexId, RoomTypes roomType) {
        RoomsFragment fileFragment = new RoomsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("complex-id", complexId);
        bundle.putString("room-type", roomType.name());
        fileFragment.roomType = roomType;
        fileFragment.setArguments(bundle);
        return fileFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            if (getArguments().containsKey("room-type"))
                roomType = RoomTypes.valueOf(getArguments().getString("room-type"));
            if (getArguments().containsKey("complex-id"))
                complexId = getArguments().getLong("complex-id");

        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_rooms, container, false);
        roomsRV = contentView.findViewById(R.id.fragment_doc_recycler_view);
        roomsRV.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        List<Entities.BaseRoom> rooms = new ArrayList<>();
        if (roomType == RoomTypes.Private) {
            rooms = DatabaseHelper.getHomeRooms();
        } else if (roomType == RoomTypes.Contact) {
            rooms = DatabaseHelper.getContactRooms();
        } else if (roomType == RoomTypes.Single) {
            rooms = DatabaseHelper.getComplexSingleRooms(complexId);
        } else if (roomType == RoomTypes.Group) {
            rooms = DatabaseHelper.getComplexNonSingleRooms(complexId);
        }
        roomsRV.setAdapter(new HomeAdapter((AppCompatActivity) getActivity(), rooms));
        return contentView;
    }

    public void dispose() {
        if (roomsRV != null && roomsRV.getAdapter() != null)
            ((HomeAdapter) roomsRV.getAdapter()).dispose();
    }
}
