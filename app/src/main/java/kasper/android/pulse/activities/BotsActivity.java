package kasper.android.pulse.activities;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import kasper.android.pulse.R;
import kasper.android.pulse.adapters.BotsSimpleAdapter;
import kasper.android.pulse.core.Core;
import kasper.android.pulse.extras.LinearDecoration;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.helpers.GraphicHelper;
import kasper.android.pulse.models.entities.Entities;

public class BotsActivity extends BaseActivity {

    RecyclerView recyclerView;
    TextView allBtn, createdBtn;

    boolean all = true;
    int scrollPos = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bots);

        recyclerView = findViewById(R.id.botsRV);
        allBtn = findViewById(R.id.botsAllBtn);
        createdBtn = findViewById(R.id.botsCreatedBtn);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.addItemDecoration(new LinearDecoration(GraphicHelper.dpToPx(80), 0));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                scrollPos = recyclerView.computeVerticalScrollOffset();
            }
        });

        allBtn.setOnClickListener(view -> {
             all = true;
             select1();
        });
        createdBtn.setOnClickListener(view -> {
            all = false;
            select2();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (all) {
            select1();
        } else {
            select2();
        }
        recyclerView.post(() -> recyclerView.scrollBy(0, scrollPos));
    }

    private void select1() {
        allBtn.setBackgroundColor(Core.getInstance().getResources().getColor(R.color.colorBlue));
        createdBtn.setBackgroundColor(Core.getInstance().getResources().getColor(R.color.colorBlackBlue3));
        List<Entities.Bot> allBots = DatabaseHelper.getSubscribedBots();
        recyclerView.setAdapter(new BotsSimpleAdapter(this, allBots));
    }

    private void select2() {
        allBtn.setBackgroundColor(Core.getInstance().getResources().getColor(R.color.colorBlackBlue3));
        createdBtn.setBackgroundColor(Core.getInstance().getResources().getColor(R.color.colorBlue));
        List<Entities.Bot> allBots = DatabaseHelper.getCreatedBots();
        recyclerView.setAdapter(new BotsSimpleAdapter(this, allBots));
    }

    public void onBackBtnClicked(View view) {
        onBackPressed();
    }

    public void onBotAddBtnClicked(View view) {
        startActivity(new Intent(this, CreateBotActivity.class));
    }
}
