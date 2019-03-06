package kasper.android.pulse.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kasper.android.pulse.R;
import kasper.android.pulse.adapters.ComplexesInviteAdapter;
import kasper.android.pulse.helpers.DatabaseHelper;

public class ComplexInvitesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complex_invites);

        long complexId = 0;
        if (getIntent().getExtras() != null)
            complexId = getIntent().getExtras().getLong("complex_id");

        recyclerView = findViewById(R.id.complexInvitesRV);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        recyclerView.setAdapter(new ComplexesInviteAdapter(DatabaseHelper.getComplexInvites(complexId)));
    }

    @Override
    protected void onDestroy() {
        if (recyclerView.getAdapter() != null)
            ((ComplexesInviteAdapter) recyclerView.getAdapter()).dispose();
        super.onDestroy();
    }

    public void onBackBtnClicked(View view) {
        this.onBackPressed();
    }
}
