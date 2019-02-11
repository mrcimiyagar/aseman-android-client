package kasper.android.pulse.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import kasper.android.pulse.R;
import kasper.android.pulse.activities.CreateRoomActivity;
import kasper.android.pulse.extras.LinearDecoration;
import kasper.android.pulse.helpers.GraphicHelper;

public class ComplexFragment extends Fragment {

    private long complexId;

    public static ComplexFragment instantiate(long complexId) {
        ComplexFragment fragment = new ComplexFragment();
        Bundle bundle = new Bundle();
        bundle.putLong("complex_id", complexId);
        fragment.setArguments(bundle);
        return fragment;
    }

    private FloatingActionButton addRoomFAB;
    private RecyclerView roomsRV;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.complexId = getArguments().getLong("complex_id");
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View cView = inflater.inflate(R.layout.fragment_room, container, false);

        initViews(cView);
        initListeners();

        return cView;
    }

    private void initViews(View cView) {
        addRoomFAB = cView.findViewById(R.id.addRootFAB);
        roomsRV = cView.findViewById(R.id.roomsRV);
    }

    private void initListeners() {
        roomsRV.setLayoutManager(new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false));
        roomsRV.addItemDecoration(new LinearDecoration(GraphicHelper.dpToPx(16), GraphicHelper.dpToPx(16)));
        /*roomsRV.setAdapter(new RoomsAdapter((AppCompatActivity) getActivity(), DatabaseHelper.getRooms(complexId), new Runnable() {
            @Override
            public void run() {

            }
        }));*/
        addRoomFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CreateRoomActivity.class).putExtra("complex_id", complexId));
            }
        });
    }
}
