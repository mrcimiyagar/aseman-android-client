package kasper.android.pulse.activities;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ComplexesSearchAdapter;
import kasper.android.pulse.adapters.HumansAdapter;
import kasper.android.pulse.callbacks.network.ServerCallback;
import kasper.android.pulse.extras.LinearDecoration;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.helpers.NetworkHelper;
import kasper.android.pulse.models.entities.Entities;
import kasper.android.pulse.models.network.Packet;
import kasper.android.pulse.retrofit.SearchHandler;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private EditText searchET;
    private RecyclerView itemsRV;

    private FrameLayout usersTab;
    private FrameLayout complexesTab;

    private Timer timer;

    private List<Entities.User> users = new ArrayList<>();
    private List<Entities.Complex> complexes = new ArrayList<>();

    private boolean userMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        this.searchET = findViewById(R.id.activity_search_edit_text);
        this.itemsRV = findViewById(R.id.activity_search_recycler_view);
        this.usersTab = findViewById(R.id.tab1Frame);
        this.complexesTab = findViewById(R.id.tab2Frame);

        this.itemsRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        this.itemsRV.addItemDecoration(new LinearDecoration(GraphicHelper.dpToPx(129), GraphicHelper.dpToPx(8)));

        usersTab.setBackgroundColor(getResources().getColor(R.color.colorBlue));
        complexesTab.setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3));

        usersTab.setOnClickListener(view -> {
            usersTab.setBackgroundColor(getResources().getColor(R.color.colorBlue));
            complexesTab.setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3));
            userMode = true;
            itemsRV.setAdapter(new HumansAdapter(SearchActivity.this, users));
        });

        complexesTab.setOnClickListener(view -> {
            usersTab.setBackgroundColor(getResources().getColor(R.color.colorBlackBlue3));
            complexesTab.setBackgroundColor(getResources().getColor(R.color.colorBlue));
            userMode = false;
            itemsRV.setAdapter(new ComplexesSearchAdapter(SearchActivity.this, complexes));
        });

        this.searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (timer != null) {
                    timer.cancel();
                    timer.purge();
                }
                scheduleInlineQuery();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void scheduleInlineQuery() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                String searchStr = searchET.getText().toString();
                if (searchStr.length() == 0) {
                    return;
                }
                Packet usersPacket = new Packet();
                usersPacket.setSearchQuery(searchStr);
                SearchHandler searchHandler = NetworkHelper.getRetrofit().create(SearchHandler.class);
                Call<Packet> usersCall = searchHandler.searchUsers(usersPacket);
                NetworkHelper.requestServer(usersCall, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        users = packet.getUsers();
                        if (userMode) {
                            itemsRV.setAdapter(new HumansAdapter(SearchActivity.this, users));
                        }
                    }

                    @Override
                    public void onServerFailure() {
                        Toast.makeText(SearchActivity.this, "User search failure", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionFailure() {
                        Toast.makeText(SearchActivity.this, "User search failure", Toast.LENGTH_SHORT).show();
                    }
                });
                Packet complexesPacket = new Packet();
                complexesPacket.setSearchQuery(searchStr);
                Call<Packet> complexesCall = searchHandler.searchComplexes(complexesPacket);
                NetworkHelper.requestServer(complexesCall, new ServerCallback() {
                    @Override
                    public void onRequestSuccess(Packet packet) {
                        complexes = packet.getComplexes();
                        if (!userMode) {
                            itemsRV.setAdapter(new ComplexesSearchAdapter(SearchActivity.this, complexes));
                        }
                    }

                    @Override
                    public void onServerFailure() {
                        Toast.makeText(SearchActivity.this, "Complex search failure", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onConnectionFailure() {
                        Toast.makeText(SearchActivity.this, "Complex search failure", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, 1000);
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
