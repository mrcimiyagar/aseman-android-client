package kasper.android.pulse.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.MembersAdapter;
import kasper.android.pulse.helpers.DatabaseHelper;
import kasper.android.pulse.models.entities.Entities;

import android.os.Bundle;
import android.view.View;

import java.util.List;

public class ComplexMembersActivity extends AppCompatActivity {

    private long complexId;
    private RecyclerView membersRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_members);

        if (getIntent().getExtras() != null)
            complexId = getIntent().getExtras().getLong("complex_id");

        initView();
        initDecorations();
        initData();
    }

    @Override
    protected void onDestroy() {
        if (membersRV.getAdapter() != null)
            ((MembersAdapter) membersRV.getAdapter()).dispose();
        super.onDestroy();
    }

    private void initView() {
        membersRV = findViewById(R.id.membersRV);
    }

    private void initDecorations() {
        membersRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
    }

    private void initData() {
        List<Entities.Membership> memberships = DatabaseHelper.getMemberships(complexId);
        Entities.User me = DatabaseHelper.getMe();
        if (me != null)
            membersRV.setAdapter(new MembersAdapter(this, me.getBaseUserId(), complexId, memberships));
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
